package com.rogueforge.game.data;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Generic cached registry for static JSON-authored definitions.
 */
public final class DefinitionRegistry<T> {
    private static final CopyOnWriteArrayList<DefinitionRegistry<?>> REGISTRIES = new CopyOnWriteArrayList<>();

    private final String assetPath;
    private final Class<T[]> arrayType;
    private final Function<T, String> idExtractor;
    private final DefinitionValidator<T> validator;

    private volatile boolean dirty = true;
    private volatile List<T> cachedDefinitions = Collections.emptyList();
    private volatile Map<String, T> cachedById = Collections.emptyMap();
    private volatile List<String> validationErrors = Collections.emptyList();

    public DefinitionRegistry(String assetPath, Class<T[]> arrayType, Function<T, String> idExtractor) {
        this(assetPath, arrayType, idExtractor, null);
    }

    public DefinitionRegistry(String assetPath, Class<T[]> arrayType, Function<T, String> idExtractor,
                              DefinitionValidator<T> validator) {
        this.assetPath = assetPath;
        this.arrayType = arrayType;
        this.idExtractor = idExtractor;
        this.validator = validator;
        REGISTRIES.add(this);
    }

    public List<T> getAll() {
        ensureLoaded();
        return cachedDefinitions;
    }

    public T get(String id) {
        ensureLoaded();
        return id != null ? cachedById.get(id) : null;
    }

    public List<String> getValidationErrors() {
        ensureLoaded();
        return validationErrors;
    }

    public synchronized void reload() {
        dirty = false;
        loadDefinitions();
    }

    public static void reloadAll() {
        for (DefinitionRegistry<?> registry : REGISTRIES) {
            registry.reload();
        }
    }

    private void ensureLoaded() {
        if (!dirty) {
            return;
        }
        synchronized (this) {
            if (!dirty) {
                return;
            }
            dirty = false;
            loadDefinitions();
        }
    }

    private void loadDefinitions() {
        T[] loaded = DefinitionJson.loadArray(assetPath, arrayType);
        List<T> validDefinitions = new ArrayList<>();
        Map<String, T> definitionsById = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        if (loaded == null) {
            errors.add("Missing or unreadable definition file: " + assetPath);
        } else {
            for (int i = 0; i < loaded.length; i++) {
                T definition = loaded[i];
                if (definition == null) {
                    errors.add(assetPath + "[" + i + "] is null.");
                    continue;
                }

                String id = idExtractor != null ? idExtractor.apply(definition) : null;
                ValidationCollector collector = new ValidationCollector(assetPath, i, id);
                if (validator != null) {
                    validator.validate(definition, collector);
                }
                if (id == null || id.trim().isEmpty()) {
                    collector.error("Missing id.");
                }

                if (collector.hasErrors()) {
                    errors.addAll(collector.getErrors());
                    continue;
                }

                if (definitionsById.containsKey(id)) {
                    errors.add(assetPath + "[" + i + "] duplicates id '" + id + "'.");
                    continue;
                }

                validDefinitions.add(definition);
                definitionsById.put(id, definition);
            }
        }

        cachedDefinitions = Collections.unmodifiableList(validDefinitions);
        cachedById = Collections.unmodifiableMap(definitionsById);
        validationErrors = Collections.unmodifiableList(errors);
        logValidationErrors(errors);
    }

    private void logValidationErrors(List<String> errors) {
        if (errors.isEmpty() || Gdx.app == null) {
            return;
        }
        for (String error : errors) {
            Gdx.app.error("DefinitionRegistry", error);
        }
    }

    @FunctionalInterface
    public interface DefinitionValidator<T> {
        void validate(T definition, ValidationCollector collector);
    }

    public static final class ValidationCollector {
        private final String assetPath;
        private final int index;
        private final String id;
        private final List<String> errors = new ArrayList<>();

        private ValidationCollector(String assetPath, int index, String id) {
            this.assetPath = assetPath;
            this.index = index;
            this.id = id;
        }

        public void require(boolean condition, String message) {
            if (!condition) {
                error(message);
            }
        }

        public void requireText(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                error("Missing required field '" + fieldName + "'.");
            }
        }

        public void error(String message) {
            String prefix = assetPath + "[" + index + "]";
            if (id != null && !id.isEmpty()) {
                prefix += " (" + id + ")";
            }
            errors.add(prefix + ": " + message);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
