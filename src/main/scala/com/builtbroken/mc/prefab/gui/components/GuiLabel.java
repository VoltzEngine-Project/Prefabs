package com.builtbroken.mc.prefab.gui.components;

import net.minecraft.client.Minecraft;

/**
 * Version of {@link GuiField} that can not be edited
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/30/2017.
 */
public class GuiLabel extends GuiComponent<GuiLabel>
{
    /** Has the current text being edited on the textbox. */
    private String text = "";

    public GuiLabel(int x, int y, String label)
    {
        super(-1, x, y);
        setText(label);
    }

    /**
     * Sets the text of the textbox
     */
    public void setText(String text)
    {
        if (text != null)
        {
            this.text = text;
        }
        else
        {
            setText("");
        }
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText()
    {
        return this.text;
    }

    @Override
    protected void preRender(Minecraft mc, int mouseX, int mouseY)
    {

    }

    @Override
    protected void postRender(Minecraft mc, int mouseX, int mouseY)
    {

    }

    @Override
    protected void drawBackground(Minecraft mc, int mouseX, int mouseY)
    {

    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY)
    {
        super.doRender(mc, mouseX, mouseY);
        final int textRenderColor = getTextColor();
        String displayString = Minecraft.getMinecraft().fontRenderer.trimStringToWidth(this.text, this.getWidth());
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(displayString, x(), y(), textRenderColor);
    }

    protected int getTextColor()
    {
        if (!isEnabled())
        {
            return 7368816;
        }
        return 14737632;
    }
}
