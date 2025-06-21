package github.kyradev.jarpmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class FakePlayerMobSpawner {

    private static final String TAG_FAKE_PLAYER = "FakePlayerSpawner";
    private static final int SPAWN_INTERVAL_TICKS = 60;
    private static final int MAX_SPAWN_RADIUS = 32;
    private static final Random RANDOM = new Random();

    // Definisci tipi di mob con i rispettivi pesi (basati su spawn table vanilla)
    private static final List<MobEntry> SPAWNABLE_MOBS = List.of(
            new MobEntry(EntityType.ZOMBIE, 40),
            new MobEntry(EntityType.SKELETON, 40),
            new MobEntry(EntityType.CREEPER, 20),
            new MobEntry(EntityType.SPIDER, 30),
            new MobEntry(EntityType.WITCH, 5)
    );

    private static final int TOTAL_WEIGHT = SPAWNABLE_MOBS.stream().mapToInt(mob -> mob.weight).sum();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (level == null) return;

        if (level.getGameTime() % SPAWN_INTERVAL_TICKS != 0) return;

        List<ArmorStand> armorStands = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ArmorStand armorStand && armorStand.getPersistentData().getBoolean(TAG_FAKE_PLAYER)) {
                armorStands.add(armorStand);
            }
        }

        if (armorStands.isEmpty()) return;

        for (ArmorStand armorStand : armorStands) {
            BlockPos center = armorStand.blockPosition();

            for (int tries = 0; tries < 10; tries++) {
                BlockPos spawnPos = findValidSpawnPos(level, center, MAX_SPAWN_RADIUS);
                if (spawnPos == null) continue;

                EntityType<? extends Monster> mobType = selectMobTypeWeighted();

                Monster mob = mobType.create(level);
                if (mob != null && mob.checkSpawnRules(level, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(level)) {
                    var spawned = mobType.spawn(level, null, (entity) -> {}, spawnPos, MobSpawnType.NATURAL, true, false);
                    if (spawned != null) {
                        return;
                    }
                }
            }
        }
    }

    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center, int radius) {
        int dx = RANDOM.nextInt(radius * 2 + 1) - radius;
        int dz = RANDOM.nextInt(radius * 2 + 1) - radius;

        for (int y = level.getMaxBuildHeight(); y > level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                return pos.above();
            }
        }
        return null;
    }

    private static EntityType<? extends Monster> selectMobTypeWeighted() {
        int randomWeight = RANDOM.nextInt(TOTAL_WEIGHT);
        int currentWeight = 0;
        for (MobEntry entry : SPAWNABLE_MOBS) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return entry.type;
            }
        }
        return EntityType.ZOMBIE; // fallback, non dovrebbe mai succedere
    }

    private static class MobEntry {
        final EntityType<? extends Monster> type;
        final int weight;

        MobEntry(EntityType<? extends Monster> type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }
}
,