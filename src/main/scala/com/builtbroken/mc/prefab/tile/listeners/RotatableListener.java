package com.builtbroken.mc.prefab.tile.listeners;

import com.builtbroken.mc.api.tile.listeners.IBlockListener;
import com.builtbroken.mc.api.tile.listeners.IPlacementListener;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.lib.helper.BlockUtility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/5/2017.
 */
public class RotatableListener extends TileListener implements IPlacementListener, IBlockListener
{
    @Override
    public void onPlacedBy(EntityLivingBase entityLivingBase, ItemStack stack)
    {
        int rotation = BlockUtility.determineRotation(entityLivingBase);
        if (!setMeta(rotation, 3))
        {
            Engine.logger().error("Failed to set rotation for block at " + x() + "x," + y() + "y," + z() + "z,");
        }
    }
}
