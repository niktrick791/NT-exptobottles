package de.niktr.purpur.exptobottles.vacuum;

import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
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
        this.vacuumTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::processVacuumCycle, interval, interval);
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
            return true; // Now enabled
        } else {
            disabledPlayers.add(uuid);
            return false; // Now disabled
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
            if (!(entity instanceof ExperienceOrb orb)) continue;
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
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.1f);
        }

        if (config.isParticlesOnPickup()) {
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1.0, 0), 4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    /**
     * Simulates vanilla Mending repair: 1 XP repairs 2 points of durability.
     */
    private int repairMendingItems(Player player, int xp) {
        List<ItemStack> damagedMendingItems = new ArrayList<>();

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (isDamagedMending(armor)) damagedMendingItems.add(armor);
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isDamagedMending(mainHand)) damagedMendingItems.add(mainHand);
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isDamagedMending(offHand)) damagedMendingItems.add(offHand);

        if (damagedMendingItems.isEmpty()) {
            return xp;
        }

        while (xp > 0 && !damagedMendingItems.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(damagedMendingItems.size());
            ItemStack item = damagedMendingItems.get(index);
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof Damageable damageable && damageable.hasDamage() && damageable.getDamage() > 0) {
                int damage = damageable.getDamage();
                int xpNeeded = (int) Math.ceil(damage / 2.0);
                int xpUsed = Math.min(xp, xpNeeded);
                int repaired = xpUsed * 2;

                damageable.setDamage(Math.max(0, damage - repaired));
                item.setItemMeta(damageable);
                xp -= xpUsed;

                if (damageable.getDamage() <= 0) {
                    damagedMendingItems.remove(index);
                }
            } else {
                damagedMendingItems.remove(index);
            }
        }

        return xp;
    }

    private boolean isDamagedMending(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (!item.containsEnchantment(Enchantment.MENDING)) return false;
        ItemMeta meta = item.getItemMeta();
        return meta instanceof Damageable damageable && damageable.hasDamage() && damageable.getDamage() > 0;
    }
}
