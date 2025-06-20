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
                player.displayClientMessage(Component.literal("Hai cliccato il totem (client side)"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            // Solo lato server
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        // Usare la logica consolidata per spawnare fake player + armor stand
        FakePlayerSpawner.spawn(serverLevel, pos);

        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
        }

        if (player != null) {
            player.displayClientMessage(Component.literal("Fake player and Armor Stand spawned!"), false);
        }

        return InteractionResult.CONSUME;
    }
}
