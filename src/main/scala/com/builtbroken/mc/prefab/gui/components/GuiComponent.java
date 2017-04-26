package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.imp.transform.vector.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Prefab for GUI components to extend
 * <p>
 * Each component acts as a button in order to receive click events.
 * <p>
 * see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2015.
 */
public abstract class GuiComponent<E extends GuiComponent> extends GuiButton
{
    public static final Point DEFAULT_SIZE = new Point(200, 20);
    public static final int DEFAULT_STRING_COLOR = 16777215;

    /** Enable debug data for all components */
    public static boolean enableDebug = false;

    private GuiComponentContainer parentComponent;
    private IPos2D relativePosition;
    private GuiScreen host;

    public GuiComponent(int id, Point point)
    {
        this(id, point.xi(), point.yi());
        this.setRelativePosition(point);
    }

    /**
     * Creates a component with the id at the position
     *
     * @param id - button action ID
     * @param x  - location
     * @param y  - location
     */
    public GuiComponent(int id, int x, int y)
    {
        this(id, x, y, DEFAULT_SIZE.xi(), DEFAULT_SIZE.yi(), "");
    }

    /**
     * Creates a component with the id at the position with size
     *
     * @param id     - button action ID
     * @param x      - location
     * @param y      - location
     * @param width  - how wide the component is (X axis)
     * @param height - how tall the component is (Y axis)
     */
    public GuiComponent(int id, int x, int y, int width, int height, String key)
    {
        super(id, x, y, width, height, key);
    }

    /**
     * Called anytime this component or it
     * parent is adjusted in size, shaped, or
     * position.
     */
    protected void updatePositions()
    {
        if (getRelativePosition() != null && getParentComponent() != null)
        {
            this.setPosition(getParentComponent().x() + getRelativePosition().xi(), getParentComponent().y() + getRelativePosition().yi());
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        update(mc, mouseX, mouseY);
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(getTexture());

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawBackground(mc, mouseX, mouseY);
            doRender(mc, mouseX, mouseY);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {

    }

    protected void drawBackground(Minecraft mc, int mouseX, int mouseY)
    {
        Color color = getBackgroundColor();
        if (color != null)
        {
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        }
        this.drawTexturedModalRect(this.xPosition, this.yPosition, getU(), getV(), this.width, this.height);
        if (enableDebug)
        {
            this.drawString(mc.fontRenderer, "" + id, this.xPosition, this.yPosition, Color.red.getRGB());
        }
    }

    protected Color getBackgroundColor()
    {
        return null;
    }

    protected void update(Minecraft mc, int mouseX, int mouseY)
    {
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    protected int getU()
    {
        if (supportsDisabledState() && !enabled) //disabled state
        {
            return width * 2;
        }
        else if (supportsHoverState() && field_146123_n) //Hover state
        {
            return width;
        }
        return 0;
    }

    protected int getV()
    {
        return 0;
    }

    public ResourceLocation getTexture()
    {
        return SharedAssets.GREY_TEXTURE;
    }

    public boolean supportsDisabledState()
    {
        return false;
    }

    public boolean supportsHoverState()
    {
        return false;
    }

    /**
     * Called to handle mouse input logic.
     * <p>
     * Designed to be used to capture right
     * click or mouse wheel movement. As
     * left click is already used.
     */
    public boolean handleMouseInput(Minecraft mc, int mouseX, int mouseY)
    {
        return false;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && visible() && isMouseInside(mouseX, mouseY);
    }

    public boolean isMouseInside(int mouseX, int mouseY)
    {
        return mouseX >= x() && mouseY >= y() && mouseX < x() + getWidth() && mouseY < y() + getHeight();
    }

    public int getHeight()
    {
        return height;
    }

    public E setHeight(int height)
    {
        this.height = height;
        return (E) this;
    }

    public int getWidth()
    {
        return width;
    }

    public E setWidth(int width)
    {
        this.width = width;
        return (E) this;
    }

    public E enable()
    {
        this.enabled = true;
        return (E) this;
    }

    public E disable()
    {
        this.enabled = false;
        return (E) this;
    }

    public E setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return (E) this;
    }

    public boolean visible()
    {
        return visible;
    }

    public E show()
    {
        this.visible = true;
        return (E) this;
    }

    public E hide()
    {
        this.visible = false;
        return (E) this;
    }

    public int x()
    {
        return xPosition;
    }

    public int y()
    {
        return yPosition;
    }

    public E setPosition(int x, int y)
    {
        this.xPosition = x;
        this.yPosition = y;
        return (E) this;
    }

    /** Parent component holding this component */
    public GuiComponentContainer getParentComponent()
    {
        return parentComponent;
    }

    public E setParentComponent(GuiComponentContainer parentComponent)
    {
        this.parentComponent = parentComponent;
        return (E) this;
    }

    /** Relative position to it's parent */
    public IPos2D getRelativePosition()
    {
        return relativePosition;
    }

    public E setRelativePosition(IPos2D relativePosition)
    {
        this.relativePosition = relativePosition;
        return (E) this;
    }

    /**
     * Gets the GUI that is hosting this component
     *
     * @return host, or parent's host
     */
    public GuiScreen getHost()
    {
        if (getParentComponent() != null)
        {
            return getParentComponent().getHost();
        }
        return host;
    }

    public E setHost(GuiScreen host)
    {
        this.host = host;
        return (E) this;
    }
}
