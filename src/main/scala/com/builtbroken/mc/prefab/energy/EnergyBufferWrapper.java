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
        if (canAcceptPower())
        {
            return internalBuffer.addEnergyToStorage(energy, doAction);
        }
        return 0;
    }

    @Override
    public int removeEnergyFromStorage(int energy, boolean doAction)
    {
        if (canReleasePower())
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

    /**
     * Does the tile have a power connection from this buffer
     *
     * @return true if connection exists and is valid
     */
    public boolean hasConnection()
    {
        return connection != null && connection.get() != null && !connection.get().isInvalid();
    }

    /**
     * Gets the activate power connection for this buffer side.
     * <p>
     * Connection is stored in a weak reference to avoid issues.
     * Additionally connection will check for invalidation of the tile.
     * However, updating connection status must be done by the host tile.
     *
     * @return connection unless invalid or missing
     */
    public TileEntity getConnection()
    {
        if (hasConnection())
        {
            return connection.get();
        }
        return null;
    }

    /**
     * Sets the active power connection
     *
     * @param connection - connection, must not be null
     */
    public void setConnection(TileEntity connection)
    {
        this.connection = new WeakReference<TileEntity>(connection);
    }

    public EnergyBufferWrapper disableInput()
    {
        this.acceptPower = false;
        return this;
    }

    public EnergyBufferWrapper enableInput()
    {
        this.acceptPower = true;
        return this;
    }

    public EnergyBufferWrapper disableOutput()
    {
        this.releasePower = false;
        return this;
    }

    public EnergyBufferWrapper enableOutput()
    {
        this.releasePower = true;
        return this;
    }

    public EnergyBufferWrapper toggleInput()
    {
        this.acceptPower = !acceptPower;
        return this;
    }

    public EnergyBufferWrapper toggleOutput()
    {
        this.releasePower = !releasePower;
        return this;
    }

    public boolean canAcceptPower()
    {
        return acceptPower;
    }

    public boolean canReleasePower()
    {
        return releasePower;
    }
}
