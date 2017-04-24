package com.builtbroken.mc.prefab.gui.slot;

import com.builtbroken.mc.api.data.weapon.IAmmoType;
import com.builtbroken.mc.api.items.weapons.IItemAmmo;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class SlotAmmo extends Slot
{
    private final IAmmoType ammoType;

    public SlotAmmo(IInventory inv, IAmmoType type, int id, int x, int y)
    {
        super(inv, id, x, y);
        this.ammoType = type;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof IItemAmmo && (ammoType == null || ammoType == ((IItemAmmo) stack.getItem()).getAmmoData(stack).getAmmoType());
    }
}
