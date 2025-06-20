package github.kyradev.jarpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import github.kyradev.jarpmod.FakePlayerSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;

public class RemoveFakePlayerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("removefakeplayer")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(RemoveFakePlayerCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        int removedCount = 0;

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof FakePlayer fakePlayer) {
                if (fakePlayer.getGameProfile().getName().equals("[JustARegularPlayer]")) {
                    fakePlayer.remove(Entity.RemovalReason.DISCARDED);
                    removedCount++;
                }
            }
        }


        final int removedCountFinal = removedCount;

        if (removedCount > 0) {
            source.sendSuccess(() -> Component.literal("Removed " + removedCountFinal + " fake player(s)."), true);
        } else {
            source.sendFailure(Component.literal("No fake player to remove."));
        }
        return 1;
    }
}
