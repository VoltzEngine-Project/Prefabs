package com.builtbroken.mc.prefab.energy;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.lib.energy.UniversalEnergySystem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Basic implementation of energy buffer
 * Created by Dark on 8/15/2015.
 */
public class EnergyBuffer implements IEnergyBuffer
{
    private final int maxStorage;
    private int energyStorage;

    public EnergyBuffer(int maxStorage)
    {
        this.maxStorage = maxStorage;
    }

    @Override
    public int addEnergyToStorage(int energy, boolean doAction)
    {
        int prev = getEnergyStored();
        if (energy > 0)
        {
            int roomLeft = getMaxBufferSize() - getEnergyStored();
            if (energy < roomLeft)
            {
                if (doAction)
                {
                    energyStorage += energy;
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.ADD);
                    }
                }
                return energy;
            }
            else
            {
                if (doAction)
                {
                    energyStorage = getMaxBufferSize();
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.ADD);
                    }
                }
                return roomLeft;
            }
        }
        return 0;
    }

    @Override
    public int removeEnergyFromStorage(int energy, boolean doAction)
    {
        int prev = getEnergyStored();
        if (energy > 0 && energyStorage > 0)
        {
            if (energy >= maxStorage)
            {
                if (doAction)
                {
                    energyStorage = 0;
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.REMOVE);
                    }
                }
                return maxStorage;
            }
            else
            {
                if (doAction)
                {
                    energyStorage -= energy;
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.REMOVE);
                    }
                }
                return energy;
            }
        }
        return 0;
    }

    /**
     * Called when the power changes in the buffer
     *
     * @param prevEnergy - energy before action
     * @param current    - energy after action
     */
    protected void onPowerChange(int prevEnergy, int current, EnergyActionType actionType)
    {

    }

    @Override
    public int getMaxBufferSize()
    {
        return maxStorage;
    }

    @Override
    public int getEnergyStored()
    {
        return energyStorage;
    }

    @Override
    public void setEnergyStored(int energy)
    {
        int prev = getEnergyStored();
        this.energyStorage = Math.min(maxStorage, Math.max(0, energy));
        if (prev != energyStorage)
        {
            onPowerChange(prev, getEnergyStored(), EnergyActionType.SET);
        }
    }

    /**
     * Called to remove energy from the item and add it to this storage
     * <p>
     * Helper method to simplify the need to call {@link UniversalEnergySystem} directly
     *
     * @param stackInSlot - stack
     */
    public void addEmeryFromItem(ItemStack stackInSlot)
    {
        if (UniversalEnergySystem.isHandler(stackInSlot, ForgeDirection.UNKNOWN))
        {
            int energy = (int) Math.floor(UniversalEnergySystem.drain(stackInSlot, Integer.MAX_VALUE, false));
            if (energy > 0)
            {
                UniversalEnergySystem.drain(stackInSlot, addEnergyToStorage(energy, true), true);
            }
        }
    }

    public static enum EnergyActionType
    {
        ADD,
        REMOVE,
        SET
    }
}
