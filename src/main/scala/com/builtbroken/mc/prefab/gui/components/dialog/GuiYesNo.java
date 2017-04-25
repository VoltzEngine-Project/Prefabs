package com.builtbroken.mc.prefab.gui.components.dialog;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public class GuiYesNo extends GuiDialog<GuiYesNo>
{
    public String message;

    public GuiYesNo(int id, int x, int y, String title, String message)
    {
        super(id, x, y);
        this.displayString = title;
        this.message = message;
    }
}
