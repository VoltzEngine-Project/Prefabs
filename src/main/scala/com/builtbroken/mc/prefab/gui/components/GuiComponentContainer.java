package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.imp.transform.vector.Point;
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
    protected boolean updatePositionLogic = true;

    public GuiComponentContainer(int id, Point point, String key)
    {
        super(id, point, key);
    }

    public GuiComponentContainer(int id, int x, int y)
    {
        super(id, x, y);
    }

    public GuiComponentContainer(int id, int x, int y, String key)
    {
        super(id, x, y, key);
    }

    public GuiComponentContainer(int id, Point point, Point size, String key)
    {
        super(id, point, size, key);
    }

    public GuiComponentContainer(int id, int x, int y, int width, int height, String key)
    {
        super(id, x, y, width, height, key);
    }

    @Override
    public E setHeight(int height)
    {
        super.setHeight(height);
        updatePositions();
        return (E) this;
    }

    @Override
    public E setWidth(int width)
    {
        super.setWidth(width);
        updatePositions();
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
            component.parentComponent = this;
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
            }
        }
    }

    public List<GuiComponent> getComponents()
    {
        return components;
    }
}
