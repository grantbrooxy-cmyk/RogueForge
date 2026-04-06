package com.rogueforge.game.combat;

import com.rogueforge.game.data.DefinitionRegistries;
import com.rogueforge.game.progression.AbilityProgressionState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            loadDefinitions();
        }
    }

    private static void loadDefinitions() {
        for (AbilityDefinition definition : DefinitionRegistries.ABILITIES.getAll()) {
            register(definition);
        }
    }

    public static void reloadDefinitions() {
        clear();
        ensureInitialized();
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
        return createInstances(abilityIds, null);
    }

    public static List<AbilityInstance> createInstances(List<String> abilityIds, Map<String, AbilityProgressionState> progressionStates) {
        ensureInitialized();
        List<AbilityInstance> instances = new ArrayList<>();

        if (abilityIds == null || abilityIds.isEmpty()) {
            return instances;
        }

        for (String abilityId : abilityIds) {
            AbilityDefinition definition = get(abilityId);
            if (definition != null) {
                AbilityProgressionState progressionState = progressionStates != null
                    ? progressionStates.get(abilityId)
                    : null;
                if (progressionStates != null && progressionState == null) {
                    progressionState = new AbilityProgressionState(abilityId);
                    progressionStates.put(abilityId, progressionState);
                }
                instances.add(new AbilityInstance(definition, progressionState));
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
