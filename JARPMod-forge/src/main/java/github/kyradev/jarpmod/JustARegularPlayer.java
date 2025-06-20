package github.kyradev.jarpmod;

import com.mojang.logging.LogUtils;
import github.kyradev.jarpmod.command.RemoveFakePlayerCommand;
import github.kyradev.jarpmod.command.SpawnFakePlayerCommand;
import github.kyradev.jarpmod.event.ArmorStandFireCheck; // importa il listener
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

@Mod(JustARegularPlayer.MODID)
@Mod.EventBusSubscriber(modid = JustARegularPlayer.MODID)
public class JustARegularPlayer {
    public static final String MODID = "jarpmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public JustARegularPlayer() {
        // Registriamo gli eventi di setup della mod
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Registriamo il listener per la morte dell’Armor Stand sull’EventBus di Forge
        MinecraftForge.EVENT_BUS.register(ArmorStandFireCheck.class);
    }

    public void setup(final FMLCommonSetupEvent event) {
        FakePlayerSpawner.register();
        MinecraftForge.EVENT_BUS.register(ArmorStandFireCheck.class); // registra il listener esterno
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SpawnFakePlayerCommand.register(event.getDispatcher());
        RemoveFakePlayerCommand.register(event.getDispatcher());
    }



}
