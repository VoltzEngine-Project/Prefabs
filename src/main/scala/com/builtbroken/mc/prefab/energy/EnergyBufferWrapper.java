package com.builtbroken.mc.prefab.energy;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import net.minecraft.tileentity.TileEntity;

import java.lang.ref.WeakReference;

/**
 * Used to wrapper an internal energy buffer so to control connections
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/1/2017.
 */
public class EnergyBufferWrapper implements IEnergyBuffer
{
    /** Internal power buffer */
    public final IEnergyBuffer internalBuffer;

    /** Should power be accepted from outside sources */
    public boolean acceptPower = true;
    /** Should power be released to outside sources */
    public boolean releasePower = true;

    public WeakReference<TileEntity> connection;

    public EnergyBufferWrapper(IEnergyBuffer internalBuffer)
    {
        this.internalBuffer = internalBuffer;
    }

    @Override
    public int addEnergyToStorage(int energy, boolean doAction)
    {
        if (acceptPower)
        {
            return internalBuffer.addEnergyToStorage(energy, doAction);
        }
        return 0;
    }

    @Override
    public int removeEnergyFromStorage(int energy, boolean doAction)
    {
        if (releasePower)
        {
            return internalBuffer.removeEnergyFromStorage(energy, doAction);
        }
        return 0;
    }

    @Override
    public int getMaxBufferSize()
    {
        return internalBuffer.getMaxBufferSize();
    }

    @Override
    public int getEnergyStored()
    {
        return internalBuffer.getEnergyStored();
    }

    @Override
    public void setEnergyStored(int energy)
    {
        internalBuffer.setEnergyStored(energy);
    }

    public boolean hasConnection()
    {
        return connection != null && connection.get() != null && !connection.get().isInvalid();
    }

    public TileEntity getConnection()
    {
        if (hasConnection())
        {
            return connection.get();
        }
        return null;
    }

    public void setConnection(TileEntity connection)
    {
        this.connection = new WeakReference<TileEntity>(connection);
    }
}
