package com.builtbroken.mc.prefab.gui.slot;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.framework.energy.UniversalEnergySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotEnergyItem extends Slot implements ISlotRender
{
	public SlotEnergyItem(IInventory inv, int par3, int par4, int par5)
	{
		super(inv, par3, par4, par5);
	}

    @Override
    public boolean isItemValid(ItemStack compareStack)
    {
        return UniversalEnergySystem.isHandler(compareStack, null);
    }

    @Override
    public void renderSlotOverlay(Gui gui, int x, int y)
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(SharedAssets.GUI_COMPONENTS);
        gui.drawTexturedModalRect(x, y, 0, 18, 18, 18);
    }
}
