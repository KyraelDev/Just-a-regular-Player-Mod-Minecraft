package github.kyradev.jarpmod.event;

import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class MobSelector {

    private static final RandomSource RANDOM = RandomSource.create();

    public static EntityType<? extends Mob> selectMobTypeWeighted(ServerLevel level, BlockPos pos) {
        boolean isSlimeChunk = isSlimeChunk(level, pos);
        List<MobEntry> spawnableMobs = getSpawnableMobs(level, isSlimeChunk);
        return selectFromList(spawnableMobs);
    }

    private static List<MobEntry> getSpawnableMobs(Level level, boolean isSlimeChunk) {
        if (level.dimension().equals(Level.NETHER)) {
            return List.of(
                    new MobEntry(EntityType.BLAZE, 100),
                    new MobEntry(EntityType.GHAST, 40),
                    new MobEntry(EntityType.WITHER_SKELETON, 30),
                    new MobEntry(EntityType.SKELETON, 30),
                    new MobEntry(EntityType.MAGMA_CUBE, 50)
            );
        } else if (level.dimension().equals(Level.END)) {
            return List.of(
                    new MobEntry(EntityType.ENDERMAN, 100),
                    new MobEntry(EntityType.ENDERMITE, 10)
            );
        } else { // Overworld
            if (isSlimeChunk) {
                return List.of(
                        new MobEntry(EntityType.SLIME, 100)
                );
            } else {
                return List.of(
                        new MobEntry(EntityType.ZOMBIE, 100),
                        new MobEntry(EntityType.SKELETON, 100),
                        new MobEntry(EntityType.CREEPER, 80),
                        new MobEntry(EntityType.SPIDER, 100),
                        new MobEntry(EntityType.WITCH, 5),
                        new MobEntry(EntityType.ENDERMAN, 10)
                );
            }
        }
    }

    private static boolean isSlimeChunk(ServerLevel level, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        long seed = level.getSeed();
        return (chunkX * 3 + chunkZ * 5 + seed) % 10 == 0;
    }

    private static EntityType<? extends Mob> selectFromList(List<MobEntry> mobs) {
        int totalWeight = mobs.stream().mapToInt(mob -> mob.weight).sum();
        int randomWeight = RANDOM.nextInt(totalWeight);
        int currentWeight = 0;
        for (MobEntry entry : mobs) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return entry.type;
            }
        }
        return EntityType.ZOMBIE;
    }

    public static void applyAttributes(Monster mob) {
        Level level = mob.getCommandSenderWorld();
        Difficulty difficulty = level.getDifficulty();

        if (mob instanceof Zombie) {
            handleZombieAttributes((Zombie) mob, difficulty);
        }

        if (mob instanceof Skeleton) {
            handleSkeletonAttributes((Skeleton) mob, difficulty);
        }

        if (mob instanceof Spider && RANDOM.nextDouble() < 0.1) {
            Skeleton jockey = EntityType.SKELETON.create(level);
            if (jockey != null) {
                jockey.startRiding(mob);
            }
        }
    }

    private static void handleZombieAttributes(Zombie zombie, Difficulty difficulty) {
        if (RANDOM.nextBoolean()) {
            zombie.setBaby(true);
        }
        equipWeapon(zombie, difficulty);
        equipRandomArmor(zombie, difficulty);
    }

    private static void handleSkeletonAttributes(Skeleton skeleton, Difficulty difficulty) {
        skeleton.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        EnchantmentHelper.enchantItem(RANDOM, skeleton.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), 5, false);
        equipRandomArmor(skeleton, difficulty);
    }

    private static void equipWeapon(Monster mob, Difficulty difficulty) {
        if (RANDOM.nextDouble() < getEquipmentChance(difficulty)) {
            ItemStack weapon;
            double chance = RANDOM.nextDouble();
            if (chance < 0.5) {
                weapon = new ItemStack(Items.IRON_SWORD);
            } else if (chance < 0.8) {
                weapon = new ItemStack(Items.GOLDEN_SWORD);
            } else if (difficulty == Difficulty.HARD && chance < 0.9) {
                weapon = new ItemStack(Items.DIAMOND_SWORD);
            } else {
                weapon = new ItemStack(Items.IRON_SHOVEL);
            }
            mob.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weapon);
        }
    }

    private static void equipRandomArmor(Monster mob, Difficulty difficulty) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && RANDOM.nextDouble() < getArmorChance(difficulty)) {
                ItemStack armor = getRandomArmor(slot, difficulty);
                if (RANDOM.nextDouble() < getEnchantmentChance(difficulty)) {
                    EnchantmentHelper.enchantItem(RANDOM, armor, 15, false);
                }
                mob.setItemSlot(slot, armor);
            }
        }
    }

    private static double getEquipmentChance(Difficulty difficulty) {
        switch (difficulty) {
            case HARD: return 0.05;
            case NORMAL: return 0.02;
            case EASY: return 0.01;
            default: return 0;
        }
    }

    private static double getArmorChance(Difficulty difficulty) {
        switch (difficulty) {
            case HARD: return 0.1;
            case NORMAL: return 0.05;
            case EASY: return 0.02;
            default: return 0;
        }
    }

    private static double getEnchantmentChance(Difficulty difficulty) {
        switch (difficulty) {
            case HARD: return 0.05;
            case NORMAL: return 0.02;
            case EASY: return 0.01;
            default: return 0;
        }
    }

    private static ItemStack getRandomArmor(EquipmentSlot slot, Difficulty difficulty) {
        double chance = RANDOM.nextDouble();
        if (difficulty == Difficulty.HARD && chance < 0.1) {
            return getDiamondArmor(slot);
        } else if (chance < 0.2) {
            return getIronArmor(slot);
        } else if (chance < 0.4) {
            return getGoldArmor(slot);
        } else if (chance < 0.6) {
            return getChainmailArmor(slot);
        } else {
            return getLeatherArmor(slot);
        }
    }

    private static ItemStack getLeatherArmor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return new ItemStack(Items.LEATHER_HELMET);
            case CHEST: return new ItemStack(Items.LEATHER_CHESTPLATE);
            case LEGS: return new ItemStack(Items.LEATHER_LEGGINGS);
            case FEET: return new ItemStack(Items.LEATHER_BOOTS);
            default: return ItemStack.EMPTY;
        }
    }

    private static ItemStack getIronArmor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return new ItemStack(Items.IRON_HELMET);
            case CHEST: return new ItemStack(Items.IRON_CHESTPLATE);
            case LEGS: return new ItemStack(Items.IRON_LEGGINGS);
            case FEET: return new ItemStack(Items.IRON_BOOTS);
            default: return ItemStack.EMPTY;
        }
    }

    private static ItemStack getGoldArmor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return new ItemStack(Items.GOLDEN_HELMET);
            case CHEST: return new ItemStack(Items.GOLDEN_CHESTPLATE);
            case LEGS: return new ItemStack(Items.GOLDEN_LEGGINGS);
            case FEET: return new ItemStack(Items.GOLDEN_BOOTS);
            default: return ItemStack.EMPTY;
        }
    }

    private static ItemStack getDiamondArmor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return new ItemStack(Items.DIAMOND_HELMET);
            case CHEST: return new ItemStack(Items.DIAMOND_CHESTPLATE);
            case LEGS: return new ItemStack(Items.DIAMOND_LEGGINGS);
            case FEET: return new ItemStack(Items.DIAMOND_BOOTS);
            default: return ItemStack.EMPTY;
        }
    }

    private static ItemStack getChainmailArmor(EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return new ItemStack(Items.CHAINMAIL_HELMET);
            case CHEST: return new ItemStack(Items.CHAINMAIL_CHESTPLATE);
            case LEGS: return new ItemStack(Items.CHAINMAIL_LEGGINGS);
            case FEET: return new ItemStack(Items.CHAINMAIL_BOOTS);
            default: return ItemStack.EMPTY;
        }
    }

    public static class MobEntry {
        public final EntityType<? extends Mob> type;
        public final int weight;

        public MobEntry(EntityType<? extends Mob> type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }
}