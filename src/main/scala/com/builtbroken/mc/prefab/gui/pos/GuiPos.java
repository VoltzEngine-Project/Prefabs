package com.builtbroken.mc.prefab.gui.pos;

import com.builtbroken.mc.imp.transform.vector.AbstractPos;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/26/2017.
 */
public abstract class GuiPos extends AbstractPos
{
    public final int x;
    public final int y;

    public GuiPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public int xi()
    {
        return x;
    }

    @Override
    public int yi()
    {
        return y;
    }
}
