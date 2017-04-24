package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.client.helpers.Render2DHelper;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import com.builtbroken.mc.prefab.gui.buttons.GuiButton9px;
import com.builtbroken.mc.prefab.gui.screen.GuiScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/24/2017.
 */
public class GuiScrollBar extends GuiComponent<GuiScrollBar>
{
    //Constants for rendering
    public static final int barU = 16;
    public static final int barV = 0;
    public static final int barWidth = 9;
    public static final int barHeight = 139;

    public int currentScroll = 0;

    private int maxScroll;
    private int middleHeight;
    private int totalSize;

    GuiButton9px upButton;
    GuiButton9px downButton;

    public GuiScrollBar(int id, int x, int y, int height, int maxScroll)
    {
        super(id, x, y, 9, height, "");
        this.maxScroll = maxScroll;
        upButton = GuiButton9px.newUpButton(0, x, y);
        downButton = GuiButton9px.newDownButton(1, x, y + 9);
        setHeight(height);
    }

    public GuiScrollBar setHeight(int height)
    {
        super.setHeight(height);
        middleHeight = height - (getTopHeight() + getBotHeight()) - 18;
        totalSize = getTopHeight() + middleHeight + getBotHeight();
        updatePositions();
        return this;
    }

    public void setMaxScroll(int maxScroll)
    {
        this.maxScroll = Math.max(0, maxScroll);
    }

    protected void updatePositions()
    {
        upButton.xPosition = xPosition;
        upButton.yPosition = yPosition;
        downButton.xPosition = xPosition;
        downButton.yPosition = yPosition + height - 9;
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        //Render background for scroll bar
        Render2DHelper.renderWithRepeatVertical(xPosition, yPosition + 9, barU, barV, barWidth, barHeight, getTopHeight(), getBotHeight(), middleHeight);

        if (maxScroll > 0)
        {
            //Render scroll bar
            int maxScroll = this.maxScroll + 1;
            float heightP = Math.min(1f, 1f / (float) maxScroll);
            int barHeight = (int) (heightP * totalSize);

            float barPercent = (float) (currentScroll + 1) / (float) maxScroll;
            int yPos = Math.max((int) (barPercent * this.totalSize) - barHeight + yPosition, yPosition);

            //Set color to red and render scroll bar
            float c = 180f / 255f;
            GL11.glColor4f(c, c, c, 1.0F);
            drawTexturedModalRect(xPosition + 1, yPos + 9, barU + 1, barV, barWidth - 2, 2 + barHeight);
        }

        upButton.drawButton(mc, mouseX, mouseY);
        downButton.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean handleMouseInput(Minecraft mc, int mouseX, int mouseY)
    {
        if (isMouseInside(mouseX, mouseY))
        {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0)
            {
                currentScroll -= Math.min(Math.max(scroll, -1), 1);
                currentScroll = Math.max(0, Math.min(maxScroll, currentScroll));
                if(mc.currentScreen instanceof GuiScreenBase)
                {
                    ((GuiScreenBase)mc.currentScreen).actionPerformed(this);
                }
                else  if(mc.currentScreen instanceof GuiContainerBase)
                {
                    ((GuiContainerBase)mc.currentScreen).actionPerformedCallback(this);
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
    public void mouseReleased(int mouseX, int mouseY)
    {
        super.mouseReleased(mouseX, mouseY);
        upButton.mouseReleased(mouseX, mouseY);
        downButton.mouseReleased(mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            if (upButton.mousePressed(mc, mouseX, mouseY))
            {
                if (currentScroll > 0)
                {
                    currentScroll--;
                    downButton.enable();
                    return true; //Trigger button press event
                }
                if (currentScroll < 0)
                {
                    currentScroll = 0;
                    return true; //Trigger button press event
                }
                if (currentScroll == 0)
                {
                    upButton.disable();
                }
            }
            else if (downButton.mousePressed(mc, mouseX, mouseY))
            {
                if (currentScroll < maxScroll)
                {
                    currentScroll++;
                    upButton.enable();
                    return true; //Trigger button press event
                }
                if (currentScroll > maxScroll)
                {
                    currentScroll = maxScroll - 1;
                    return true; //Trigger button press event
                }
                if (currentScroll == maxScroll)
                {
                    downButton.disable();
                }
            }
        }
        return false;
    }

    @Override
    public void func_146111_b(int mouseX, int mouseY)
    {
        super.func_146111_b(mouseX, mouseY);
        upButton.func_146111_b(mouseX, mouseY);
        downButton.func_146111_b(mouseX, mouseY);
    }

    @Override
    public void func_146113_a(SoundHandler handler)
    {
        upButton.func_146113_a(handler);
        downButton.func_146113_a(handler);
    }
}
