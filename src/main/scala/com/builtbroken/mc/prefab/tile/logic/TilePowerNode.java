package com.builtbroken.mc.prefab.tile.logic;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.tile.ConnectionType;
import com.builtbroken.mc.api.tile.ITileConnection;
import com.builtbroken.mc.api.tile.listeners.IChangeListener;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.energy.UniversalEnergySystem;
import com.builtbroken.mc.prefab.energy.EnergyBuffer;
import com.builtbroken.mc.prefab.energy.EnergyBufferWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Prefab for tiles that act as power components
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/1/2017.
 */
public class TilePowerNode extends TileNode implements ITileConnection, IEnergyBufferProvider, IChangeListener
{
    /** Internal energy buffer */
    protected EnergyBuffer energyBuffer;

    /** Acts as side configuration and cache of connections */
    protected EnergyBufferWrapper[] sides = new EnergyBufferWrapper[6];

    protected int nextConnectionUpdate = 1;

    public TilePowerNode(String id, String mod)
    {
        super(id, mod);
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            sides[dir.ordinal()] = createEnergySideWrapper();
        }
    }

    protected EnergyBufferWrapper createEnergySideWrapper()
    {
        return new EnergyBufferWrapper(getEnergyBuffer(ForgeDirection.UNKNOWN));
    }

    @Override
    public void update(long ticks)
    {
        super.update(ticks);
        updateConnections(ticks);
    }

    /** Called to update the connection map */
    protected void updateConnections(long ticks)
    {
        if (ticks % nextConnectionUpdate == 0 || ticks == -1)
        {
            final Pos center = toPos();
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                //Reset
                sides[dir.ordinal()].setConnection(null);

                //Get tile
                Pos pos = center.add(dir); //TODO fix to avoid orphan chunk loading
                TileEntity tile = pos.getTileEntity(world());

                //Check if is a valid connection
                if (UniversalEnergySystem.isHandler(tile, dir.getOpposite()) && canConnect(tile, ConnectionType.POWER, dir))
                {
                    sides[dir.ordinal()].setConnection(tile);
                }
            }

            //Set next tick time (Randomized to distribute ticks over time)
            nextConnectionUpdate = 10 + world().rand.nextInt(100);
        }
    }

    @Override
    public void onBlockChanged()
    {
        nextConnectionUpdate = 1;
    }

    //==================================
    //========== Energy code ===========
    //==================================

    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (getEnergyBufferSize() > 0)
        {
            if (energyBuffer == null)
            {
                energyBuffer = new EnergyBuffer(getEnergyBufferSize());
            }
            return sides[side.ordinal()];
        }
        return null;
    }

    @Override
    public boolean canConnect(TileEntity connection, ConnectionType type, ForgeDirection from)
    {
        return getEnergyBufferSize() > 0 && type == ConnectionType.POWER;
    }

    @Override
    public boolean hasConnection(ConnectionType type, ForgeDirection side)
    {
        if (type == ConnectionType.POWER)
        {
            return sides[side.ordinal()] != null && sides[side.ordinal()].hasConnection();
        }
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
        if (nbt.hasKey("energy") && getEnergyBuffer(ForgeDirection.UNKNOWN) != null)
        {
            getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(nbt.getInteger("energy"), true);
        }
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        super.save(nbt);
        if (energyBuffer != null && energyBuffer.getEnergyStored() > 0)
        {
            nbt.setInteger("energy", energyBuffer.getEnergyStored());
        }
        return nbt;
    }
}
