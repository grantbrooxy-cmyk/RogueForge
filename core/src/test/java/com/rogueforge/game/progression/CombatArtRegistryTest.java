package com.rogueforge.game.progression;

import com.rogueforge.game.combat.WeaponType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatArtRegistryTest {

    @Test
    void returnsExpectedArtIdsForSwordMilestones() {
        assertEquals("sword_cleave", CombatArtRegistry.getArtId(WeaponType.SWORD, 1));
        assertEquals("sword_guard_break", CombatArtRegistry.getArtIdForLevel(WeaponType.SWORD, 6));
        assertEquals("sword_last_rites", CombatArtRegistry.getArtIdForLevel(WeaponType.SWORD, 10));
    }

    @Test
    void returnsExpectedUnlockLevelsAndTierLookup() {
        assertEquals(3, CombatArtRegistry.levelForTier(1));
        assertEquals(6, CombatArtRegistry.levelForTier(2));
        assertEquals(10, CombatArtRegistry.levelForTier(3));
        assertEquals(2, CombatArtRegistry.tierOf(WeaponType.SWORD, "sword_guard_break"));
        assertEquals(-1, CombatArtRegistry.tierOf(WeaponType.SWORD, "missing"));
    }

    @Test
    void nonCombatWeaponTypeReturnsEmptyValues() {
        assertEquals("", CombatArtRegistry.getArtId(WeaponType.NONE, 1));
        assertEquals("", CombatArtRegistry.getArtIdForLevel(WeaponType.NONE, 3));
        assertEquals(List.of(), CombatArtRegistry.getUnlockedAtLevel(WeaponType.NONE, 3));
        assertTrue(CombatArtRegistry.getAllArtIds(WeaponType.NONE).isEmpty());
    }
}
