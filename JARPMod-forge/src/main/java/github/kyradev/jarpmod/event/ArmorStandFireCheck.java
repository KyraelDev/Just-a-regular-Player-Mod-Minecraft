package github.kyradev.jarpmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import github.kyradev.jarpmod.FakePlayerSpawner; // Importa la tua classe

import github.kyradev.jarpmod.item.ModItems;

@Mod.EventBusSubscriber
public class ArmorStandFireCheck {

    private static final int BURN_DURATION_TICKS = 60;
    private static final String DROPPED_TAG = "TotemDropped";
    private static final String FAKEPLAYER_TAG = "FakePlayerSpawner";

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof ArmorStand armorStand && armorStand.getPersistentData().contains(FAKEPLAYER_TAG)) {
                    var tag = armorStand.getPersistentData();

                    if (armorStand.isOnFire()) {
                        if (!tag.contains("BurnStartTick")) {
                            tag.putInt("BurnStartTick", (int) level.getGameTime());
                        } else {
                            long burnStart = tag.getInt("BurnStartTick");
                            long elapsed = level.getGameTime() - burnStart;

                            if (elapsed >= BURN_DURATION_TICKS) {

                                if (!tag.getBoolean(DROPPED_TAG)) {
                                    ItemStack totemStack = new ItemStack(ModItems.SPAWN_TOTEM.get());
                                    ItemEntity drop = new ItemEntity(level, armorStand.getX(), armorStand.getY(), armorStand.getZ(), totemStack);
                                    level.addFreshEntity(drop);
                                    tag.putBoolean(DROPPED_TAG, true);
                                }

                                level.playSound(null, armorStand.blockPosition(), SoundEvents.WITCH_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F);

                                // Rimuovi l’armor stand e decrementa il contatore
                                FakePlayerSpawner.handleRemoval(armorStand);
                                armorStand.remove(Entity.RemovalReason.KILLED);
                            }
                        }
                    } else {
                        tag.remove("BurnStartTick");
                        tag.remove(DROPPED_TAG);
                    }
                }
            }
        }
    }
}