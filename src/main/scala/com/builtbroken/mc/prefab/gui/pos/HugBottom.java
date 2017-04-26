package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.mc.prefab.gui.components.GuiComponent;

/**
 * Used to get a component to hug the x side of a container and the bottom at the same time
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public class HugBottom extends HugXSide
{
    public final GuiComponent component;

    public HugBottom(GuiComponent component, int xOffset, int yOffset, boolean left)
    {
        super(component, xOffset, left);
        this.component = component;
        this.setYOffset(yOffset);
    }

    @Override
    public int yi()
    {
        return super.yi() + component.getHeight();
    }
}