package github.kyradev.jarpmod.item;

import github.kyradev.jarpmod.JustARegularPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.rmi.registry.Registry;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JustARegularPlayer.MODID);

    public static final RegistryObject<Item> FIRE_STICK = ITEMS.register("fire_stick",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
            ));
    public static final RegistryObject<Item> SPAWN_TOTEM = ITEMS.register("spawn_totem",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
            ));
}
