package github.kyradev.jarpmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ArmorStandListGui extends Screen {

    private List<ArmorStand> armorStandList;

    public ArmorStandListGui() {
        super(Component.literal("Elenco Armor Stand"));
    }

    @Override
    protected void init() {
        refresh(); // Carica gli Armor Stand all'inizio
    }

    private void loadArmorStands() {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.armorStandList = StreamSupport.stream(this.minecraft.level.entitiesForRendering().spliterator(), false)
                    .filter(entity -> entity instanceof ArmorStand)
                    .map(entity -> (ArmorStand) entity)
                    .filter(armorStand -> {
                        CompoundTag tag = armorStand.getPersistentData();
                        return tag != null && tag.getBoolean("FakePlayerSpawner");
                    })
                    .collect(Collectors.toList());
        }
    }

    public void refresh() {
        this.clearWidgets(); // Rimuovi i widget esistenti
        loadArmorStands();
        int y = 20;

        if (armorStandList != null && !armorStandList.isEmpty()) {
            for (ArmorStand armorStand : armorStandList) {
                this.addRenderableWidget(
                        Button.builder(Component.literal("Modifica " + armorStand.getId()), button -> {
                                    openEditGui(armorStand);
                                })
                                .bounds(10, y, 150, 20)
                                .build()
                );
                y += 25;
            }
        }
    }

    private void openEditGui(ArmorStand armorStand) {
        Minecraft.getInstance().setScreen(new EditArmorStandGui(armorStand));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        int middleX = this.width / 2;
        int middleY = this.height / 2;

        if (armorStandList == null || armorStandList.isEmpty()) {
            String message = "No FakePlayer spawned yet.";
            int textWidth = this.font.width(message);
            graphics.drawString(this.font, message, middleX - textWidth / 2, middleY - 20, 0xFFFFFF);
        }

        // Mostra sempre il numero di Armor Stands caricati
        String countMessage = "Number of Armor Stands loaded: " + (armorStandList != null ? armorStandList.size() : 0);
        int countTextWidth = this.font.width(countMessage);
        graphics.drawString(this.font, countMessage, middleX - countTextWidth / 2, middleY, 0xFFFFFF);

        if (armorStandList != null && !armorStandList.isEmpty()) {
            super.render(graphics, mouseX, mouseY, partialTicks);
        }
    }
}