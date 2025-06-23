package github.kyradev.jarpmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class FakePlayerMobSpawner {

    private static final String TAG_FAKE_PLAYER = "FakePlayerSpawner";
    private static final int SPAWN_INTERVAL_TICKS = 1;
    private static final int MODE_CHECK_INTERVAL_TICKS = 400;
    public static final int MAX_SPAWN_RADIUS = 128;
    public static final int MIN_SPAWN_DISTANCE = 24;
    private static final int MONSTER_MOB_CAP = 70;
    public static final boolean DEBUG_MODE = true;
    private static final int CHUNK_RADIUS = 2;

    private static Difficulty lastCheckedDifficulty = Difficulty.PEACEFUL;
    private static long lastModeCheckTime = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (level == null) return;

        long currentTime = level.getGameTime();

        if (currentTime - lastModeCheckTime > MODE_CHECK_INTERVAL_TICKS) {
            lastCheckedDifficulty = level.getDifficulty();
            lastModeCheckTime = currentTime;
        }

        if (lastCheckedDifficulty == Difficulty.PEACEFUL || currentTime % SPAWN_INTERVAL_TICKS != 0) return;

        List<ArmorStand> armorStands = new ArrayList<>();
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof ArmorStand armorStand && armorStand.getPersistentData().getBoolean(TAG_FAKE_PLAYER)) {
                armorStands.add(armorStand);
            }
        }

        if (armorStands.isEmpty()) return;

        for (ArmorStand armorStand : armorStands) {
            BlockPos center = armorStand.blockPosition();

            if (MobSpawnerUtils.isPlayerNearby(level, center, getChunkRadius() * 16)) continue;

            if (level.getEntitiesOfClass(Monster.class, armorStand.getBoundingBox().inflate(MAX_SPAWN_RADIUS)).size() >= MONSTER_MOB_CAP) continue;

            MobSpawnerUtils.attemptMobSpawn(level, center);
        }
    }

    public static int getChunkRadius() {
        return CHUNK_RADIUS;
    }
}