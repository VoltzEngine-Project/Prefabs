package com.builtbroken.mc.prefab.tile.logic;

import com.builtbroken.mc.api.ISave;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.tile.ConnectionType;
import com.builtbroken.mc.api.tile.ITileConnection;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.mc.prefab.energy.EnergyBuffer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/17/2017.
 */
public abstract class TileMachineNode<I extends IInventory> extends TileNode implements IInventoryProvider<I>, ITileConnection, IEnergyBufferProvider
{
    /** Primary inventory container for this machine, all {@link IInventory} and {@link ISidedInventory} calls are wrapped to this object */
    protected I inventory_module;
    protected EnergyBuffer energyBuffer;

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
    //========== Energy code ===========
    //==================================

    @Override
    public EnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (energyBuffer == null && getEnergyBufferSize() > 0)
        {
            energyBuffer = new EnergyBuffer(getEnergyBufferSize());
        }
        return energyBuffer;
    }

    @Override
    public boolean canConnect(TileEntity connection, ConnectionType type, ForgeDirection from)
    {
        return getEnergyBufferSize() > 0 && type == ConnectionType.POWER;
    }

    @Override
    public boolean hasConnection(ConnectionType type, ForgeDirection side)
    {
        return false;
    }

    public int getEnergyBufferSize()
    {
        return 0;
    }

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
        if (nbt.hasKey("energy") && getEnergyBuffer(ForgeDirection.UNKNOWN) != null)
        {
            getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(nbt.getInteger("energy"), true);
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

        if (energyBuffer != null && energyBuffer.getEnergyStored() > 0)
        {
            nbt.setInteger("energy", energyBuffer.getEnergyStored());
        }
        return nbt;
    }
}
