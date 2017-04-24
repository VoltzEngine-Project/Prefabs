package com.builtbroken.mc.prefab.gui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class SlotOutput extends Slot
{
    public SlotOutput(IInventory inv, int id, int x, int y)
    {
        super(inv, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack p_75214_1_)
    {
        return false;
    }
}
