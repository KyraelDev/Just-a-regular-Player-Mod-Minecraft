package github.kyradev.jarpmod;

import com.mojang.logging.LogUtils;
import github.kyradev.jarpmod.command.RemoveFakePlayerCommand;
import github.kyradev.jarpmod.command.SpawnFakePlayerCommand;
import github.kyradev.jarpmod.event.ArmorStandFireCheck;
import github.kyradev.jarpmod.item.CreativeTabInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import github.kyradev.jarpmod.item.ModItems;
import org.slf4j.Logger;

@Mod(JustARegularPlayer.MODID)
@EventBusSubscriber(modid = JustARegularPlayer.MODID)
public class JustARegularPlayer {
    public static final String MODID = "jarpmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public JustARegularPlayer() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        CreativeTabInit.TABS.register(bus);
    }



    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("JustARegularPlayer mod setup");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SpawnFakePlayerCommand.register(event.getDispatcher());
        RemoveFakePlayerCommand.register(event.getDispatcher());
    }
}
