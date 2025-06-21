package github.kyradev.jarpmod;

import com.mojang.logging.LogUtils;
import github.kyradev.jarpmod.command.RemoveFakePlayerCommand;
import github.kyradev.jarpmod.command.SpawnFakePlayerCommand;
import github.kyradev.jarpmod.event.ArmorStandFireCheck;
import github.kyradev.jarpmod.item.CreativeTabInit;
import github.kyradev.jarpmod.item.ModItems;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JustARegularPlayer.MODID)
@EventBusSubscriber(modid = JustARegularPlayer.MODID)
public class JustARegularPlayer {
    public static final String MODID = "jarpmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public JustARegularPlayer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        ModItems.ITEMS.register(modEventBus);
        CreativeTabInit.TABS.register(modEventBus);

        // Registrazione del listener dell’evento ArmorStandFireCheck sul Forge event bus
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ArmorStandFireCheck.class);
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
