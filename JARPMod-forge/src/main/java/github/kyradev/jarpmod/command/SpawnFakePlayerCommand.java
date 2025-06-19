package github.kyradev.jarpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.kyradev.jarpmod.FakePlayerSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SpawnFakePlayerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("spawnfakeplayer")
                        .requires(cs -> cs.hasPermission(2)) // solo admin
                        .executes(SpawnFakePlayerCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = player.blockPosition();

        FakePlayerSpawner.spawn(player.serverLevel(), pos);

        source.sendSuccess(() -> Component.literal("Fake player spawned at your position."), true);
        return 1;
    }
}
