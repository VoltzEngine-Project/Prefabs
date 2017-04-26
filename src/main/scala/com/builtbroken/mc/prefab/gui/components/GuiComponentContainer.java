package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.jlib.data.vector.IPos2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public class GuiComponentContainer<E extends GuiComponentContainer> extends GuiComponent<E>
{
    private final List<GuiComponent> components = new ArrayList();

    public boolean resizeAsNeeded = false;
    protected boolean updatePositionLogic = true;

    public GuiComponentContainer(int id, IPos2D point)
    {
        super(id, point);
    }

    public GuiComponentContainer(int id, int x, int y)
    {
        super(id, x, y);
    }

    public GuiComponentContainer(int id, int x, int y, int width, int height, String key)
    {
        super(id, x, y, width, height, key);
    }

    @Override
    public E setHeight(int height)
    {
        super.setHeight(height);
        if (updatePositionLogic)
        {
            updatePositions();
        }
        return (E) this;
    }

    @Override
    public E setWidth(int width)
    {
        super.setWidth(width);
        if (updatePositionLogic)
        {
            updatePositions();
        }
        return (E) this;
    }

    @Override
    protected void updatePositions()
    {
        if (updatePositionLogic)
        {
            super.updatePositions();
            for (GuiComponent component : getComponents())
            {
                component.updatePositions();
            }
        }
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        super.doRender(mc, mouseX, mouseY);
        for (GuiComponent component : getComponents())
        {
            GL11.glPushMatrix();
            component.drawButton(mc, mouseX, mouseY);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY)
    {
        super.mouseReleased(mouseX, mouseY);
        for (GuiComponent component : getComponents())
        {
            component.mouseReleased(mouseX, mouseY);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            for (GuiComponent component : getComponents())
            {
                if (component.mousePressed(mc, mouseX, mouseY))
                {
                    actionPerformed(component);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void func_146111_b(int mouseX, int mouseY)
    {
        super.func_146111_b(mouseX, mouseY);
        for (GuiComponent component : getComponents())
        {
            component.func_146111_b(mouseX, mouseY);
        }
    }

    @Override
    public void func_146113_a(SoundHandler handler)
    {
        for (GuiComponent component : getComponents())
        {
            component.func_146113_a(handler);
        }
    }

    /**
     * Called when a sub component is pressed
     *
     * @param button
     */
    public void actionPerformed(GuiButton button)
    {

    }

    /**
     * Called to add a component to this container
     *
     * @param component
     * @param <E>
     * @return
     */
    public <E extends GuiComponent> E add(E component)
    {
        if (!getComponents().contains(component))
        {
            this.getComponents().add(component);
            component.setParentComponent(this);
            if (updatePositionLogic)
            {
                calcSize();
            }
        }
        return component;
    }

    public void remove(GuiComponent component)
    {
        if (getComponents().contains(component))
        {
            getComponents().remove(component);
            if (updatePositionLogic)
            {
                updatePositions();
                calcSize();
            }
        }
    }

    /**
     * Attempts to resize the container to fit it's
     * components automatically.
     */
    protected void calcSize()
    {
        if (resizeAsNeeded)
        {
            int width = 0;
            int height = 0;
            for (GuiComponent component : getComponents())
            {
                int sizeX = (component.x() - xPosition) + component.getWidth();
                int sizeY = (component.y() - xPosition) + component.getHeight();
                if (sizeX > width)
                {
                    width = sizeX;
                }
                if (sizeY > height)
                {
                    height = sizeY;
                }
            }
            updatePositionLogic = false;
            setWidth(width);
            setHeight(height);
            updatePositionLogic = true;
        }
    }

    public List<GuiComponent> getComponents()
    {
        return components;
    }
}
