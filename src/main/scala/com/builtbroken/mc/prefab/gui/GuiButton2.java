package com.builtbroken.mc.prefab.gui;

import com.builtbroken.mc.imp.transform.vector.Point;
import net.minecraft.client.gui.GuiButton;

/**
 * Wrapper/Utility version of the default MC button
 * Is also used a prefab for buttons with more features
 * <p>
 * Created by robert on 4/23/2015.
 * TODO need a class name
 */
public class GuiButton2<E extends GuiButton2> extends GuiButton
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

    public E setHeight(int height)
    {
        this.height = height;
        return (E) this;
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
}
