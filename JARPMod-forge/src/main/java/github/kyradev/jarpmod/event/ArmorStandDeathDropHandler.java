package github.kyradev.jarpmod.event;

import github.kyradev.jarpmod.item.ModItems;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ArmorStandDeathDropHandler {

    @SubscribeEvent
    public static void onArmorStandDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        Level level = armorStand.level();
        if (level.isClientSide()) return;

        if (armorStand.getPersistentData().hasUUID("FakePlayerUUID")) {
            // Controlla che il drop non sia già stato fatto
            if (!armorStand.getPersistentData().getBoolean("HasDroppedTotem")) {
                ItemStack totemStack = new ItemStack(ModItems.SPAWN_TOTEM.get());

                ItemEntity itemEntity = new ItemEntity(level,
                        armorStand.getX(),
                        armorStand.getY(),
                        armorStand.getZ(),
                        totemStack);
                level.addFreshEntity(itemEntity);

                armorStand.getPersistentData().putBoolean("HasDroppedTotem", true);
            }
        }
    }
}
