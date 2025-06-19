package github.kyradev.jarpmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class FakePlayerSpawner {
    private static final GameProfile PROFILE = new GameProfile(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "[JustARegularPlayer]"
    );

    private static FakePlayer fakePlayer = null;

    public static void spawn(ServerLevel level, BlockPos pos) {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get(level, PROFILE);
            fakePlayer.setGameMode(GameType.SURVIVAL);
        }

        fakePlayer.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        fakePlayer.setInvisible(false);
        fakePlayer.setSilent(true);

        level.addFreshEntity(fakePlayer);
    }

    public static FakePlayer get() {
        return fakePlayer;
    }
}
