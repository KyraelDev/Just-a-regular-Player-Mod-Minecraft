package github.kyradev.jarpmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class FakePlayerSpawner {

    private static final String NAME = "[JustARegularPlayer]";
    private static int nextId = 0;

    public static GameProfile createUniqueProfile() {
        int id = nextId;
        nextId = (nextId + 1) % 100;

        UUID baseUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        long mostSigBits = baseUUID.getMostSignificantBits();
        long leastSigBits = (baseUUID.getLeastSignificantBits() & 0xFFFFFFFFFFFFFF00L) | id;

        return new GameProfile(new UUID(mostSigBits, leastSigBits), NAME);
    }

    public static void spawn(ServerLevel level, BlockPos pos) {
        GameProfile fakePlayerProfile = createUniqueProfile();
        FakePlayer fakePlayer = FakePlayerFactory.get(level, fakePlayerProfile);

        fakePlayer.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        fakePlayer.setGameMode(GameType.SURVIVAL);
        fakePlayer.setInvisible(false);
        fakePlayer.setSilent(true);
        fakePlayer.setCustomName(Component.literal(NAME));
        fakePlayer.setCustomNameVisible(true);
        fakePlayer.setNoGravity(false);
        level.addFreshEntity(fakePlayer);

        ArmorStand armorStand = new ArmorStand(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        armorStand.setInvisible(false);
        armorStand.setInvulnerable(false); // Rende vulnerabile
        armorStand.setNoGravity(false);

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile skinProfile = getProfileWithSkin(level, "Kyrael_");
        applyPlayerProfileToHead(head, skinProfile);
        armorStand.setItemSlot(EquipmentSlot.HEAD, head);

        // Salva l'UUID del fake player nei dati persistenti dell'armor stand
        CompoundTag tag = armorStand.getPersistentData();
        tag.putUUID("FakePlayerUUID", fakePlayer.getUUID());

        level.addFreshEntity(armorStand);
    }

    private static GameProfile getProfileWithSkin(ServerLevel level, String playerName) {
        var server = level.getServer();
        if (server == null) return new GameProfile(UUID.randomUUID(), playerName);

        var sessionService = server.getSessionService();
        var cachedProfile = server.getProfileCache().get(playerName).orElse(new GameProfile(UUID.randomUUID(), playerName));
        if (!cachedProfile.getProperties().containsKey("textures")) {
            return sessionService.fillProfileProperties(cachedProfile, true);
        }
        return cachedProfile;
    }

    private static void applyPlayerProfileToHead(ItemStack head, GameProfile profile) {
        CompoundTag tag = head.getOrCreateTag();
        CompoundTag skullOwner = new CompoundTag();

        skullOwner.putString("Name", profile.getName() != null ? profile.getName() : "");
        skullOwner.putUUID("Id", profile.getId());

        if (profile.getProperties().containsKey("textures")) {
            ListTag textures = new ListTag();
            profile.getProperties().get("textures").forEach(prop -> {
                CompoundTag texture = new CompoundTag();
                texture.putString("Value", prop.getValue());
                if (prop.getSignature() != null) {
                    texture.putString("Signature", prop.getSignature());
                }
                textures.add(texture);
            });

            CompoundTag propertiesTag = new CompoundTag();
            propertiesTag.put("textures", textures);
            skullOwner.put("Properties", propertiesTag);
        }

        tag.put("SkullOwner", skullOwner);
        head.setTag(tag);
    }
}
