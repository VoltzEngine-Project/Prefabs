package com.builtbroken.mc.prefab.gui.screen;

import com.builtbroken.mc.imp.transform.region.Rectangle;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

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
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        for (Object o : buttonList)
        {
            if (o instanceof GuiComponent)
            {
                if (((GuiComponent) o).handleMouseInput(Minecraft.getMinecraft(), i, j))
                {
                    return;
                }
            }
        }
        super.handleMouseInput();
    }

    /**
     * Called to add a component to the GUI
     *
     * @param component
     * @param <E>
     * @return
     */
    protected <E extends GuiComponent> E add(E component)
    {
        buttonList.add(component);
        component.setHost(this);
        return component;
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        //Packet data is not sent if game is paused
        return false;
    }
}
