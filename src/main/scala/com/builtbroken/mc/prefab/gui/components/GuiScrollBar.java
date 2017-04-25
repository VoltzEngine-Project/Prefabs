package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.client.helpers.Render2DHelper;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import com.builtbroken.mc.prefab.gui.buttons.GuiButton9px;
import com.builtbroken.mc.prefab.gui.screen.GuiScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/24/2017.
 */
public class GuiScrollBar extends GuiComponentContainer<GuiScrollBar>
{
    //Constants for rendering
    public static final int barU = 16;
    public static final int barV = 0;
    public static final int barWidth = 9;
    public static final int barHeight = 139;

    public static final int sbarU = 26;
    public static final int sbarV = 0;
    public static final int sbarWidth = 7;
    public static final int sbarHeight = 138;

    private int currentScroll = 0;

    private int maxScroll;
    private int middleHeight;
    private int totalSize;

    GuiButton9px upButton;
    GuiButton9px downButton;

    public GuiScrollBar(int id, int x, int y, int height, int maxScroll)
    {
        super(id, x, y, barWidth, height, "");
        this.maxScroll = maxScroll;
        upButton = add(GuiButton9px.newUpButton(0, x, y));
        downButton = add(GuiButton9px.newDownButton(1, x, y + barWidth));
        setHeight(height);
    }

    @Override
    public GuiScrollBar setHeight(int height)
    {
        super.setHeight(Math.max(height, 40 + 18)); //Min size is 40 plus button size
        middleHeight = height - (getTopHeight() + getBotHeight()) - barWidth * 2; //Mid height is equal to height minus size of caps & buttons
        totalSize = getTopHeight() + middleHeight + getBotHeight();
        return this;
    }

    @Override
    public GuiScrollBar setWidth(int w)
    {
        //Right now size change for width is not supported
        //TODO implement size change
        return this;
    }

    public void setMaxScroll(int maxScroll)
    {
        this.maxScroll = Math.max(0, maxScroll);
        updatePositions();
    }

    @Override
    protected void updatePositions()
    {
        super.updatePositions();
        upButton.xPosition = xPosition;
        upButton.yPosition = yPosition;
        downButton.xPosition = xPosition;
        downButton.yPosition = yPosition + height - barWidth;
    }

    @Override
    protected void drawBackground(Minecraft mc, int mouseX, int mouseY)
    {
        //Render background for scroll bar
        Color color = new Color(144, 144, 144);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getGreen() / 255f, 1.0F);
        Render2DHelper.renderWithRepeatVertical(xPosition, yPosition + barWidth, barU, barV, barWidth, barHeight, getTopHeight(), getBotHeight(), middleHeight);

        if (maxScroll > 0)
        {
            //Render scroll bar
            int maxScroll = this.maxScroll + 1;
            float heightP = Math.min(1f, 1f / (float) maxScroll);
            int barHeight = (int) (heightP * totalSize);

            float barPercent = (float) (getCurrentScroll() + 1) / (float) maxScroll;
            int yPos = Math.max((int) (barPercent * this.totalSize) - barHeight + yPosition, yPosition);

            //Set color to red and render scroll bar
            color = new Color(119, 119, 119);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getGreen() / 255f, 1.0F);
            Render2DHelper.renderWithRepeatVertical(xPosition + 1, yPos + 9, sbarU, sbarV, sbarWidth, sbarHeight, 4, 4, barHeight - 8);
        }
    }

    @Override
    public boolean handleMouseInput(Minecraft mc, int mouseX, int mouseY)
    {
        if (isMouseInside(mouseX, mouseY))
        {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0)
            {
                setCurrentScroll(getCurrentScroll() - Math.min(Math.max(scroll, -1), 1));
                setCurrentScroll(Math.max(0, Math.min(maxScroll, getCurrentScroll())));
                if (mc.currentScreen instanceof GuiScreenBase)
                {
                    ((GuiScreenBase) mc.currentScreen).actionPerformed(this);
                }
                else if (mc.currentScreen instanceof GuiContainerBase)
                {
                    ((GuiContainerBase) mc.currentScreen).actionPerformedCallback(this);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getTexture()
    {
        return SharedAssets.GUI_COMPONENTS_BARS;
    }

    public int getTopHeight()
    {
        return 20;
    }

    public int getBotHeight()
    {
        return 20;
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        if (button.id == 0)
        {
            if (getCurrentScroll() > 0)
            {
                setCurrentScroll(getCurrentScroll() - 1);
                downButton.enable();
            }
            if (getCurrentScroll() < 0)
            {
                setCurrentScroll(0);
            }
            if (getCurrentScroll() == 0)
            {
                upButton.disable();
            }
        }
        else if (button.id == 1)
        {
            if (getCurrentScroll() < maxScroll)
            {
                setCurrentScroll(getCurrentScroll() + 1);
                upButton.enable();
            }
            if (getCurrentScroll() > maxScroll)
            {
                setCurrentScroll(maxScroll - 1);
            }
            if (getCurrentScroll() == maxScroll)
            {
                downButton.disable();
            }
        }
    }

    public int getCurrentScroll()
    {
        return currentScroll;
    }

    public void setCurrentScroll(int currentScroll)
    {
        this.currentScroll = currentScroll;
    }
}
