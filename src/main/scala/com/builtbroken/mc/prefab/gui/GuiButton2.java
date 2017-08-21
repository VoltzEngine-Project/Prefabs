package com.builtbroken.mc.prefab.gui;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

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

    public GuiButton2(int id, IPos2D point, String key)
    {
        super(id, point);
        this.displayString = key;
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
        int hoverState = this.getHoverState(this.hovered);
        this.drawTexturedModalRect(this.x(), this.y(), 0, 46 + hoverState * 20, this.getWidth() / 2, this.getHeight());
        this.drawTexturedModalRect(this.x() + this.getWidth() / 2, this.y(), 200 - this.getWidth() / 2, 46 + hoverState * 20, this.getWidth() / 2, this.getHeight());

        int color = 14737632;

        if (packedFGColour != 0)
        {
            color = packedFGColour;
        }
        else if (!this.enabled)
        {
            color = 10526880;
        }
        else if (this.hovered)
        {
            color = 16777120;
        }
        this.drawCenteredString(mc.fontRenderer, this.displayString, this.x() + this.getWidth() / 2, this.y() + (this.getHeight() - 8) / 2, color);
    }

    @Override
    public ResourceLocation getTexture()
    {
        return BUTTON_TEXTURES;
    }
}
