package com.builtbroken.mc.prefab.gui.buttons;

import com.builtbroken.mc.client.SharedAssets;
import net.minecraft.util.ResourceLocation;

/**
 * Small 9px by 9px button used in place of larger buttons and text based buttons.
 * <p>
 * These buttons are imaged based but can easily be setup with text as well. However, do to the size only 1 to 2 chars can fit on the button.
 * It is advised to use these for images/icon buttons only.
 * <p>
 * Example uses of these button are close icon, save icon, on/off icon, simple option settings, and check box icons
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class GuiButton9px<E extends GuiButton9px> extends GuiImageButton<E>
{
    public static final int SIZE = 9;

    public GuiButton9px(int id, int x, int y, int row, int col)
    {
        super(id, x, y, SIZE, SIZE, col * SIZE * 3, row * SIZE); //Each col is 3 buttons wide (normal, hover, disable)
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

    public static GuiButton9px newXButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 1, 0);
    }

    public static GuiButton9px newUpButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 6, 0);
    }

    public static GuiButton9px newDownButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 7, 0);
    }

    public static GuiButton9px newLeftButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 8, 0);
    }

    public static GuiButton9px newRightButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 9, 0);
    }

    public static GuiButton9px newPlayerButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 13, 0);
    }

    public static GuiButton9px newGearButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 14, 0);
    }

    public static GuiButton9px newQuestionButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 16, 0);
    }

    public static GuiButton9px newNodeButton(int id, int x, int y)
    {
        return new GuiButton9px(id, x, y, 15, 0);
    }

    public void setTexturePos(int row, int col)
    {
        setUV(row * SIZE, col * SIZE * 3);
    }

    @Override
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
