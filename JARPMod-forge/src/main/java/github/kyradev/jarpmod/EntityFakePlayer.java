package github.kyradev.jarpmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;

public class EntityFakePlayer extends FakePlayer {

    public EntityFakePlayer(ServerLevel world, GameProfile profile) {
        super(world, profile);
        this.setInvisible(true); // Di default invisibile, puoi cambiare
        this.setInvulnerable(true);
        this.noPhysics = true; // disabilita la fisica
    }

    // Puoi aggiungere metodi custom qui se ti serve
}
