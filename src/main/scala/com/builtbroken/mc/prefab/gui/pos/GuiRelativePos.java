package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.mc.prefab.gui.components.GuiComponent;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public abstract class GuiRelativePos extends GuiAbstractPos
{
    public final GuiComponent component;

    public GuiRelativePos(GuiComponent component)
    {
        this.component = component;
    }
}
