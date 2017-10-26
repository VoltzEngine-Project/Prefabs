package com.builtbroken.mc.prefab.tile;

import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;

/**
 * Temporary fix for Tile not breaking with TileEntity dat
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/25/2017.
 */
public class TileEvents
{
    public static final TileEvents instance = new TileEvents();

    @SubscribeEvent
    public void onBreakEvent(BlockEvent.BreakEvent event)
    {
        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if (tile instanceof Tile && event.getPlayer() instanceof EntityPlayerMP && !event.getPlayer().capabilities.isCreativeMode)
        {
            EntityPlayerMP playerMP = (EntityPlayerMP) event.getPlayer();

            ItemStack stack = playerMP.getCurrentEquippedItem();
            if (stack != null && stack.getItem().onBlockStartBreak(stack, event.x, event.y, event.z, playerMP))
            {
                return;
            }
            //Get data
            Block block = event.world.getBlock(event.x, event.y, event.z);
            int metadata = event.world.getBlockMetadata(event.x, event.y, event.z);

            //Fire audio event
            event.world.playAuxSFXAtEntity(playerMP, 2001, event.x, event.y, event.z, Block.getIdFromBlock(block) + (event.world.getBlockMetadata(event.x, event.y, event.z) << 12));

            //Check if player can break block
            ItemStack itemstack = playerMP.getCurrentEquippedItem();
            boolean flag1 = block.canHarvestBlock(playerMP, metadata);

            //Damage item
            if (itemstack != null)
            {
                itemstack.func_150999_a(event.world, block, event.x, event.y, event.z, playerMP);

                if (itemstack.stackSize == 0)
                {
                    playerMP.destroyCurrentEquippedItem();
                }
            }

            //Get drops before block is broken
            ArrayList<ItemStack> items = ((Tile) tile).getDrops(metadata, EnchantmentHelper.getFortuneModifier(playerMP));

            //Remove block
            if (removeBlock(playerMP, event.world, event.x, event.y, event.z, flag1))
            {
                for (ItemStack drop : items)
                {
                    if (drop != null && drop.getItem() != null && drop.stackSize > 0)
                    {
                        InventoryUtility.dropItemStack(event.world, event.x + 0.5, event.y + 0.5, event.z + 0.5, drop, 0, 0);
                    }
                }
            }

            //Stop normal event
            event.setCanceled(true);
        }
    }

    private boolean removeBlock(EntityPlayerMP playerMP, World theWorld, int x, int p_73079_2_, int p_73079_3_, boolean canHarvest)
    {
        Block block = theWorld.getBlock(x, p_73079_2_, p_73079_3_);
        int l = theWorld.getBlockMetadata(x, p_73079_2_, p_73079_3_);
        block.onBlockHarvested(theWorld, x, p_73079_2_, p_73079_3_, l, playerMP);
        boolean flag = block.removedByPlayer(theWorld, playerMP, x, p_73079_2_, p_73079_3_, canHarvest);

        if (flag)
        {
            block.onBlockDestroyedByPlayer(theWorld, x, p_73079_2_, p_73079_3_, l);
        }

        return flag;
    }
}
