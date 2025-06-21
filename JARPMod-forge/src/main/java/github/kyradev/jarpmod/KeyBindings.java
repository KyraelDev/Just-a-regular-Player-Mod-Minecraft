package github.kyradev.jarpmod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import github.kyradev.jarpmod.gui.ArmorStandListGui;

@Mod.EventBusSubscriber(modid = "jarpmod", value = Dist.CLIENT)
public class KeyBindings {
    public static final String CATEGORY = "key.categories.jarpmod";
    public static final KeyMapping openGuiKey = new KeyMapping(
            "key.jarpmod.opengui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openGuiKey);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (openGuiKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new ArmorStandListGui());
        }
    }
}