package zabi.minecraft.nbttooltip.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import zabi.minecraft.nbttooltip.NBTTooltip;

public class ModGuiConfig extends GuiConfig {
    public ModGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(ModConfig.config.getCategory(ModConfig.CATEGORY)).getChildElements(), NBTTooltip.MOD_ID, false, false, NBTTooltip.MOD_NAME);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ModConfig.load();
    }
}