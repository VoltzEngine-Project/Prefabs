package com.builtbroken.mc.prefab.tile.entity;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Base version of TileEntity with useful helper methods
 * and functionality
 * Created by Dark on 9/4/2015.
 */
@Deprecated
public abstract class TileEntityBase extends TileEntity implements IWorldPosition
{
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    public Pos toPos()
    {
        return new Pos(xCoord, yCoord, zCoord);
    }

    public Location toLocation()
    {
        return new Location(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public World oldWorld()
    {
        return worldObj;
    }

    @Override
    public double x()
    {
        return xCoord;
    }

    @Override
    public double y()
    {
        return yCoord;
    }

    @Override
    public double z()
    {
        return zCoord;
    }

    @Override
    public float xf()
    {
        return xCoord;
    }

    @Override
    public float yf()
    {
        return yCoord;
    }

    @Override
    public float zf()
    {
        return zCoord;
    }

    @Override
    public int xi()
    {
        return xCoord;
    }

    @Override
    public int yi()
    {
        return yCoord;
    }

    @Override
    public int zi()
    {
        return zCoord;
    }
}
