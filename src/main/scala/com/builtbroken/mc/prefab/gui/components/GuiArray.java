package com.builtbroken.mc.prefab.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

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
    private int ySpaceing = 20;

    public GuiArray(CallbackGuiArray callback, int id, int x, int y, int entries)
    {
        this(callback, id, x, y, entries, 20);
    }

    public GuiArray(CallbackGuiArray callback, int id, int x, int y, int entries, int ySpacing)
    {
        super(id, x, y, 200, entries * ySpacing, "");
        this.ySpaceing = ySpacing;
        this.callback = callback;
        //TODO implement left bar position
        scrollBar = add(new GuiScrollBar(0, x, y, height, entries));
        setEntriesShown(entries);
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
    }

    @Override
    protected void updatePositions()
    {
        scrollBar.xPosition = xPosition + width - GuiScrollBar.barWidth;
        scrollBar.yPosition = yPosition;
        scrollBar.setHeight(height);

        int i = 0;
        for (GuiComponent component : buttonEntries)
        {
            component.xPosition = xPosition;
            component.yPosition = yPosition + (i++ * ySpaceing);
        }
        super.updatePositions();
    }

    public void reloadEntries()
    {
        if (buttonEntries == null)
        {
            buttonEntries = new GuiComponent[entriesShown];
            for (int i = 0; i < buttonEntries.length; i++)
            {
                buttonEntries[i] = add(callback.newEntry(i, i + 10, xPosition, yPosition));
            }
        }

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
        return ySpaceing;
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
