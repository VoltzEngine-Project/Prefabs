package com.builtbroken.mc.prefab.gui.buttons;

import com.builtbroken.mc.client.SharedAssets;
import net.minecraft.util.ResourceLocation;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class GuiButton9px extends GuiImageButton
{
    public GuiButton9px(int id, int x, int y, int row, int col)
    {
        super(id, x, y, 9, 9, row * 9, col * 9 * 3); //Each col is 3 buttons wide (normal, hover, disable)
    }

    public static GuiButton9px newOnButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 3, 0);
    }

    public static GuiButton9px newOffButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 2, 0);
    }

    public static GuiButton9px newPlusButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 4, 0);
    }

    public static GuiButton9px newMinusButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 5, 0);
    }

    public static GuiButton9px newBlankButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 0, 0);
    }

    public ResourceLocation getTexture()
    {
        return SharedAssets.GUI_BUTTON_9PX;
    }

    @Override
    public boolean supportsDisabledState()
    {
        return true;
    }
}
