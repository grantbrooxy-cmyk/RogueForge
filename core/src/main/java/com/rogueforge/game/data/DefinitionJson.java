package com.rogueforge.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.Element;
import com.rogueforge.game.combat.StatusEffectType;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.robot.RobotDefinition;

/**
 * Shared LibGDX Json configuration for gameplay definitions loaded from assets.
 */
public final class DefinitionJson {
    private static final Json BASE_JSON = createBaseJson();
    private static final Json JSON = createConfiguredJson();

    private DefinitionJson() {
    }

    public static Json newJson() {
        return createConfiguredJson();
    }

    public static <T> T[] loadArray(String assetPath, Class<T[]> arrayType) {
        FileHandle file = Gdx.files.internal(assetPath);
        if (!file.exists()) {
            return null;
        }
        return JSON.fromJson(arrayType, file.readString());
    }

    private static Json createConfiguredJson() {
        Json json = createBaseJson();
        json.setSerializer(AbilityDefinition.class, new DelegatingSerializer<>(AbilityDefinition.class));
        json.setSerializer(MonsterDefinition.class, new DelegatingSerializer<>(MonsterDefinition.class));
        json.setSerializer(ZoneDefinition.class, new DelegatingSerializer<>(ZoneDefinition.class));
        json.setSerializer(RobotDefinition.class, new RobotDefinitionSerializer());
        json.setSerializer(EquipmentItem.class, new DelegatingSerializer<>(EquipmentItem.class));
        return json;
    }

    private static Json createBaseJson() {
        Json json = new Json();
        json.setSerializer(Element.class, new ElementSerializer());
        json.setSerializer(WeaponType.class, new WeaponTypeSerializer());
        json.setSerializer(StatusEffectType.class, new StatusEffectTypeSerializer());
        json.setSerializer(AbilityDefinition.AbilityType.class,
            new EnumByNameSerializer<>(AbilityDefinition.AbilityType.class, null));
        json.setSerializer(AbilityDefinition.TargetType.class,
            new EnumByNameSerializer<>(AbilityDefinition.TargetType.class, null));
        json.setSerializer(RobotDefinition.RoleEnum.class,
            new EnumByNameSerializer<>(RobotDefinition.RoleEnum.class, null));
        json.setSerializer(RobotDefinition.EquipmentSlot.class,
            new EnumByNameSerializer<>(RobotDefinition.EquipmentSlot.class, null));
        return json;
    }

    private static class DelegatingSerializer<T> implements Json.Serializer<T> {
        private final Class<T> type;

        private DelegatingSerializer(Class<T> type) {
            this.type = type;
        }

        @Override
        public void write(Json json, T object, Class knownType) {
            json.writeFields(object);
        }

        @Override
        public T read(Json json, JsonValue jsonData, Class type) {
            return BASE_JSON.fromJson(this.type, jsonData.toString());
        }
    }

    private static class RobotDefinitionSerializer implements Json.Serializer<RobotDefinition> {
        @Override
        public void write(Json json, RobotDefinition object, Class knownType) {
            json.writeFields(object);
        }

        @Override
        public RobotDefinition read(Json json, JsonValue jsonData, Class type) {
            RobotDefinition definition = BASE_JSON.fromJson(RobotDefinition.class, jsonData.toString());
            if (definition.getEquipmentSlots() == null) {
                definition.setEquipmentSlots(new RobotDefinition.EquipmentSlot[0]);
            }
            if (definition.getAbilityIds() == null) {
                definition.setAbilityIds(null);
            }
            return definition;
        }
    }

    private static class ElementSerializer extends EnumByNameSerializer<Element> {
        private ElementSerializer() {
            super(Element.class, Element.NONE);
        }

        @Override
        protected Element parse(String rawValue) {
            return Element.fromString(rawValue);
        }
    }

    private static class WeaponTypeSerializer extends EnumByNameSerializer<WeaponType> {
        private WeaponTypeSerializer() {
            super(WeaponType.class, WeaponType.NONE);
        }

        @Override
        protected WeaponType parse(String rawValue) {
            if (rawValue == null || rawValue.isEmpty()) {
                return WeaponType.NONE;
            }
            try {
                return WeaponType.valueOf(rawValue.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return WeaponType.NONE;
            }
        }
    }

    private static class StatusEffectTypeSerializer extends EnumByNameSerializer<StatusEffectType> {
        private StatusEffectTypeSerializer() {
            super(StatusEffectType.class, null);
        }
    }

    private static class EnumByNameSerializer<T extends Enum<T>> implements Json.Serializer<T> {
        private final Class<T> enumType;
        private final T fallbackValue;

        private EnumByNameSerializer(Class<T> enumType, T fallbackValue) {
            this.enumType = enumType;
            this.fallbackValue = fallbackValue;
        }

        @Override
        public void write(Json json, T object, Class knownType) {
            json.writeValue(object != null ? object.name() : null);
        }

        @Override
        public T read(Json json, JsonValue jsonData, Class type) {
            String rawValue = jsonData == null ? null : jsonData.asString();
            if (rawValue == null || rawValue.isEmpty()) {
                return fallbackValue;
            }
            return parse(rawValue);
        }

        protected T parse(String rawValue) {
            try {
                return Enum.valueOf(enumType, rawValue.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return fallbackValue;
            }
        }
    }
}
