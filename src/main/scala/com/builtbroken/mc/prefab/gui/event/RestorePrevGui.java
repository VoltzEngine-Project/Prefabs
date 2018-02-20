package com.builtbroken.mc.prefab.gui.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.function.Consumer;

/**
 * Simple consume function for use with restoring GUIs
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/20/2018.
 */
public class RestorePrevGui implements Consumer<GuiScreen>
{
    protected final GuiScreen prev_gui;

    public RestorePrevGui(GuiScreen prev_gui)
    {
        this.prev_gui = prev_gui;
    }

    @Override
    public void accept(GuiScreen guiScreen)
    {
        if (prev_gui != null && !(prev_gui instanceof GuiContainer))
        {
            //TODO build a list of GUIs that fail or shouldn't be opened
            Minecraft.getMinecraft().displayGuiScreen(prev_gui);
        }
    }
}
