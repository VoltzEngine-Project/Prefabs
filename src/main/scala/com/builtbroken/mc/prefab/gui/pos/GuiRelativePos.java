package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.mc.prefab.gui.components.GuiComponent;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public class GuiRelativePos extends GuiAbstractPos
{
    public final GuiComponent component;
    private int yOffset;
    private int xOffset;

    public GuiRelativePos(GuiComponent component)
    {
        this.component = component;
    }

    public GuiRelativePos(GuiComponent component, int x, int y)
    {
        this.component = component;
        this.setXOffset(x);
        this.setYOffset(y);
    }

    @Override
    public int xi()
    {
        return getXOffset();
    }


    @Override
    public int yi()
    {
        return getYOffset();
    }

    public int getYOffset()
    {
        return yOffset;
    }

    public GuiRelativePos setYOffset(int yOffset)
    {
        this.yOffset = yOffset;
        return this;
    }

    public int getXOffset()
    {
        return xOffset;
    }

    public GuiRelativePos setXOffset(int xOffset)
    {
        this.xOffset = xOffset;
        return this;
    }
}
