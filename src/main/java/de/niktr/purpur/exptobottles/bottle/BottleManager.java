package de.niktr.purpur.exptobottles.bottle;

import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import de.niktr.purpur.exptobottles.experience.XVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BottleManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private NamespacedKey xpKey;
    private NamespacedKey ownerKey;

    public BottleManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        try {
            this.xpKey = new NamespacedKey(plugin, "stored_xp");
            this.ownerKey = new NamespacedKey(plugin, "bottle_owner");
        } catch (Throwable ignored) {
            // Pre-1.13 servers without NamespacedKey
            this.xpKey = null;
            this.ownerKey = null;
        }
    }

    public NamespacedKey getXpKey() {
        return xpKey;
    }

    public NamespacedKey getOwnerKey() {
        return ownerKey;
    }

    /**
     * Checks if an ItemStack is an experience bottle created by this plugin.
     */
    public boolean isExpBottle(ItemStack item) {
        if (item == null || !XVersion.isExpBottleMaterial(item.getType()) || !item.hasItemMeta()) {
            return false;
        }
        return getStoredExp(item) > 0;
    }

    /**
     * Retrieves the stored experience amount from an ItemStack.
     */
    public int getStoredExp(ItemStack item) {
        return XVersion.readXp(item, xpKey);
    }

    /**
     * Creates a custom experience bottle containing the specified amount of XP.
     */
    public ItemStack createExpBottle(int xp, String ownerName) {
        ItemStack item = new ItemStack(XVersion.getExpBottleMaterial(), 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        XVersion.storeXp(meta, xpKey, xp, ownerName);

        int approxLevels = ExperienceManager.getLevelFromExp(xp);

        // Display Name
        meta.setDisplayName(config.parse(config.getItemName(), "xp", xp, "levels", approxLevels, "player", ownerName != null ? ownerName : "Unbekannt"));

        // Lore
        List<String> loreList = new ArrayList<>();
        for (String line : config.getItemLore()) {
            loreList.add(config.parse(line, "xp", xp, "levels", approxLevels, "player", ownerName != null ? ownerName : "Unbekannt"));
        }
        meta.setLore(loreList);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Attempts to bottle XP from a player into a bottle.
     */
    public boolean bottleExp(Player player, int requestedXp) {
        int playerTotalExp = ExperienceManager.getPlayerTotalExp(player);

        if (requestedXp < config.getMinXp()) {
            player.sendMessage(config.getPrefixedMessage(config.getMinXpError(), "min", config.getMinXp()));
            return false;
        }

        if (config.getMaxXpPerBottle() > 0 && requestedXp > config.getMaxXpPerBottle()) {
            player.sendMessage(config.getPrefixedMessage(config.getMaxXpError(), "max", config.getMaxXpPerBottle()));
            return false;
        }

        if (playerTotalExp < requestedXp) {
            player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", playerTotalExp));
            return false;
        }

        // Check empty bottle requirement
        if (config.isRequireEmptyBottle()) {
            if (!hasGlassBottle(player)) {
                player.sendMessage(config.getPrefixedMessage(config.getNoBottle()));
                return false;
            }
            removeGlassBottle(player);
        }

        // Calculate tax
        int tax = 0;
        if (config.getTaxPercent() > 0.0) {
            tax = (int) Math.round(requestedXp * (config.getTaxPercent() / 100.0));
        }
        int actualStoredXp = Math.max(1, requestedXp - tax);

        // Deduct player XP
        ExperienceManager.removePlayerExp(player, requestedXp);

        // Create and give bottle
        ItemStack bottle = createExpBottle(actualStoredXp, player.getName());
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(bottle);
        if (!overflow.isEmpty()) {
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        // Play effects
        playFillEffects(player);

        // Send feedback
        if (tax > 0) {
            player.sendMessage(config.getPrefixedMessage(config.getBottledSuccessTax(), "xp", actualStoredXp, "tax", tax));
        } else {
            player.sendMessage(config.getPrefixedMessage(config.getBottledSuccess(), "xp", actualStoredXp));
        }

        return true;
    }

    /**
     * Checks if player has at least one glass bottle in hand or inventory.
     */
    public boolean hasGlassBottle(Player player) {
        ItemStack inHand = XVersion.getItemInHand(player);
        if (inHand != null && inHand.getType() == Material.GLASS_BOTTLE) return true;
        try {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off != null && off.getType() == Material.GLASS_BOTTLE) return true;
        } catch (Throwable ignored) {}
        return player.getInventory().contains(Material.GLASS_BOTTLE);
    }

    /**
     * Removes one glass bottle from player (hand preferred, then inventory).
     */
    public void removeGlassBottle(Player player) {
        ItemStack inHand = XVersion.getItemInHand(player);
        if (inHand != null && inHand.getType() == Material.GLASS_BOTTLE) {
            inHand.setAmount(inHand.getAmount() - 1);
            XVersion.setItemInHand(player, inHand.getAmount() <= 0 ? null : inHand);
            return;
        }

        try {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off != null && off.getType() == Material.GLASS_BOTTLE) {
                off.setAmount(off.getAmount() - 1);
                player.getInventory().setItemInOffHand(off.getAmount() <= 0 ? null : off);
                return;
            }
        } catch (Throwable ignored) {}

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    player.getInventory().setItem(i, null);
                }
                return;
            }
        }
    }

    /**
     * Plays sound effects when filling a bottle.
     */
    public void playFillEffects(Player player) {
        if (config.isSoundsEnabled()) {
            XVersion.playSound(player, config.getSoundFill(), "ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
        }
    }

    /**
     * Plays sound effects when consuming a bottle.
     */
    public void playConsumeEffects(Player player) {
        if (config.isSoundsEnabled()) {
            XVersion.playSound(player, config.getSoundConsume(), "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        }
    }
}
