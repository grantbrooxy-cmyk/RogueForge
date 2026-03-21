package com.rogueforge.game.combat;

import java.util.*;

/**
 * Static registry that maps ability IDs to their definitions.
 * Provides lookup and instantiation of abilities for entities.
 */
public class AbilityRegistry {

    private static Map<String, AbilityDefinition> registry = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Initializes the registry with default abilities.
     * Called automatically on first access.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            registerDefaults();
        }
    }

    /**
     * Registers all default ability definitions.
     * These match the abilityIds from robots.json
     */
    private static void registerDefaults() {
        // Hardcoded ability definitions matching robots.json abilityIds
        register(new AbilityDefinition(
            "dash",
            "Dash",
            AbilityDefinition.AbilityType.UTILITY,
            AbilityDefinition.TargetType.SELF,
            5f,
            0f,
            2f,
            "Quick dash increasing speed temporarily"
        ));

        register(new AbilityDefinition(
            "scan",
            "Scan",
            AbilityDefinition.AbilityType.UTILITY,
            AbilityDefinition.TargetType.SINGLE_ENEMY,
            8f,
            0f,
            5f,
            "Reveals enemy stats and weaknesses"
        ));

        register(new AbilityDefinition(
            "shield_wall",
            "Shield Wall",
            AbilityDefinition.AbilityType.BUFF,
            AbilityDefinition.TargetType.SELF,
            12f,
            15f,
            6f,
            "Greatly increases defense for 6 seconds"
        ));

        register(new AbilityDefinition(
            "taunt",
            "Taunt",
            AbilityDefinition.AbilityType.DEBUFF,
            AbilityDefinition.TargetType.ALL_ENEMIES,
            10f,
            0f,
            4f,
            "Forces enemies to target this robot"
        ));

        register(new AbilityDefinition(
            "power_strike",
            "Power Strike",
            AbilityDefinition.AbilityType.DAMAGE,
            AbilityDefinition.TargetType.SINGLE_ENEMY,
            6f,
            45f,
            0f,
            "A devastating single-target attack"
        ));

        register(new AbilityDefinition(
            "rapid_fire",
            "Rapid Fire",
            AbilityDefinition.AbilityType.DAMAGE,
            AbilityDefinition.TargetType.ALL_ENEMIES,
            10f,
            20f,
            0f,
            "Hits all enemies for moderate damage"
        ));

        register(new AbilityDefinition(
            "heal_pulse",
            "Heal Pulse",
            AbilityDefinition.AbilityType.HEAL,
            AbilityDefinition.TargetType.ALL_ALLIES,
            15f,
            30f,
            0f,
            "Heals all allies for 30 HP"
        ));

        register(new AbilityDefinition(
            "repair_aura",
            "Repair Aura",
            AbilityDefinition.AbilityType.HEAL,
            AbilityDefinition.TargetType.ALL_ALLIES,
            20f,
            5f,
            8f,
            "Heals allies over time for 8 seconds"
        ));
    }

    /**
     * Registers an ability definition in the registry
     *
     * @param definition The ability definition to register
     */
    public static void register(AbilityDefinition definition) {
        if (definition == null) {
            return;
        }
        registry.put(definition.getId(), definition);
    }

    /**
     * Looks up an ability definition by ID
     *
     * @param id The ability ID
     * @return The ability definition, or null if not found
     */
    public static AbilityDefinition get(String id) {
        ensureInitialized();
        return registry.get(id);
    }

    /**
     * Creates runtime ability instances from a list of ability IDs.
     * This should be called when initializing an entity's abilities.
     *
     * @param abilityIds List of ability IDs (e.g., from robots.json)
     * @return List of AbilityInstance objects, or empty list if abilityIds is null
     */
    public static List<AbilityInstance> createInstances(List<String> abilityIds) {
        ensureInitialized();
        List<AbilityInstance> instances = new ArrayList<>();

        if (abilityIds == null || abilityIds.isEmpty()) {
            return instances;
        }

        for (String abilityId : abilityIds) {
            AbilityDefinition definition = get(abilityId);
            if (definition != null) {
                instances.add(new AbilityInstance(definition));
            }
        }

        return instances;
    }

    /**
     * Gets all registered ability IDs
     *
     * @return Set of all registered ability IDs
     */
    public static Set<String> getAllIds() {
        ensureInitialized();
        return new HashSet<>(registry.keySet());
    }

    /**
     * Gets the total number of registered abilities
     *
     * @return Number of registered abilities
     */
    public static int getCount() {
        ensureInitialized();
        return registry.size();
    }

    /**
     * Clears the registry (useful for testing)
     */
    public static void clear() {
        registry.clear();
        initialized = false;
    }
}
