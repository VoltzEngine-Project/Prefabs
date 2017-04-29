package com.builtbroken.mc.prefab.gui.pos.size;

import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import com.builtbroken.mc.prefab.gui.pos.GuiAbstractPos;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/29/2017.
 */
public class GuiRelativeSize extends GuiAbstractPos
{
    public final GuiComponent host;

    public int width;
    public int height;

    private boolean useHostWidth = true;
    private boolean useHostHeight = true;

    public GuiRelativeSize(GuiComponent host, int width, int height)
    {
        this.host = host;
        this.width = width;
        this.height = height;
    }

    @Override
    public int xi()
    {
        return (useHostWidth() ? host.getWidth() : 0) + width;
    }

    @Override
    public int yi()
    {
        return (useHostHeight() ? host.getHeight() : 0) + height;
    }

    public boolean useHostWidth()
    {
        return useHostWidth;
    }

    public GuiRelativeSize setUseHostWidth(boolean useHostWidth)
    {
        this.useHostWidth = useHostWidth;
        return this;
    }

    public boolean useHostHeight()
    {
        return useHostHeight;
    }

    public GuiRelativeSize setUseHostHeight(boolean useHostHeight)
    {
        this.useHostHeight = useHostHeight;
        return this;
    }
}
