package github.kyradev.jarpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import github.kyradev.jarpmod.FakePlayerSpawner;

public class SpawnFakePlayerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("spawnfakeplayer")
                        .requires(cs -> cs.hasPermission(2)) // livello permesso 2 = admin
                        .executes(SpawnFakePlayerCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Vec3 pos = player.position();

        ServerLevel level = (ServerLevel) player.level();

        FakePlayerSpawner.spawn(level, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), player);

        source.sendSuccess(() -> Component.literal("Fake player spawned at your position."), true);
        return 1;
    }
}