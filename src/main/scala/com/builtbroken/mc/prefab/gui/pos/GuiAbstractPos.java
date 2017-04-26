package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.jlib.data.vector.IPos2D;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public abstract class GuiAbstractPos implements IPos2D
{
    @Override
    public final double x()
    {
        return xi();
    }

    @Override
    public final double y()
    {
        return yi();
    }

    public abstract int xi();

    public abstract int yi();
}
