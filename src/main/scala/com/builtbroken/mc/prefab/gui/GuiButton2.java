package com.builtbroken.mc.prefab.gui;

import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * Wrapper/Utility version of the default MC button
 * Is also used a prefab for buttons with more features
 * <p>
 * Created by robert on 4/23/2015.
 * TODO need a class name
 */
public class GuiButton2<E extends GuiButton2> extends GuiComponent<E>
{
    public static final Point DEFAULT_SIZE = new Point(200, 20);

    public GuiButton2(int id, Point point, String key)
    {
        this(id, point, DEFAULT_SIZE, key);
    }

    public GuiButton2(int id, int x, int y, String key)
    {
        this(id, x, y, DEFAULT_SIZE.xi(), DEFAULT_SIZE.yi(), key);
    }

    public GuiButton2(int id, Point point, Point size, String key)
    {
        super(id, point.xi(), point.yi(), size.xi(), size.yi(), key);
    }

    public GuiButton2(int id, int x, int y, int width, int height, String key)
    {
        super(id, x, y, width, height, key);
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        int hoverState = this.getHoverState(this.field_146123_n);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
        this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);

        int color = 14737632;

        if (packedFGColour != 0)
        {
            color = packedFGColour;
        }
        else if (!this.enabled)
        {
            color = 10526880;
        }
        else if (this.field_146123_n)
        {
            color = 16777120;
        }
        this.drawCenteredString(mc.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);

        if (enableDebug)
        {
            this.drawString(mc.fontRenderer, "" + id, this.xPosition, this.yPosition, Color.red.getRGB());
        }
    }

    @Override
    public ResourceLocation getTexture()
    {
        return buttonTextures;
    }
}
