package github.kyradev.jarpmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class ArmorStandFireCheck {

    private static final int BURN_DURATION_TICKS = 60; // 3 secondi

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
                                UUID fakePlayerUUID = tag.getUUID("FakePlayerUUID");
                                Entity fakePlayer = level.getEntity(fakePlayerUUID);

                                // 🎵 Riproduce il suono della morte della strega
                                level.playSound(null, armorStand.blockPosition(), SoundEvents.WITCH_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F);

                                if (fakePlayer != null && !fakePlayer.isRemoved()) {
                                    fakePlayer.remove(Entity.RemovalReason.KILLED);
                                }

                                armorStand.remove(Entity.RemovalReason.KILLED);
                            }
                        }
                    } else {
                        tag.remove("BurnStartTick");
                    }
                }
            }
        }
    }
}
