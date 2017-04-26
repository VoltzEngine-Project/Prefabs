package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.GuiButton2;
import com.builtbroken.mc.prefab.gui.screen.GuiScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
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
    private GuiScreenBase host;

    public GuiComponent(int id, IPos2D point)
    {
        this(id, 0, 0);
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
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getTextureManager().bindTexture(getTexture() != null ? getTexture() : SharedAssets.GREY_TEXTURE_40pAlpha);
            drawBackground(mc, mouseX, mouseY);

            if (enableDebug)
            {
                this.drawCenteredString(mc.fontRenderer, "" + id, this.xPosition + (width / 2), this.yPosition + (height / 2), this instanceof GuiComponentContainer ? Color.red.getRGB() : Color.blue.getRGB());

                if (!(this instanceof GuiButton2))
                {
                    mc.getTextureManager().bindTexture(SharedAssets.GREY_TEXTURE);

                    Color color = Color.RED;
                    if (this instanceof GuiComponentContainer)
                    {
                        color = Color.blue;
                    }
                    GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                    this.drawTexturedModalRect(xPosition, yPosition, getU(), getV(), getWidth(), 2);
                    this.drawTexturedModalRect(xPosition, yPosition + getHeight() - 2, getU(), getV(), getWidth(), 2);

                    this.drawTexturedModalRect(xPosition, yPosition, getU(), getV(), 2, getHeight());
                    this.drawTexturedModalRect(xPosition + getWidth() - 2, yPosition, getU(), getV(), 2, getHeight());

                    color = Color.GREEN;
                    GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                    this.drawTexturedModalRect(xPosition, yPosition, getU(), getV(), 2, 2);

                    color = Color.black;
                    GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                    this.drawTexturedModalRect(xPosition + getWidth() - 2, yPosition, getU(), getV(), 2, 2);
                    this.drawTexturedModalRect(xPosition + getWidth() - 2, yPosition + getHeight() - 2, getU(), getV(), 2, 2);
                    this.drawTexturedModalRect(xPosition, yPosition + getHeight() - 2, getU(), getV(), 2, 2);
                }
            }

            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(getTexture() != null ? getTexture() : SharedAssets.GREY_TEXTURE_40pAlpha);
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
    }

    protected Color getBackgroundColor()
    {
        return null;
    }

    protected void update(Minecraft mc, int mouseX, int mouseY)
    {
        this.field_146123_n = isMouseInside(mouseX, mouseY);
    }

    protected int getU()
    {
        if (supportsDisabledState() && !isEnabled()) //disabled state
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
        return null;
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

    public boolean keyTyped(char c, int id)
    {
        return false;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.isEnabled() && visible() && isMouseInside(mouseX, mouseY);
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

    public boolean isEnabled()
    {
        return enabled && (getParentComponent() == null || getParentComponent().isEnabled());
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
    public GuiScreenBase getHost()
    {
        if (getParentComponent() != null)
        {
            return getParentComponent().getHost();
        }
        return host;
    }

    public E setHost(GuiScreenBase host)
    {
        this.host = host;
        return (E) this;
    }

    public void drawString(int x, int y, String s)
    {
        drawString(x, y, s, DEFAULT_STRING_COLOR);
    }

    public void drawString(int x, int y, String s, Color color)
    {
        drawString(Minecraft.getMinecraft().fontRenderer, s, x, y, color != null ? color.getRGB() : DEFAULT_STRING_COLOR);
    }

    public void drawString(int x, int y, String s, int color)
    {
        drawString(Minecraft.getMinecraft().fontRenderer, "" + s, x + x(), y + y(), color);
    }
}
