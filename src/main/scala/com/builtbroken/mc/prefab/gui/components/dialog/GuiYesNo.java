package com.builtbroken.mc.prefab.gui.components.dialog;

import com.builtbroken.mc.prefab.gui.GuiButton2;
import com.builtbroken.mc.prefab.gui.pos.HugBottom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/25/2017.
 */
public class GuiYesNo extends GuiDialog<GuiYesNo>
{
    public String message;

    public int state = -1;

    public GuiYesNo(int id, int x, int y, String title, String message)
    {
        super(id, x, y);
        this.setComponentWidth(300);
        this.setComponentHeight(200);
        this.displayString = title;
        this.message = message;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        add(new GuiButton2(0, new HugBottom(this, 5, -25, true), "Yes").setComponentWidth(50));
        add(new GuiButton2(1, new HugBottom(this, -55, -25, false), "No").setComponentWidth(50));
        updatePositions();
    }

    @Override
    protected void doRender(Minecraft mc, int mouseX, int mouseY, float ticks)
    {
        super.doRender(mc, mouseX, mouseY, ticks);
        drawString(0, 0, displayString);
        drawString(0, 20, message);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        state = button.id;
        if (getParentComponent() != null)
        {
            getParentComponent().actionPerformed(this);
        }
        else if (getHost() != null)
        {
            getHost().actionPerformed(this);
        }
    }
}
