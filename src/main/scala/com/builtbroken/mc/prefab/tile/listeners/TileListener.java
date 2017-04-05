package com.builtbroken.mc.prefab.tile.listeners;

import com.builtbroken.mc.api.tile.listeners.ITileEventListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/3/2017.
 */
public abstract class TileListener implements ITileEventListener
{
    private World world;
    protected IBlockAccess blockAccess;
    private int x, y, z;

    @Override
    public World world()
    {
        return world;
    }

    @Override
    public double x()
    {
        return x;
    }

    @Override
    public double y()
    {
        return y;
    }

    @Override
    public double z()
    {
        return z;
    }

    public void inject(World world, int x, int y, int z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void inject(IBlockAccess world, int x, int y, int z)
    {
        this.blockAccess = world;
        if (world instanceof World)
        {
            this.world = (World) world;
        }
        else
        {
            this.world = null;
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected TileEntity getTileEntity()
    {
        if (world != null)
        {
            return world.getTileEntity(x, y, z);
        }
        else if (blockAccess != null)
        {
            return blockAccess.getTileEntity(x, y, z);
        }
        return null;
    }

    protected boolean setMeta(int meta, int flag)
    {
        if (world != null)
        {
            return world.setBlockMetadataWithNotify(x, y, z, meta, flag);
        }
        return false;
    }
}
