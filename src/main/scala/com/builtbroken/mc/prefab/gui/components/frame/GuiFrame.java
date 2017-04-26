package com.builtbroken.mc.prefab.gui.components.frame;

import com.builtbroken.mc.prefab.gui.components.GuiComponentContainer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public class GuiFrame<E extends GuiFrame> extends GuiComponentContainer<E>
{
    public GuiFrame lastOpenedFrame;

    public GuiFrame(int id, int x, int y)
    {
        super(id, x, y);
    }

    public void initGui()
    {
        getComponents().clear();
    }
}
