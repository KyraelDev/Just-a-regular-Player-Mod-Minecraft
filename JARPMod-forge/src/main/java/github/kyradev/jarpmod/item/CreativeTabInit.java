package github.kyradev.jarpmod.item;

import github.kyradev.jarpmod.JustARegularPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JustARegularPlayer.MODID);

    public static final RegistryObject<CreativeModeTab> JARP_TAB = TABS.register("jarpmod_tab",
        () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.jarpmod_tab"))
                .icon(ModItems.SPAWN_TOTEM.get()::getDefaultInstance)
                .displayItems((displayParams, output) -> {
                    output.accept(ModItems.SPAWN_TOTEM.get());
                    output.accept(ModItems.FIRE_STICK.get());
                })
                .build()
    );
}