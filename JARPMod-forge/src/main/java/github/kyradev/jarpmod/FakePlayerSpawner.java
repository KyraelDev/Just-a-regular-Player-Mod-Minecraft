package github.kyradev.jarpmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FakePlayerSpawner {

    private static final String NAME = "[JustARegularPlayer]";

    public static void spawn(ServerLevel level, BlockPos pos) {
        ArmorStand armorStand = new ArmorStand(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        armorStand.setInvisible(false);    // visibile ai player reali
        armorStand.setInvulnerable(false); // vulnerabile
        armorStand.setNoGravity(false);
        armorStand.setCustomName(net.minecraft.network.chat.Component.literal(NAME));
        armorStand.setCustomNameVisible(true);

        // Applico la testa del player custom (come prima)
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        var skinProfile = getProfileWithSkin(level, "Kyrael_");
        applyPlayerProfileToHead(head, skinProfile);
        armorStand.setItemSlot(EquipmentSlot.HEAD, head);

        // Tag NBT per riconoscerlo come FakePlayerSpawner
        CompoundTag tag = armorStand.getPersistentData();
        tag.putBoolean("FakePlayerSpawner", true);

        level.addFreshEntity(armorStand);
    }

    private static GameProfile getProfileWithSkin(ServerLevel level, String playerName) {
        var server = level.getServer();
        if (server == null) return new GameProfile(java.util.UUID.randomUUID(), playerName);

        var sessionService = server.getSessionService();
        var cachedProfile = server.getProfileCache().get(playerName).orElse(new GameProfile(java.util.UUID.randomUUID(), playerName));
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
