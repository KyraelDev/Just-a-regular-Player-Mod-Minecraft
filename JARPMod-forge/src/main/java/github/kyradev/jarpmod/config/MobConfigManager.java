package github.kyradev.jarpmod.config;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class MobConfigManager {
    private static final Map<EntityType<?>, Integer> mobWeights = new HashMap<>();

    public static void loadConfig() {
        // Placeholder for logic to load config from a file
        ForgeRegistries.ENTITY_TYPES.forEach((type) -> {
            if (!mobWeights.containsKey(type) && isValidCustomMob(type)) {
                mobWeights.put(type, 10); // Default weight
            }
        });
    }

    private static boolean isValidCustomMob(EntityType<?> type) {
        // Add logic for validating custom mobs
        return true;
    }

    public static int getMobWeight(EntityType<?> type) {
        return mobWeights.getOrDefault(type, 0);
    }
}