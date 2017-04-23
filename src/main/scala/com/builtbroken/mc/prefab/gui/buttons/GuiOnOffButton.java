package com.builtbroken.mc.prefab.gui.buttons;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 8/7/2016.
 */
public class GuiOnOffButton extends GuiImageButton
{
    public GuiOnOffButton(int id, int x, int y, boolean on)
    {
        super(id, x, y, 9, 9, 54, 180 + (!on ? 9 : 0));
    }

    @Override
    public boolean supportsDisabledState()
    {
        return true;
    }
}
