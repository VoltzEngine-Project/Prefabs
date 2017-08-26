package com.builtbroken.mc.prefab.gui.buttons;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.prefab.gui.GuiButton2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Simple button that uses images instead of text
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2016.
 */
public class GuiImageButton<E extends GuiImageButton> extends GuiButton2<E>
{
    private int u, v;
    private ResourceLocation textureOverride;

    protected GuiImageButton(int id, int x, int y, int width, int height, int u, int v)
    {
        super(id, x, y, width, height, "");
        this.u = u;
        this.v = v;
    }

    /**
     * Updates the UV location of the
     * button on the texture sheet
     *
     * @param u
     * @param v
     */
    public void setUV(int u, int v)
    {
        this.u = u;
        this.v = v;
    }

    /**
     * Creates a new save button
     *
     * @param id
     * @param x
     * @param y
     * @return
     */
    public static GuiImageButton newSaveButton(int id, int x, int y)
    {
        return new GuiImageButton(id, x, y, 18, 18, 18, 162);
    }

    public static GuiImageButton newButtonEmpty(int id, int x, int y)
    {
        return new GuiImageButton(id, x, y, 18, 18, 18, 180);
    }

    /**
     * Creates a new refresh button
     *
     * @param id
     * @param x
     * @param y
     * @return
     */
    public static GuiImageButton newRefreshButton(int id, int x, int y)
    {
        return new GuiImageButton(id, x, y, 18, 18, 18, 198);
    }

    /**
     * Creates a new button with a width and height of 18 pixels
     *
     * @param id  - button id
     * @param x   - pos x
     * @param y   - pos y
     * @param row - row in the texture sheet
     * @param col - colume in the texture sheet
     * @return button
     */
    public static GuiImageButton newButton18(int id, int x, int y, int row, int col)
    {
        return new GuiImageButton(id, x, y, 18, 18, col * 18, row * 18);
    }

    /**
     * Overrides the default texture for buttons
     *
     * @param location
     */
    public GuiImageButton setTexture(ResourceLocation location)
    {
        this.textureOverride = location;
        return this;
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        this.drawTexturedModalRect(this.x, this.y, u + getURenderModifier(), v + getVRenderModifier(), this.getWidth(), this.getHeight());
    }

    protected int getURenderModifier()
    {
        if (supportsDisabledState() && !isEnabled())
        {
            return getWidth() * 2;
        }
        else if (hovered) //Hover state
        {
            return getWidth();
        }
        return 0;
    }

    protected int getVRenderModifier()
    {
        return 0;
    }

    @Override
    public ResourceLocation getTexture()
    {
        if(textureOverride != null)
        {
            return textureOverride;
        }
        return SharedAssets.GUI_COMPONENTS;
    }

    public boolean supportsDisabledState()
    {
        return false;
    }
}
