package github.kyradev.jarpmod.event;

import github.kyradev.jarpmod.gui.ArmorStandListGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = "jarpmod", bus = Bus.FORGE, value = Dist.CLIENT)
public class GuiEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide) return;

        // Messaggio di debug iniziale
        event.getEntity().displayClientMessage(Component.literal("Evento PlayerInteractEntity rilevato"), false);

        if (event.getTarget() instanceof ArmorStand armorStand) {
            event.getEntity().displayClientMessage(Component.literal("Bersaglio è un ArmorStand"), false);

            CompoundTag tag = armorStand.getPersistentData();
            if (tag != null && tag.getBoolean("FakePlayerSpawner")) {
                event.getEntity().displayClientMessage(Component.literal("ArmorStand ha il tag FakePlayerSpawner"), false);

                if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity().getMainHandItem().isEmpty()) {
                    event.getEntity().displayClientMessage(Component.literal("Aprendo GUI"), false);
                    // Apri la GUI senza parametri
                    Minecraft.getInstance().setScreen(new ArmorStandListGui());
                } else {
                    event.getEntity().displayClientMessage(Component.literal("Devi avere la mano libera"), false);
                }
            } else {
                event.getEntity().displayClientMessage(Component.literal("ArmorStand non ha tag corretto"), false);
            }
        } else {
            event.getEntity().displayClientMessage(Component.literal("Bersaglio non è un ArmorStand"), false);
        }
    }
}