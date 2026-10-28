package de.niktr.purpur.exptobottles.vacuum;

import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import de.niktr.purpur.exptobottles.experience.XVersion;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class XpVacuumManager {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private BukkitTask vacuumTask;
    private static final Enchantment MENDING_ENCH = Enchantment.getByName("MENDING");

    public XpVacuumManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() {
        if (vacuumTask != null) {
            vacuumTask.cancel();
        }

        if (!config.isFastPickupEnabled()) {
            return;
        }

        int interval = Math.max(1, config.getIntervalTicks());
        this.vacuumTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                processVacuumCycle();
            }
        }, interval, interval);
    }

    public void stop() {
        if (vacuumTask != null) {
            vacuumTask.cancel();
            vacuumTask = null;
        }
    }

    public boolean toggleVacuum(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            return true;
        } else {
            disabledPlayers.add(uuid);
            return false;
        }
    }

    public boolean isVacuumEnabled(Player player) {
        return config.isFastPickupEnabled()
                && !disabledPlayers.contains(player.getUniqueId())
                && player.hasPermission("exptobottles.use");
    }

    private void processVacuumCycle() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!isVacuumEnabled(player)) continue;
            if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR) continue;

            processPlayerVacuum(player);
        }
    }

    public void processPlayerVacuum(Player player) {
        double vacuumRadius = config.getVacuumRadius();
        double instantRadius = config.getInstantPickupRadius();
        double instantRadiusSq = instantRadius * instantRadius;

        Location playerLoc = player.getLocation();
        Collection<Entity> nearby = player.getWorld().getNearbyEntities(playerLoc, vacuumRadius, vacuumRadius, vacuumRadius);

        int collectedXp = 0;
        int orbsCollected = 0;
        int maxOrbs = config.getMaxOrbsPerBatch();

        Vector playerCenter = playerLoc.clone().add(0, 0.5, 0).toVector();

        for (Entity entity : nearby) {
            if (!(entity instanceof ExperienceOrb)) continue;
            ExperienceOrb orb = (ExperienceOrb) entity;
            if (orb.isDead() || !orb.isValid()) continue;

            double distSq = orb.getLocation().distanceSquared(playerLoc);

            // Close enough for instant absorption
            if (distSq <= instantRadiusSq && orbsCollected < maxOrbs) {
                collectedXp += orb.getExperience();
                orb.remove();
                orbsCollected++;
            } else if (config.isPullEffect() && distSq <= vacuumRadius * vacuumRadius) {
                // Smooth suction effect towards player
                Vector dir = playerCenter.clone().subtract(orb.getLocation().toVector()).normalize().multiply(0.45);
                orb.setVelocity(dir);
            }
        }

        if (collectedXp > 0) {
            applyCollectedXp(player, collectedXp);
        }
    }

    /**
     * Applies collected XP to the player, prioritizing Mending repairs if configured.
     */
    public void applyCollectedXp(Player player, int xp) {
        int remainingXp = xp;
        if (config.isRepairMending()) {
            remainingXp = repairMendingItems(player, xp);
        }

        if (remainingXp > 0) {
            ExperienceManager.addPlayerExp(player, remainingXp);
        }

        // Play feedback
        if (config.isPlaySoundOnPickup()) {
            XVersion.playSound(player, "ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
        }

        if (config.isParticlesOnPickup()) {
            try {
                // Safe reflection call for modern Particle API
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                Object happyVillager = Enum.valueOf((Class<Enum>) particleClass, "HAPPY_VILLAGER");
                Location loc = player.getLocation().add(0, 1.0, 0);
                player.getWorld().getClass().getMethod("spawnParticle", particleClass, Location.class, int.class, double.class, double.class, double.class, double.class)
                        .invoke(player.getWorld(), happyVillager, loc, 4, 0.3, 0.3, 0.3, 0.05);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Simulates vanilla Mending repair: 1 XP repairs 2 points of durability.
     */
    private int repairMendingItems(Player player, int xp) {
        if (MENDING_ENCH == null) {
            return xp; // Minecraft 1.8 has no Mending
        }

        List<ItemStack> damagedMendingItems = new ArrayList<>();

        if (player.getInventory().getArmorContents() != null) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (isDamagedMending(armor)) damagedMendingItems.add(armor);
            }
        }

        ItemStack mainHand = XVersion.getItemInHand(player);
        if (isDamagedMending(mainHand)) damagedMendingItems.add(mainHand);

        try {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (isDamagedMending(offHand)) damagedMendingItems.add(offHand);
        } catch (Throwable ignored) {}

        if (damagedMendingItems.isEmpty()) {
            return xp;
        }

        while (xp > 0 && !damagedMendingItems.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(damagedMendingItems.size());
            ItemStack item = damagedMendingItems.get(index);
            int damage = XVersion.getItemDamage(item);

            if (damage > 0) {
                int xpNeeded = (int) Math.ceil(damage / 2.0);
                int xpUsed = Math.min(xp, xpNeeded);
                int repaired = xpUsed * 2;

                int newDamage = Math.max(0, damage - repaired);
                XVersion.setItemDamage(item, newDamage);
                xp -= xpUsed;

                if (newDamage <= 0) {
                    damagedMendingItems.remove(index);
                }
            } else {
                damagedMendingItems.remove(index);
            }
        }

        return xp;
    }

    private boolean isDamagedMending(ItemStack item) {
        if (item == null || item.getType().name().endsWith("AIR")) return false;
        if (MENDING_ENCH == null || !item.containsEnchantment(MENDING_ENCH)) return false;
        return XVersion.getItemDamage(item) > 0;
    }
}
