package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.mc.prefab.gui.components.GuiComponent;

/**
 * Used to get a component to hug the x side of a container
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public class HugXSide extends GuiRelativePos
{
    public final boolean left;

    public HugXSide(GuiComponent component, int xOffset, boolean left)
    {
        super(component, xOffset, 0);
        this.left = left;
    }

    @Override
    public int xi()
    {
        return super.xi() + (left ? 0 : component.getWidth());
    }
}