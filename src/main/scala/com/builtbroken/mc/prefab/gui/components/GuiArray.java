package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.prefab.gui.pos.GuiRelativePos;
import com.builtbroken.mc.prefab.gui.pos.HugXSide;
import com.builtbroken.mc.prefab.gui.pos.size.GuiRelativeSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public class GuiArray extends GuiComponentContainer<GuiArray>
{
    private int entriesShown = 0;

    private GuiScrollBar scrollBar;
    private GuiComponent[] buttonEntries;

    private CallbackGuiArray callback;

    private int entryCount = -1;
    private int ySpacing = 20;

    public GuiArray(CallbackGuiArray callback, int id, int x, int y, int entries)
    {
        this(callback, id, x, y, entries, 20);
    }

    public GuiArray(CallbackGuiArray callback, int id, int x, int y, int entries, int ySpacing)
    {
        super(id, x, y, 200, entries * ySpacing, "");
        this.resizeAsNeeded = false;
        this.ySpacing = ySpacing;
        this.callback = callback;
        scrollBar = add(new GuiScrollBar(0, new HugXSide(this, -9, false), getHeight(), entries));
        scrollBar.setRelativeSize(new GuiRelativeSize(this, 9, 0).setUseHostWidth(false));
        setEntriesShown(entries);
    }

    @Override
    protected Color getBackgroundColor()
    {
        return enableDebug ? Color.MAGENTA : null;
    }

    @Override
    protected void update(Minecraft mc, int mouseX, int mouseY)
    {
        super.update(mc, mouseX, mouseY);
        if (entryCount != callback.getSize())
        {
            entryCount = callback.getSize();
            reloadEntries();
            scrollBar.setMaxScroll(callback.getSize() - entriesShown);
        }
        if (entriesShown > entryCount)
        {
            scrollBar.downButton.disable();
        }
    }

    public void reloadEntries()
    {
        //Create buttons if empty
        if (buttonEntries == null)
        {
            buttonEntries = new GuiComponent[entriesShown];
            for (int i = 0; i < buttonEntries.length; i++)
            {
                buttonEntries[i] = add(callback.newEntry(i, i + 10, x, y));
                //Set relative position so it auto updates
                buttonEntries[i].setRelativePosition(new GuiRelativePos(this, 0, i * getEntryYSpacing()));
            }
        }

        //Update buttons
        for (int i = 0; i < buttonEntries.length; i++)
        {
            buttonEntries[i].hide();
            int index = i + scrollBar.getCurrentScroll();
            if (index < entryCount)
            {
                buttonEntries[i].show();
                buttonEntries[i].setEnabled(callback.isEnabled(index));
                callback.updateEntry(index, buttonEntries[i]);
            }
        }
        updatePositions();
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        int id = button.id;
        if (id == 0)
        {
            reloadEntries();
        }
        else if (id >= 10 && id < 10 + entriesShown)
        {
            int index = id - 10 + scrollBar.getCurrentScroll();
            callback.onPressed(index);
            reloadEntries();
        }
    }

    protected int getEntryYSpacing()
    {
        return ySpacing;
    }

    public void setEntriesShown(int entries)
    {
        if (entriesShown != entries)
        {
            if (buttonEntries != null)
            {
                updatePositionLogic = false;
                for (GuiComponent button : buttonEntries)
                {
                    remove(button);
                }
                updatePositionLogic = true;
                buttonEntries = null;
            }
        }
        this.entriesShown = entries;
        reloadEntries();
    }
}
