package com.builtbroken.mc.prefab.gui;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

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

    protected Function<E, Boolean> onMousePressFunction;

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
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            if (onMousePressFunction != null)
            {
                return !onMousePressFunction.apply((E) this);
            }
            return true;
        }
        return false;
    }

    public E setOnMousePressFunction(Function<E, Boolean> onMousePressFunction)
    {
        this.onMousePressFunction = onMousePressFunction;
        return (E) this;
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        //Reset color
        GL11.glColor4f(1f, 1f, 1f, 1f);

        int hoverState = this.getHoverState(this.field_146123_n);

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
        else if (this.field_146123_n)
        {
            color = 16777120;
        }
        this.drawCenteredString(mc.fontRenderer, this.displayString, this.x() + this.getWidth() / 2, this.y() + (this.getHeight() - 8) / 2, color);

        //Reset color
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    public ResourceLocation getTexture()
    {
        return buttonTextures;
    }
}
