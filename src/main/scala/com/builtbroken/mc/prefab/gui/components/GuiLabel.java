package com.builtbroken.mc.prefab.gui.components;

import net.minecraft.client.Minecraft;

/**
 * Version of {@link GuiField} that can not be edited
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/30/2017.
 */
public class GuiLabel extends GuiField
{
    //TODO rewrite to not extend GuiField
    public GuiLabel(int x, int y, String label)
    {
        super(x, y);
        setText(label);
        setEnableBackgroundDrawing(false);
        setEnabled(false);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return false;
    }

    @Override
    protected int getTextColor()
    {
        return 14737632;
    }
}
