package com.builtbroken.mc.prefab.gui.screen;

import com.builtbroken.mc.imp.transform.region.Rectangle;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/24/2017.
 */
public class GuiScreenBase extends GuiScreen
{
    protected HashMap<Rectangle, String> tooltips = new HashMap();
    protected ArrayList<GuiTextField> fields = new ArrayList();

    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonList.clear();
        this.fields.clear();
        tooltips.clear();
    }
    @Override
    public void handleMouseInput()
    {
        for (Object o : buttonList)
        {
            if (o instanceof GuiComponent)
            {
                if (((GuiComponent) o).handleMouseInput())
                {
                    return;
                }
            }
        }
        super.handleMouseInput();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        //Packet data is not sent if game is paused
        return false;
    }
}
