package github.kyradev.jarpmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class FakePlayerMobSpawner {

    private static final String TAG_FAKE_PLAYER = "FakePlayerSpawner";
    private static final int SPAWN_INTERVAL_TICKS = 1; // Check spawn every tick
    private static final int MODE_CHECK_INTERVAL_TICKS = 400; // Interval for checking mode
    private static final int MAX_SPAWN_RADIUS = 128;
    private static final int MIN_SPAWN_DISTANCE = 24; // Minimum distance from ArmorStand
    private static final int MAX_LIGHT_LEVEL = 7;
    private static final int MONSTER_MOB_CAP = 70;
    private static final Random RANDOM = new Random();
    private static final boolean DEBUG_MODE = true; // Toggle for debugging
    private static final int CHUNK_RADIUS = 2; // Radius in chunks around the ArmorStand

    private static final List<MobEntry> SPAWNABLE_MOBS = List.of(
            new MobEntry(EntityType.ZOMBIE, 40),
            new MobEntry(EntityType.SKELETON, 40),
            new MobEntry(EntityType.CREEPER, 20),
            new MobEntry(EntityType.SPIDER, 30),
            new MobEntry(EntityType.WITCH, 5),
            new MobEntry(EntityType.ENDERMAN, 10) // Add Enderman with some weight
    );

    private static final int TOTAL_WEIGHT = SPAWNABLE_MOBS.stream().mapToInt(mob -> mob.weight).sum();

    private static Difficulty lastCheckedDifficulty = Difficulty.PEACEFUL;
    private static long lastModeCheckTime = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (level == null) return;

        long currentTime = level.getGameTime();

        // Check mode at defined intervals
        if (currentTime - lastModeCheckTime > MODE_CHECK_INTERVAL_TICKS) {
            lastCheckedDifficulty = level.getDifficulty();
            lastModeCheckTime = currentTime;
        }

        // Skip spawning if in peaceful mode
        if (lastCheckedDifficulty == Difficulty.PEACEFUL) return;

        if (currentTime % SPAWN_INTERVAL_TICKS != 0) return;

        List<ArmorStand> armorStands = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ArmorStand armorStand && armorStand.getPersistentData().getBoolean(TAG_FAKE_PLAYER)) {
                armorStands.add(armorStand);
            }
        }

        if (armorStands.isEmpty()) return;

        for (ArmorStand armorStand : armorStands) {
            BlockPos center = armorStand.blockPosition();

            // Check for nearby players in a 2-chunk radius
            if (isPlayerNearby(level, center, CHUNK_RADIUS * 16)) {
                continue;
            }

            if (level.getEntitiesOfClass(Monster.class, armorStand.getBoundingBox().inflate(MAX_SPAWN_RADIUS)).size() >= MONSTER_MOB_CAP) {
                continue;
            }

            attemptMobSpawn(level, center);
        }
    }

    private static boolean isPlayerNearby(ServerLevel level, BlockPos center, int radius) {
        return level.getNearestPlayer(center.getX(), center.getY(), center.getZ(), radius, true) != null;
    }

    private static void attemptMobSpawn(ServerLevel level, BlockPos center) {
        int spawnAttempts = DEBUG_MODE ? 50 : 10;
        for (int tries = 0; tries < spawnAttempts; tries++) {
            BlockPos spawnPos = findValidSpawnPos(level, center, MAX_SPAWN_RADIUS);
            if (spawnPos == null || !isWithinDistance(center, spawnPos) || isInLiquid(level, spawnPos))
                continue;

            EntityType<? extends Monster> mobType = selectMobTypeWeighted();

            if (!hasEnoughSpace(level, spawnPos, mobType)) continue;

            Monster mob = mobType.create(level);
            if (mob != null && mob.checkSpawnRules(level, MobSpawnType.NATURAL) && mob.checkSpawnObstruction(level)) {

                if (mob instanceof Skeleton) {
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                }

                if (mob instanceof EnderMan && RANDOM.nextInt(10) == 0) { // 10% chance to hold a block
                    ((EnderMan) mob).setCarriedBlock(level.getBlockState(spawnPos.below()));
                }

                if (mob instanceof Zombie) {
                    equipZombieWithRandomGear((Zombie) mob);
                }

                mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, RANDOM.nextFloat() * 360F, 0F);
                level.addFreshEntity(mob);

                if (DEBUG_MODE) {
                    System.out.println("Spawned mob: " + mob.getType().getDescriptionId() + " at " + spawnPos);
                }
            }
        }
    }

    private static void equipZombieWithRandomGear(Zombie zombie) {
        if (RANDOM.nextFloat() < 0.5) { // 50% chance to have an item
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }

        if (RANDOM.nextFloat() < 0.25) { // 25% chance for helmet
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        }

        // Additional logic for other gear slots can be added here
    }

    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center, int radius) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int dx = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dz = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dy = RANDOM.nextInt(91) - 45;

            BlockPos pos = new BlockPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
            BlockState belowBlock = level.getBlockState(pos.below());

            if (isSpawnableSurface(level, belowBlock, pos.below()) &&
                    level.getBrightness(LightLayer.BLOCK, pos) <= MAX_LIGHT_LEVEL) {
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
        return distanceSquared > MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE && distanceSquared <= MAX_SPAWN_RADIUS * MAX_SPAWN_RADIUS;
    }

    private static boolean isInLiquid(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getFluidState().is(FluidTags.WATER) ||
                level.getBlockState(pos).getFluidState().is(FluidTags.LAVA);
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
        return EntityType.ZOMBIE;
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