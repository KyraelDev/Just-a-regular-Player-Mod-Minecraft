package github.kyradev.jarpmod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ArmorStandFireCheck {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isClientSide()) return;

        Level world = event.world;

        // Cerca tutti gli ArmorStand nel mondo
        List<ArmorStand> armorStands = world.getEntitiesOfClass(ArmorStand.class, entity -> entity.isOnFire());

        for (ArmorStand armorStand : armorStands) {
            if (!armorStand.isAlive()) continue;
            if (!armorStand.getTags().contains("FakePlayerLinked")) continue;

            // Invia messaggio per debug
            armorStand.sendSystemMessage(Component.literal("§c[DEBUG] ArmorStand è in fiamme!"));

            // Recupera l’UUID dal tag NBT o tag Forge
            UUID uuid = null;
            try {
                uuid = UUID.fromString(armorStand.getPersistentData().getString("FakePlayerUUID"));
            } catch (Exception ignored) {}

            if (uuid == null) continue;

            // Rimuovi il fake player se ancora presente
            Entity maybeFake = ((ServerLevel) world).getEntity(uuid);
            if (maybeFake != null) {
                maybeFake.remove(Entity.RemovalReason.DISCARDED);
            }

            // Rimuovi anche l’armor stand stesso
            armorStand.discard(); // oppure armorStand.kill(); se vuoi ucciderlo

            // (facoltativo) rimuovi il tag per evitare chiamate multiple
            armorStand.getTags().remove("FakePlayerLinked");
        }
    }
}
