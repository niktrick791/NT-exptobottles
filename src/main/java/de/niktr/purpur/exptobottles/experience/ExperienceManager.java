package de.niktr.purpur.exptobottles.experience;

import org.bukkit.entity.Player;

/**
 * Utility class for accurately calculating and manipulating Minecraft player experience.
 */
public final class ExperienceManager {

    private ExperienceManager() {}

    /**
     * Calculates the experience points required to advance from the given level to the next.
     *
     * @param level the current level
     * @return the experience required for the next level
     */
    public static int getExpToNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    /**
     * Calculates the total experience points required from level 0 to reach the given level.
     *
     * @param level target level
     * @return total experience points
     */
    public static int getTotalExpAtLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) Math.round(2.5 * level * level - 40.5 * level + 360);
        } else {
            double total = 4.5 * (double) level * (double) level - 162.5 * (double) level + 2220.0;
            if (total >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) Math.round(total);
        }
    }

    /**
     * Determines the player's level from total experience points using binary search.
     *
     * @param exp total experience points
     * @return level corresponding to the experience
     */
    public static int getLevelFromExp(int exp) {
        if (exp <= 0) return 0;
        int low = 0;
        int high = 21863; // Maximum level before 32-bit signed integer overflow
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midExp = getTotalExpAtLevel(mid);
            if (midExp <= exp) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return high;
    }

    /**
     * Calculates the total experience points of a player, including current level progress.
     *
     * @param player player to inspect
     * @return total experience points
     */
    public static int getPlayerTotalExp(Player player) {
        int level = player.getLevel();
        int expToNext = getExpToNextLevel(level);
        int currentProgress = Math.round(player.getExp() * expToNext);
        return getTotalExpAtLevel(level) + currentProgress;
    }

    /**
     * Sets a player's experience points accurately, updating level and progress bar correctly.
     *
     * @param player player to update
     * @param totalExp new total experience points
     */
    public static void setPlayerTotalExp(Player player, int totalExp) {
        if (totalExp < 0) {
            totalExp = 0;
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int level = getLevelFromExp(totalExp);
        int levelExp = getTotalExpAtLevel(level);
        int remaining = totalExp - levelExp;
        int expToNext = getExpToNextLevel(level);

        player.setLevel(level);
        if (expToNext > 0) {
            player.setExp((float) remaining / (float) expToNext);
        } else {
            player.setExp(0);
        }
        player.setTotalExperience(totalExp);
    }

    /**
     * Adds experience points to a player.
     *
     * @param player player
     * @param amount experience points to add
     */
    public static void addPlayerExp(Player player, int amount) {
        if (amount <= 0) return;
        setPlayerTotalExp(player, getPlayerTotalExp(player) + amount);
    }

    /**
     * Removes experience points from a player.
     *
     * @param player player
     * @param amount experience points to remove
     */
    public static void removePlayerExp(Player player, int amount) {
        if (amount <= 0) return;
        int current = getPlayerTotalExp(player);
        setPlayerTotalExp(player, Math.max(0, current - amount));
    }

    /**
     * Calculates how many total experience points correspond to a given number of levels
     * starting from the player's current level downwards.
     *
     * @param player player
     * @param levels number of levels to take
     * @return experience points corresponding to those levels
     */
    public static int getExpForLevelsDown(Player player, int levels) {
        int currentTotal = getPlayerTotalExp(player);
        int targetLevel = Math.max(0, player.getLevel() - levels);
        int targetTotal = getTotalExpAtLevel(targetLevel);
        return Math.max(0, currentTotal - targetTotal);
    }
}
