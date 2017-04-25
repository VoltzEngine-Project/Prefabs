package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.prefab.gui.GuiButton2;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public abstract class CallbackGuiArray
{
    public String getEntryName(int index)
    {
        return "Entry[" + index + "]";
    }

    public boolean isEnabled(int index)
    {
        return true;
    }

    public void onPressed(int index)
    {

    }

    protected GuiComponent newEntry(int index, int buttonID, int x, int y)
    {
        return new GuiButton2(buttonID, x, y, getEntryWidth(), getEntryHeight(), "Entry[" + index + "]");
    }

    protected int getEntryWidth()
    {
        return 100;
    }

    protected int getEntryHeight()
    {
        return 20;
    }

    public abstract int getSize();

    public void updateEntry(int index, GuiComponent buttonEntry)
    {
        buttonEntry.displayString = getEntryName(index);
    }
}
