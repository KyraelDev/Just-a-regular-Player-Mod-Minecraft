package github.kyradev.jarpmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class EditArmorStandGui extends Screen {
    private final ArmorStand armorStand;
    private EditBox nameEditBox;

    public EditArmorStandGui(ArmorStand armorStand) {
        super(Component.literal("Modifica Armor Stand"));
        this.armorStand = armorStand;
    }

    @Override
    protected void init() {
        this.nameEditBox = new EditBox(this.font, this.width / 2 - 100, 40, 200, 20, Component.literal("Nome"));
        this.nameEditBox.setValue(armorStand.getCustomName() != null ? armorStand.getCustomName().getString() : "");
        this.addRenderableWidget(nameEditBox);

        this.addRenderableWidget(
                Button.builder(Component.literal("Salva"), button -> {
                            if (!nameEditBox.getValue().isEmpty()) {
                                armorStand.setCustomName(Component.literal(nameEditBox.getValue()));
                                // Aggiungi qui il codice per inviare i pacchetti al server, se necessario
                            }
                            Minecraft.getInstance().setScreen(null);
                        })
                        .bounds(this.width / 2 - 100, 100, 200, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Annulla"), button -> {
                            Minecraft.getInstance().setScreen(null);
                        })
                        .bounds(this.width / 2 - 100, 130, 200, 20)
                        .build()
        );
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderDirtBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(this.font, "Modifica Armor Stand", this.width / 2 - 100, 20, 0xFFFFFF, false);
        this.nameEditBox.render(graphics, mouseX, mouseY, partialTicks);
    }
}