package de.niktr.purpur.exptobottles.bottle;

import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BottleManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final NamespacedKey xpKey;
    private final NamespacedKey ownerKey;

    public BottleManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.xpKey = new NamespacedKey(plugin, "stored_xp");
        this.ownerKey = new NamespacedKey(plugin, "bottle_owner");
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
        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(xpKey, PersistentDataType.INTEGER);
    }

    /**
     * Retrieves the stored experience amount from an ItemStack.
     */
    public int getStoredExp(ItemStack item) {
        if (!isExpBottle(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        Integer xp = meta.getPersistentDataContainer().get(xpKey, PersistentDataType.INTEGER);
        return xp != null ? xp : 0;
    }

    /**
     * Creates a custom experience bottle containing the specified amount of XP.
     */
    public ItemStack createExpBottle(int xp, String ownerName) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta meta = item.getItemMeta();

        // Store PDC data
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(xpKey, PersistentDataType.INTEGER, xp);
        if (ownerName != null) {
            pdc.set(ownerKey, PersistentDataType.STRING, ownerName);
        }

        int approxLevels = ExperienceManager.getLevelFromExp(xp);

        // Display Name
        meta.displayName(config.parse(config.getItemName(), "xp", xp, "levels", approxLevels, "player", ownerName != null ? ownerName : "Unbekannt"));

        // Lore
        List<Component> loreList = new ArrayList<>();
        for (String line : config.getItemLore()) {
            loreList.add(config.parse(line, "xp", xp, "levels", approxLevels, "player", ownerName != null ? ownerName : "Unbekannt"));
        }
        meta.lore(loreList);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Attempts to bottle XP from a player into a bottle.
     *
     * @param player player to bottle XP from
     * @param requestedXp XP points requested to bottle
     * @return true if bottling was successful, false otherwise
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
        // First check hands
        if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) return true;
        if (player.getInventory().getItemInOffHand().getType() == Material.GLASS_BOTTLE) return true;
        return player.getInventory().contains(Material.GLASS_BOTTLE);
    }

    /**
     * Removes one glass bottle from player (hand preferred, then inventory).
     */
    public void removeGlassBottle(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.GLASS_BOTTLE) {
            main.setAmount(main.getAmount() - 1);
            return;
        }

        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == Material.GLASS_BOTTLE) {
            off.setAmount(off.getAmount() - 1);
            return;
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == Material.GLASS_BOTTLE) {
                item.setAmount(item.getAmount() - 1);
                return;
            }
        }
    }

    /**
     * Plays sound and particle effects when filling a bottle.
     */
    public void playFillEffects(Player player) {
        if (config.isSoundsEnabled()) {
            try {
                Sound sound = Sound.valueOf(config.getSoundFill());
                player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    /**
     * Plays sound effects when consuming a bottle.
     */
    public void playConsumeEffects(Player player) {
        if (config.isSoundsEnabled()) {
            try {
                Sound sound = Sound.valueOf(config.getSoundConsume());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
