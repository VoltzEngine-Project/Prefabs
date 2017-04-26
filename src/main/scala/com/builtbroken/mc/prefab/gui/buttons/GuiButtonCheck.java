package com.builtbroken.mc.prefab.gui.buttons;

/**
 * Simple check box button used for boolean selection in settings GUIs
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/24/2017.
 */
public class GuiButtonCheck extends GuiButton9px<GuiButtonCheck>
{
    protected boolean checked;
    protected int type;

    public GuiButtonCheck(int id, int x, int y, int type, boolean checked)
    {
        super(id, x, y, 10, 0);
        this.type = type;
        this.checked = checked;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public GuiButtonCheck check()
    {
        this.checked = true;
        return this;
    }

    public GuiButtonCheck uncheck()
    {
        this.checked = false;
        return this;
    }

    public GuiButtonCheck setChecked(boolean b)
    {
        this.checked = b;
        return this;
    }

    @Override
    protected int getVRenderModifier()
    {
        if (checked)
        {
            if (type == 1)
            {
                return height * 2; //Solid check box
            }
            return height; //Classic check symbol with no box background
        }
        return 0;
    }
}
