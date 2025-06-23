package github.kyradev.jarpmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;

import java.util.Random;

public class MobSpawnerUtils {

    private static final Random RANDOM = new Random();
    private static final int MAX_LIGHT_LEVEL = 7;

    public static void attemptMobSpawn(ServerLevel level, BlockPos center) {
        if (isPlayerNearby(level, center, FakePlayerMobSpawner.getChunkRadius() * 16)) return;

        int spawnAttempts = FakePlayerMobSpawner.DEBUG_MODE ? 50 : 10;
        for (int tries = 0; tries < spawnAttempts; tries++) {
            BlockPos spawnPos = findValidSpawnPos(level, center, FakePlayerMobSpawner.MAX_SPAWN_RADIUS);
            if (spawnPos == null) continue;
            if (level.getBrightness(LightLayer.BLOCK, spawnPos) > MAX_LIGHT_LEVEL) continue;
            if (!isWithinDistance(center, spawnPos)) continue;
            if (isInLiquid(level, spawnPos)) continue;
            if (!isAreaClear(level, spawnPos)) continue;

            EntityType<? extends Monster> mobType = MobSelector.selectMobTypeWeighted(level, spawnPos);
            if (!hasEnoughSpace(level, spawnPos, mobType)) continue;

            Monster mob = mobType.create(level);
            if (mob != null && mob.checkSpawnRules(level, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(level)) {
                mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, RANDOM.nextFloat() * 360F, 0F);
                MobSelector.applyAttributes(mob);
                level.addFreshEntity(mob);

                if (FakePlayerMobSpawner.DEBUG_MODE) {
                    System.out.println("Spawned mob: " + mob.getType().getDescriptionId() + " at " + spawnPos);
                }
            }
        }
    }

    private static boolean isAreaClear(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos);
        return level.getEntitiesOfClass(Monster.class, area).isEmpty();
    }

    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center, int radius) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int dx = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dz = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dy = RANDOM.nextInt(91) - 45;

            BlockPos pos = new BlockPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            BlockState belowBlock = level.getBlockState(pos.below());

            if (isSpawnableSurface(level, belowBlock, pos.below())) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSpawnableSurface(ServerLevel level, BlockState state, BlockPos pos) {
        return !state.getFluidState().is(FluidTags.WATER) &&
                !state.getFluidState().is(FluidTags.LAVA) &&
                state.isSolidRender(level, pos);
    }

    private static boolean hasEnoughSpace(ServerLevel level, BlockPos pos, EntityType<? extends Monster> mobType) {
        if (mobType == EntityType.SPIDER || mobType == EntityType.ENDERMAN) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!level.isEmptyBlock(pos.offset(dx, 0, dz)) || !level.isEmptyBlock(pos.offset(dx, 1, dz))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above());
    }

    private static boolean isWithinDistance(BlockPos center, BlockPos pos) {
        double distanceSquared = center.distSqr(pos);
        return distanceSquared > FakePlayerMobSpawner.MIN_SPAWN_DISTANCE * FakePlayerMobSpawner.MIN_SPAWN_DISTANCE &&
                distanceSquared <= FakePlayerMobSpawner.MAX_SPAWN_RADIUS * FakePlayerMobSpawner.MAX_SPAWN_RADIUS;
    }

    private static boolean isInLiquid(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getFluidState().is(FluidTags.WATER) ||
                level.getBlockState(pos).getFluidState().is(FluidTags.LAVA);
    }

    static boolean isPlayerNearby(ServerLevel level, BlockPos center, int radius) {
        return level.getNearestPlayer(center.getX(), center.getY(), center.getZ(), radius, true) != null;
    }
}