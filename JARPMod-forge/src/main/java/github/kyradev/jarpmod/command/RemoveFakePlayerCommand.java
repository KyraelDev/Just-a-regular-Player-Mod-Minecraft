package github.kyradev.jarpmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import github.kyradev.jarpmod.FakePlayerSpawner; // Assicurati che l'import sia corretto

public class RemoveFakePlayerCommand {

    private static final String TAG_FAKE_PLAYER_SPAWNER = "FakePlayerSpawner";

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
            if (entity instanceof ArmorStand armorStand) {
                if (armorStand.getPersistentData().getBoolean(TAG_FAKE_PLAYER_SPAWNER)) {
                    armorStand.remove(Entity.RemovalReason.DISCARDED);
                    removedCount++;
                }
            }
        }

        // Reset the spawnerCounter to zero
        FakePlayerSpawner.resetCounter();

        final int removedCountFinal = removedCount;

        if (removedCount > 0) {
            source.sendSuccess(() -> Component.literal("Removed " + removedCountFinal + " fake player(s)."), true);
        } else {
            source.sendFailure(Component.literal("No fake player to remove."));
        }

        return 1;
    }
}