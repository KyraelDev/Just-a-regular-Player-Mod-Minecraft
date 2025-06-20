package github.kyradev.jarpmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity; // <-- CORRETTO QUI
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

// Importa la tua classe ModItems per lo SpawnTotem
import github.kyradev.jarpmod.item.ModItems;

@Mod.EventBusSubscriber
public class ArmorStandFireCheck {

    private static final int BURN_DURATION_TICKS = 60; // 3 secondi
    private static final String DROPPED_TAG = "TotemDropped";

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof ArmorStand armorStand && armorStand.getPersistentData().contains("FakePlayerUUID")) {
                    var tag = armorStand.getPersistentData();

                    if (armorStand.isOnFire()) {
                        if (!tag.contains("BurnStartTick")) {
                            tag.putInt("BurnStartTick", (int) level.getGameTime());
                        } else {
                            long burnStart = tag.getInt("BurnStartTick");
                            long elapsed = level.getGameTime() - burnStart;

                            if (elapsed >= BURN_DURATION_TICKS) {

                                // Fai il drop solo una volta
                                if (!tag.getBoolean(DROPPED_TAG)) {
                                    // Drop del totem (SpawnTotemItem)
                                    ItemStack totemStack = new ItemStack(ModItems.SPAWN_TOTEM.get());
                                    ItemEntity drop = new ItemEntity(level, armorStand.getX(), armorStand.getY(), armorStand.getZ(), totemStack);
                                    level.addFreshEntity(drop);
                                    tag.putBoolean(DROPPED_TAG, true);
                                }

                                // Suona il suono della strega
                                level.playSound(null, armorStand.blockPosition(), SoundEvents.WITCH_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F);

                                // Rimuovi il fake player
                                UUID fakePlayerUUID = tag.getUUID("FakePlayerUUID");
                                Entity fakePlayer = level.getEntity(fakePlayerUUID);
                                if (fakePlayer != null && !fakePlayer.isRemoved()) {
                                    fakePlayer.remove(Entity.RemovalReason.KILLED);
                                }

                                // Rimuovi l’armor stand
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
