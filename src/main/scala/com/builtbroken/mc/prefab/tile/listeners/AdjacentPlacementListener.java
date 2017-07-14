package com.builtbroken.mc.prefab.tile.listeners;

import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.api.IModObject;
import com.builtbroken.mc.api.data.ActionResponse;
import com.builtbroken.mc.api.tile.listeners.IBlockListener;
import com.builtbroken.mc.api.tile.listeners.IPlacementListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListener;
import com.builtbroken.mc.api.tile.listeners.ITileEventListenerBuilder;
import com.builtbroken.mc.api.tile.node.ITileNodeHost;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.json.loading.JsonProcessorData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified placement listener for checking if a placement is valid
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 7/14/2017.
 */
public class AdjacentPlacementListener extends TileListener implements IPlacementListener, IBlockListener
{
    @JsonProcessorData("invert")
    protected boolean invert = false;

    protected List<Pair<Block, Integer>> blockList = new ArrayList();
    protected List<String> contentIDs = new ArrayList();

    public final Block block;

    public AdjacentPlacementListener(Block block)
    {
        this.block = block;
    }

    @Override
    public List<String> getListenerKeys()
    {
        List<String> list = new ArrayList();
        list.add("placement");
        return list;
    }

    @Override
    public ActionResponse canPlaceAt(Entity entity)
    {
        if (entity instanceof EntityPlayer)
        {
            ItemStack stack = ((EntityPlayer) entity).getHeldItem();
            if (stack == null || stack.getItemDamage() != metaCheck) //TODO add content ID check on item
            {
                return ActionResponse.IGNORE;
            }

            final Pos center = new Pos(this);
            IBlockAccess access = world() != null ? world() : blockAccess;
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
            {
                Pos pos = center.add(direction);
                Block block = pos.getBlock(access);
                int meta = pos.getBlockMetadata(access);

                if (block != null)
                {
                    //Check block and/or meta
                    if (blockList.contains(new Pair(block, -1)) || blockList.contains(new Pair(block, meta)))
                    {
                        return ActionResponse.DO;
                    }

                    //Check unique content ids
                    List<String> ids = new ArrayList();
                    TileEntity tile = pos.getTileEntity(access);
                    if (tile instanceof ITileNodeHost && ((ITileNodeHost) tile).getTileNode() != null)
                    {
                        ids.add((((ITileNodeHost) tile).getTileNode()).modID() + ":" + (((ITileNodeHost) tile).getTileNode()).uniqueContentID());
                    }
                    if (tile instanceof IModObject)
                    {
                        ids.add(((IModObject) tile).modID() + ":" + ((IModObject) tile).uniqueContentID());
                    }
                    if (block instanceof IModObject)
                    {
                        ids.add(((IModObject) block).modID() + ":" + ((IModObject) block).uniqueContentID());
                    }

                    for (String id : ids)
                    {
                        if (contentIDs.contains(id.toLowerCase()))
                        {
                            return ActionResponse.DO;
                        }
                    }
                }
            }
            return ActionResponse.CANCEL;
        }
        return ActionResponse.IGNORE;
    }

    @Override
    public boolean isValidForTile()
    {
        return true;
    }

    @JsonProcessorData("blocks")
    public void process(JsonElement inputElement)
    {
        if (inputElement.isJsonArray())
        {
            //Loop through elements in array
            for (JsonElement element : inputElement.getAsJsonArray())
            {
                //Get as object
                if (element.isJsonObject())
                {
                    JsonObject object = element.getAsJsonObject();

                    if (object.has("block"))
                    {
                        String blockName = object.getAsJsonPrimitive("block").getAsString();
                        int meta = -1;
                        if (object.has("data"))
                        {
                            meta = object.getAsJsonPrimitive("data").getAsInt();
                        }

                        //Get block
                        Block block = (Block) Block.blockRegistry.getObject(blockName);

                        if (block != null)
                        {
                            blockList.add(new Pair(block, meta));
                        }
                        //Provide warning if block is missing
                        else
                        {
                            Engine.logger().warn("AdjacentPlacementListener#process(JsonElement) >> Could not find '" + blockName + "' for " + this);
                        }
                    }
                    else if (object.has("contentID"))
                    {
                        contentIDs.add(object.getAsJsonPrimitive("contentID").getAsString().toLowerCase());
                    }
                    else
                    {
                        Engine.logger().warn("AdjacentPlacementListener#process(JsonElement) >> Could not find convert '" + element + "' int a usable type for " + this);
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Invalid data, block entries must look like \n {\n\t \"block\" : \"minecraft:tnt\",\n\t \"data\" : 0 \n}");
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid data, blocks data must be an array");
        }
    }

    @Override
    public String toString()
    {
        if (contentUseID != null)
        {
            return "AdjacentPlacementListener[" + block + " >> " + contentUseID + "]@" + hashCode();
        }
        else if (metaCheck != -1)
        {
            return "AdjacentPlacementListener[" + block + "@" + metaCheck + "]@" + hashCode();
        }
        return "AdjacentPlacementListener[" + block + "]@" + hashCode();
    }

    public static class Builder implements ITileEventListenerBuilder
    {
        @Override
        public ITileEventListener createListener(Block block)
        {
            return new AdjacentPlacementListener(block);
        }

        @Override
        public String getListenerKey()
        {
            return "adjacentPlacementListener";
        }
    }
}
