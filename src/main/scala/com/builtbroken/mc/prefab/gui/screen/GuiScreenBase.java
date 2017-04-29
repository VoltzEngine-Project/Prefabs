package com.builtbroken.mc.prefab.gui.screen;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.region.Rectangle;
import com.builtbroken.mc.prefab.gui.components.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
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
    private GuiButton lastButtonClicked;

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

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseKey)
    {
        if (this.lastButtonClicked != null && mouseKey == 0) //left click
        {
            this.lastButtonClicked.mouseReleased(mouseX, mouseY);
            this.lastButtonClicked = null;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseKey)
    {
        if (mouseKey == 0) //left click
        {
            for (int l = 0; l < this.buttonList.size(); ++l)
            {
                GuiButton guibutton = (GuiButton) this.buttonList.get(l);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY))
                {
                    //Check if the event should be canceled
                    GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (MinecraftForge.EVENT_BUS.post(event))
                    {
                        break;
                    }
                    //Mark button as selected
                    this.lastButtonClicked = event.button;

                    //Fire audio of click
                    event.button.func_146113_a(this.mc.getSoundHandler());

                    if (guibutton.id >= 0)
                    {
                        //Fire event of click
                        this.actionPerformed(event.button);
                    }

                    //Do forge stuff
                    if (this.equals(this.mc.currentScreen))
                    {
                        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.buttonList));
                    }
                }
            }
        }
    }

    @Override
    protected void keyTyped(char c, int i)
    {
        if (Engine.runningAsDev && i == Keyboard.KEY_GRAVE)
        {
            GuiComponent.enableDebug = !GuiComponent.enableDebug;
            return;
        }
        for (Object o : buttonList)
        {
            if (o instanceof GuiComponent)
            {
                if (((GuiComponent) o).keyTyped(c, i))
                {
                    return;
                }
            }
        }
        super.keyTyped(c, i);
    }

    /**
     * Called to add a component to the GUI
     *
     * @param component
     * @param <E>
     * @return
     */
    public <E extends GuiComponent> E add(E component)
    {
        buttonList.add(component);
        component.setHost(this);
        return component;
    }

    public void remove(GuiButton button)
    {
        if (buttonList.contains(button))
        {
            buttonList.remove(button);
        }
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
