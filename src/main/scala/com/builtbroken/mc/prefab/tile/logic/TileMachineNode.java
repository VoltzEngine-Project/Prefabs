package com.builtbroken.mc.prefab.tile.logic;

import com.builtbroken.mc.api.ISave;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Prefab for creating tiles that have inventories and use power.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/17/2017.
 */
public abstract class TileMachineNode<I extends IInventory> extends TilePowerNode implements IInventoryProvider<I>
{
    /** Primary inventory container for this machine, all {@link IInventory} and {@link ISidedInventory} calls are wrapped to this object */
    protected I inventory_module;

    public TileMachineNode(String id, String mod)
    {
        super(id, mod);
    }

    //==================================
    //====== Inventory redirects =======
    //==================================

    @Override
    public I getInventory()
    {
        if (inventory_module == null)
        {
            inventory_module = createInventory();
        }
        return inventory_module;
    }

    /**
     * Creates a new inventory instance.
     * Called by {@link #getInventory()} if
     * {@link #inventory_module} is null
     *
     * @return new inventory
     */
    protected abstract I createInventory();


    //==================================
    //========== Save code =============
    //==================================

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        //By calling getInventory() we force the inventory to exist
        if (nbt.getBoolean("hasInventory") && getInventory() instanceof ISave)
        {
            ((ISave) getInventory()).load(nbt);
        }
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        super.save(nbt);
        //Cache if the inventory existed so it will force load
        nbt.setBoolean("hasInventory", inventory_module != null);
        //save inventory
        if (inventory_module instanceof ISave)
        {
            ((ISave) inventory_module).save(nbt);
        }
        return nbt;
    }
}
