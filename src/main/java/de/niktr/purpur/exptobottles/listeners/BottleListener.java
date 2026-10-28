package de.niktr.purpur.exptobottles.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.niktr.purpur.exptobottles.bottle.BottleManager;
import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import de.niktr.purpur.exptobottles.vacuum.XpVacuumManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class BottleListener implements Listener {

    private final BottleManager bottleManager;
    private final ConfigManager config;
    private final XpVacuumManager vacuumManager;

    public BottleListener(BottleManager bottleManager, ConfigManager config, XpVacuumManager vacuumManager) {
        this.bottleManager = bottleManager;
        this.config = config;
        this.vacuumManager = vacuumManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only process MAIN_HAND interactions to prevent double triggering
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Feature 1: Sneak + Right-Click with Glass Bottle to fill XP
        if (item.getType() == Material.GLASS_BOTTLE && player.isSneaking() && config.isSneakRightClickToFill()) {
            if (!player.hasPermission("exptobottles.use")) {
                player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return;
            }

            event.setCancelled(true);

            int amountToBottle;
            String mode = config.getSneakFillMode().toUpperCase();
            if (mode.equals("ALL")) {
                amountToBottle = ExperienceManager.getPlayerTotalExp(player);
            } else if (mode.equals("LEVEL")) {
                if (player.getLevel() > 0) {
                    amountToBottle = ExperienceManager.getExpForLevelsDown(player, 1);
                } else {
                    amountToBottle = ExperienceManager.getPlayerTotalExp(player);
                }
            } else {
                try {
                    amountToBottle = Integer.parseInt(mode);
                } catch (NumberFormatException e) {
                    amountToBottle = ExperienceManager.getExpForLevelsDown(player, 1);
                }
            }

            bottleManager.bottleExp(player, amountToBottle);
            return;
        }

        // Feature 2: Right-Click with Custom XP Bottle to directly consume / absorb XP
        if (bottleManager.isExpBottle(item)) {
            if (config.isDirectConsumeOnRightClick()) {
                event.setCancelled(true);

                if (!player.hasPermission("exptobottles.use")) {
                    player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                    return;
                }

                int storedXp = bottleManager.getStoredExp(item);
                if (storedXp <= 0) return;

                // Consume 1 bottle
                item.setAmount(item.getAmount() - 1);

                // Add XP to player
                ExperienceManager.addPlayerExp(player, storedXp);

                // Return empty glass bottle if configured
                if (config.isReturnEmptyBottleOnConsume()) {
                    ItemStack emptyBottle = new ItemStack(Material.GLASS_BOTTLE, 1);
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(emptyBottle);
                    if (!overflow.isEmpty()) {
                        for (ItemStack leftover : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                        }
                    }
                }

                // Play effect and feedback
                bottleManager.playConsumeEffects(player);
                player.sendMessage(config.getPrefixedMessage(config.getConsumedSuccess(), "xp", storedXp));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownExpBottle thrownBottle) {
            ItemStack item = thrownBottle.getItem();
            if (bottleManager.isExpBottle(item)) {
                int storedXp = bottleManager.getStoredExp(item);
                thrownBottle.getPersistentDataContainer().set(bottleManager.getXpKey(), PersistentDataType.INTEGER, storedXp);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExpBottleBreak(ExpBottleEvent event) {
        ThrownExpBottle bottle = event.getEntity();
        if (bottle.getPersistentDataContainer().has(bottleManager.getXpKey(), PersistentDataType.INTEGER)) {
            Integer stored = bottle.getPersistentDataContainer().get(bottleManager.getXpKey(), PersistentDataType.INTEGER);
            if (stored != null && stored > 0) {
                event.setExperience(stored);
                event.setShowEffect(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupExp(PlayerPickupExperienceEvent event) {
        if (vacuumManager != null && vacuumManager.isVacuumEnabled(event.getPlayer())) {
            // Instantly vacuum all adjacent orbs when player touches any orb
            vacuumManager.processPlayerVacuum(event.getPlayer());
        }
    }
}
