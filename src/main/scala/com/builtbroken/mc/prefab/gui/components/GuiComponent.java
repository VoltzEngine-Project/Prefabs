package com.builtbroken.mc.prefab.gui.components;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.prefab.gui.GuiButton2;
import net.minecraft.client.Minecraft;
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
public abstract class GuiComponent<E extends GuiComponent> extends GuiButton2<E>
{
    /** Enable debug data for all components */
    public static boolean enableDebug = false;

    public GuiComponent(int id, Point point, String key)
    {
        super(id, point, key);
    }

    public GuiComponent(int id, int x, int y)
    {
        this(id, x, y, "");
    }

    public GuiComponent(int id, int x, int y, String key)
    {
        super(id, x, y, key);
    }

    public GuiComponent(int id, Point point, Point size, String key)
    {
        super(id, point, size, key);
    }

    public GuiComponent(int id, int x, int y, int width, int height, String key)
    {
        super(id, x, y, width, height, key);
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
            doRender(mc, mouseX, mouseY);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        this.drawTexturedModalRect(this.xPosition, this.yPosition, getU(), getV(), this.width, this.height);
        if (enableDebug)
        {
            this.drawString(mc.fontRenderer, "" + id, this.xPosition, this.yPosition, Color.red.getRGB());
        }
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
        return SharedAssets.GUI_COMPONENTS;
    }

    public boolean supportsDisabledState()
    {
        return false;
    }

    public boolean supportsHoverState()
    {
        return false;
    }
}
