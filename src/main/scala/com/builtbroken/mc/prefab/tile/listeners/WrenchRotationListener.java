package com.builtbroken.mc.prefab.tile.listeners;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.api.tile.listeners.IBlockListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListenerBuilder;
import com.builtbroken.mc.api.tile.listeners.IWrenchListener;
import com.builtbroken.mc.api.tile.node.ITileNodeHost;
import com.builtbroken.mc.lib.helper.BlockUtility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/8/2017.
 */
public class WrenchRotationListener extends TileListener implements IBlockListener, IWrenchListener
{
    @Override
    public boolean onPlayerRightClickWrench(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        IRotatable rotatable = null;
        TileEntity tile = getTileEntity();
        if (tile instanceof IRotatable)
        {
            rotatable = (IRotatable) tile;
        }
        else if (tile instanceof ITileNodeHost && ((ITileNodeHost) tile).getTileNode() instanceof IRotatable)
        {
            rotatable = (IRotatable) ((ITileNodeHost) tile).getTileNode();
        }

        if (rotatable != null)
        {
            if (isServer())
            {
                if (!player.isSneaking())
                {
                    if (rotatable.getDirection() == ForgeDirection.NORTH)
                    {
                        rotatable.setDirection(ForgeDirection.EAST);
                    }
                    else if (rotatable.getDirection() == ForgeDirection.EAST)
                    {
                        rotatable.setDirection(ForgeDirection.SOUTH);
                    }
                    else if (rotatable.getDirection() == ForgeDirection.SOUTH)
                    {
                        rotatable.setDirection(ForgeDirection.WEST);
                    }
                    else if (rotatable.getDirection() == ForgeDirection.WEST)
                    {
                        rotatable.setDirection(ForgeDirection.NORTH);
                    }
                }
                else
                {
                    ForgeDirection direction = BlockUtility.determineForgeDirection(player);
                    if (rotatable.getDirection() == direction)
                    {
                        rotatable.setDirection(direction.getOpposite());
                    }
                    else
                    {
                        rotatable.setDirection(direction);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> getListenerKeys()
    {
        List<String> list = new ArrayList();
        list.add("wrench");
        return list;
    }

    public static class Builder implements ITileEventListenerBuilder
    {
        @Override
        public ITileEventListener createListener(Block block)
        {
            return new WrenchRotationListener();
        }

        @Override
        public String getListenerKey()
        {
            return "wrenchRotation";
        }
    }
}
