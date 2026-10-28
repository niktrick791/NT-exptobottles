package de.niktr.purpur.exptobottles;

import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceManagerTest {

    @Test
    void testExpToNextLevel() {
        // Level 0 requires 7 XP to reach Level 1
        assertEquals(7, ExperienceManager.getExpToNextLevel(0));
        // Level 15 requires 2*15 + 7 = 37 XP
        assertEquals(37, ExperienceManager.getExpToNextLevel(15));
        // Level 16 requires 5*16 - 38 = 42 XP
        assertEquals(42, ExperienceManager.getExpToNextLevel(16));
        // Level 30 requires 5*30 - 38 = 112 XP
        assertEquals(112, ExperienceManager.getExpToNextLevel(30));
        // Level 31 requires 9*31 - 158 = 121 XP
        assertEquals(121, ExperienceManager.getExpToNextLevel(31));
    }

    @Test
    void testTotalExpAtLevel() {
        // Level 0: 0 XP
        assertEquals(0, ExperienceManager.getTotalExpAtLevel(0));
        // Level 1: 7 XP
        assertEquals(7, ExperienceManager.getTotalExpAtLevel(1));
        // Level 16: 352 XP
        assertEquals(352, ExperienceManager.getTotalExpAtLevel(16));
        // Level 30: 1395 XP
        assertEquals(1395, ExperienceManager.getTotalExpAtLevel(30));
        // Level 31: 1507 XP
        assertEquals(1507, ExperienceManager.getTotalExpAtLevel(31));
        // Level 32: 1628 XP
        assertEquals(1628, ExperienceManager.getTotalExpAtLevel(32));
    }

    @Test
    void testGetLevelFromExp() {
        assertEquals(0, ExperienceManager.getLevelFromExp(0));
        assertEquals(0, ExperienceManager.getLevelFromExp(6));
        assertEquals(1, ExperienceManager.getLevelFromExp(7));
        assertEquals(15, ExperienceManager.getLevelFromExp(351));
        assertEquals(16, ExperienceManager.getLevelFromExp(352));
        assertEquals(30, ExperienceManager.getLevelFromExp(1395));
        assertEquals(30, ExperienceManager.getLevelFromExp(1506));
        assertEquals(31, ExperienceManager.getLevelFromExp(1507));
        assertEquals(32, ExperienceManager.getLevelFromExp(1628));
        assertEquals(100, ExperienceManager.getLevelFromExp(ExperienceManager.getTotalExpAtLevel(100)));
    }
}
