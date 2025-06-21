package github.kyradev.jarpmod.item;

import github.kyradev.jarpmod.FakePlayerSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SpawnTotemItem extends Item {

    public SpawnTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide()) {
            // Feedback client-side
            if (player != null) {
                player.displayClientMessage(Component.literal("You clicked the totem (client side)"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            // Solo lato server
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        if (player instanceof ServerPlayer serverPlayer) {
            // Usa il metodo spawn aggiornato
            FakePlayerSpawner.spawn(serverLevel, pos, serverPlayer);

            if (!serverPlayer.isCreative()) {
                context.getItemInHand().shrink(1);
            }

            serverPlayer.displayClientMessage(Component.literal("Fake player Armor Stand spawned!"), false);
        }

        return InteractionResult.CONSUME;
    }
}