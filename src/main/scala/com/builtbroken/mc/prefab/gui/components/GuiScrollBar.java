package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.client.helpers.Render2DHelper;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import com.builtbroken.mc.prefab.gui.buttons.GuiButton9px;
import com.builtbroken.mc.prefab.gui.pos.HugBottom;
import com.builtbroken.mc.prefab.gui.pos.HugXSide;
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

    //Current scroll index
    private int currentScroll = 0;

    //Max scroll index
    private int maxScroll;
    //Size in pixels of mid section of rendered bar
    private int middleHeight;
    //Total size in pixels of rendered bar
    private int totalSize;

    //Buttons
    protected GuiButton9px upButton;
    protected GuiButton9px downButton;

    public GuiScrollBar(int id, IPos2D point, int height, int maxScroll)
    {
        super(id, point);
        this.width = 9;
        this.height = height;
        this.maxScroll = maxScroll;
        addArrows();
        setHeight(height);
    }

    public GuiScrollBar(int id, int x, int y, int height, int maxScroll)
    {
        super(id, x, y, barWidth, height, "");
        this.maxScroll = maxScroll;
        addArrows();
        setHeight(height);
    }

    protected void addArrows()
    {
        upButton = (GuiButton9px) add(GuiButton9px.newUpButton(0, 0, 0).setRelativePosition(new HugXSide(this, 0, true))).disable();
        downButton = (GuiButton9px) add(GuiButton9px.newDownButton(1, 0, 0).setRelativePosition(new HugBottom(this, 0, -GuiButton9px.SIZE, true)));
    }

    @Override
    public GuiScrollBar setHeight(int height)
    {
        super.setHeight(Math.max(height, 40 + GuiButton9px.SIZE * 2)); //Min size is 40 plus button size
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
            //Calculate slider render size
            int maxScroll = this.maxScroll + 1;
            float heightP = Math.min(1f, 1f / (float) maxScroll);
            int barHeight = (int) (heightP * totalSize);

            //Calculate slider render position
            float barPercent = (float) (getCurrentScroll() + 1) / (float) maxScroll;
            int sliderRenderY = Math.max((int) (barPercent * this.totalSize) - barHeight + yPosition, yPosition);

            //Set color to red and render scroll bar
            color = new Color(119, 119, 119);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getGreen() / 255f, 1.0F);

            //Render slider
            Render2DHelper.renderWithRepeatVertical(xPosition + 1, sliderRenderY + GuiButton9px.SIZE, sbarU, sbarV, sbarWidth, sbarHeight, 4, 4, barHeight - 8);
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
