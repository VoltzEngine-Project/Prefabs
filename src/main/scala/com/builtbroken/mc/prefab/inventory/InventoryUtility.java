package com.builtbroken.mc.prefab.inventory;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.lang.StringHelpers;
import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.api.IInventoryFilter;
import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.data.item.ItemStackWrapper;
import com.builtbroken.mc.lib.helper.NBTUtility;
import com.google.common.collect.Table;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Series of helper classes for dealing with any kind of inventory
 *
 * @author Calclavia, DarkCow(aka Darkguardsman, Robert)
 */
public class InventoryUtility
{
    private static final HashMap<String, List<Item>> MOD_TO_ITEMS = new HashMap();
    private static final HashMap<String, Object> NAME_TO_ITEM = new HashMap();
    private static final HashMap<Item, List<IRecipe>> ITEM_TO_RECIPES = new HashMap();
    private static final HashMap<ItemStackWrapper, List<IRecipe>> ITEMSTACK_TO_RECIPES = new HashMap();

    /**
     * Called to map all registered items to
     * several data points. Such as mod to
     * items.
     */
    public static void mapItems()
    {
        //Empty old data
        MOD_TO_ITEMS.clear();
        NAME_TO_ITEM.clear();

        //Start logging
        Engine.logger().info("Mapping item data...");
        long time = System.nanoTime();

        //Get registry
        RegistryNamespaced<ResourceLocation, Item> registry = Item.REGISTRY;
        Engine.logger().info("  " + registry.getKeys().size() + " entries detected");
        //Loop all entries
        for (ResourceLocation key : registry.getKeys())
        {
            String name = (String) key.toString();
            String modID = name.substring(0, name.indexOf(":"));
            name = name.substring(name.indexOf(":") + 1, name.length()).toLowerCase();

            Object entry = registry.getObject(key);
            if (entry instanceof Item)
            {
                Item item = (Item) entry;

                //Map items to mod name
                List<Item> items = MOD_TO_ITEMS.get(modID);
                if (items == null)
                {
                    items = new ArrayList();
                }
                items.add(item);
                MOD_TO_ITEMS.put(modID, items);

                //Map item name to item
                Object nameEntry = NAME_TO_ITEM.get(name);
                if (nameEntry != null)
                {
                    if (nameEntry instanceof Item)
                    {
                        List<Item> list = new ArrayList();
                        list.add(item);
                        list.add((Item) nameEntry);
                        NAME_TO_ITEM.put(name, list);
                    }
                    else if (nameEntry instanceof List)
                    {
                        ((List) nameEntry).add(item);
                        NAME_TO_ITEM.put(name, nameEntry);
                    }
                }
                else
                {
                    NAME_TO_ITEM.put(name, item);
                }
            }
        }
        Engine.logger().info(" Done in.. " + StringHelpers.formatNanoTime(System.nanoTime() - time));
    }

    /**
     * Gets all items registered to a mod
     *
     * @param modid - mod's id
     * @return list or null if nothing was registered
     */
    public static List<Item> getItemsForMod(String modid)
    {
        if (MOD_TO_ITEMS.isEmpty())
        {
            mapItems();
        }
        return MOD_TO_ITEMS.get(modid);
    }

    /**
     * Gets the item by name
     * <p>
     * (mod:name)
     * <p>
     * name needs to be exact for this method to function correctly.
     *
     * @param name - mod:name, registered string ID of the item
     * @return item or null if not found
     */
    public static Item getItem(String name)
    {
        ResourceLocation location = new ResourceLocation(name);
        RegistryNamespaced<ResourceLocation, Item> registry = Item.REGISTRY;
        if (registry.containsKey(location))
        {
            return registry.getObject(location);
        }
        return null;
    }

    public static Block getBlock(String name)
    {
        return Block.getBlockFromName(name);
    }

    /**
     * Gets the item by name as an ItemStack of the meta value
     * <p>
     * (mod:name)
     * <p>
     * name needs to be exact for this method to function correctly.
     *
     * @param name - mod:name, registered string ID of the item
     * @param meta - meta value (0-32,000)
     * @return ItemStack or null if not found
     */
    public static ItemStack getItemStack(String name, int meta)
    {
        Item item = getItem(name);
        if (item != null)
        {
            return new ItemStack(item, 1, meta);
        }
        return null;
    }

    /**
     * Gets the item by name as an ItemStack
     * <p>
     * Attempts to use the custom data to
     * get the item first. This way any NBT data or other params
     * can be copied to the stack.
     * <p>
     * Keep in mind the name value will be different for custom stacks
     * than it will be for the actual registered item. So use this
     * with caution and backup values.
     * <p>
     * (mod:name)
     * <p>
     * Name needs to be exact for this method to function correctly.
     * Use {@link com.builtbroken.mc.core.commands.debug.CommandDebugItem}
     * to get data on what the name actually is and sub types.
     *
     * @param name - mod:name, registered string ID of the item
     * @return ItemStack or null if not found
     */
    public static ItemStack getItemStack(String name)
    {
        ItemStack is = null;
        try
        {
            Field field = GameData.class.getDeclaredField("customItemStacks");
            field.setAccessible(true);
            String[] split = name.split(":");
            is = ((Table<String, String, ItemStack>) field.get(null)).get(split[0], split[1]);
            if (is != null && is.getItem() == null)
            {
                is = null;
                Engine.logger().error("Error: CustomItemStack with name=" + name + " contains a null item", new RuntimeException());
            }
            if (is != null)
            {
                is = is.copy();
            }
        }
        catch (Exception e)
        {
            if (Engine.runningAsDev)
            {
                Engine.logger().error("Failed to access customItemStack data", e);
            }
        }
        if (is == null)
        {
            Item i = getItem(name);
            if (i != null)
            {
                is = new ItemStack(i, 1, 0);
            }
        }
        if (is == null)
        {
            Block b = getBlock(name);
            if (b != null)
            {
                is = new ItemStack(b, 1, Short.MAX_VALUE);
            }
        }
        if (is != null)
        {
            is.setCount(1);
        }
        return is;
    }

    /**
     * Called to map recipes to items and
     * itemstacks. Used for varies purposes,
     * such as, quicker filter checks, crafting,
     * guis, and commands.
     */
    public static void mapRecipes()
    {
        ITEMSTACK_TO_RECIPES.clear();
        ITEM_TO_RECIPES.clear();

        RegistryNamespaced<ResourceLocation, IRecipe> registry = CraftingManager.REGISTRY;
        Engine.logger().info("Mapping basic recipe data...");
        Engine.logger().info("   " + registry.getKeys().size() + " recipes detected.");

        long time = System.nanoTime();

        for (ResourceLocation key : registry.getKeys())
        {
            IRecipe recipe = registry.getObject(key);
            if (recipe != null && recipe.getRecipeOutput() != null)
            {
                ItemStackWrapper wrapper = new ItemStackWrapper(recipe.getRecipeOutput());

                //Update stack recipe list
                List<IRecipe> list = ITEMSTACK_TO_RECIPES.get(wrapper);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add(recipe);
                ITEMSTACK_TO_RECIPES.put(wrapper, list);

                //Update item recipe list
                Item item = recipe.getRecipeOutput().getItem();
                list = ITEM_TO_RECIPES.get(item);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add(recipe);
                ITEM_TO_RECIPES.put(item, list);
            }
        }
        Engine.logger().info(" Done in.. " + StringHelpers.formatNanoTime(System.nanoTime() - time));
    }

    /**
     * Gets all recipes with the given output item
     *
     * @param item - item
     * @return list or null if none were mapped
     */
    public static List<IRecipe> getRecipesWithOutput(Item item)
    {
        if (ITEM_TO_RECIPES.isEmpty())
        {
            mapRecipes();
        }
        return ITEM_TO_RECIPES.get(item);
    }

    /**
     * Gets all recipes with the given output item
     *
     * @param item - item
     * @return list or null if none were mapped
     */
    public static List<IRecipe> getRecipesWithOutput(ItemStack item)
    {
        if (ITEMSTACK_TO_RECIPES.isEmpty())
        {
            mapRecipes();
        }
        return ITEMSTACK_TO_RECIPES.get(new ItemStackWrapper(item));
    }

    /**
     * Used to combine two chests together to make a large chest
     *
     * @param inv - inventory that is an instance of TileEntityChest
     * @return new InventoryLargeChest
     */
    public static IInventory checkChestInv(IInventory inv)
    {
        if (inv instanceof TileEntityChest)
        {
            TileEntityChest main = (TileEntityChest) inv;
            TileEntityChest adj = null;

            if (main.adjacentChestXNeg != null)
            {
                adj = main.adjacentChestXNeg;
            }
            else if (main.adjacentChestXPos != null)
            {
                adj = main.adjacentChestXPos;
            }
            else if (main.adjacentChestZNeg != null)
            {
                adj = main.adjacentChestZNeg;
            }
            else if (main.adjacentChestZPos != null)
            {
                adj = main.adjacentChestZPos;
            }

            if (adj != null)
            {
                return new InventoryLargeChest("", main, adj);
            }
        }

        return inv;
    }

    public static ItemStack copyStack(ItemStack stack, int stackSize)
    {
        ItemStack stack1 = stack.copy();
        stack1.setCount(stackSize);
        return stack1;
    }

    /**
     * Places the stack into the inventory in the first slot it can find
     *
     * @param inventory            - inventory to scan
     * @param toInsert             - stack to insert into the inventory
     * @param ignoreIsValidForSlot - ignores the {@link IInventory#isItemValidForSlot(int, ItemStack)} check on
     *                             inventories. Not normally used but is here just in case it is needed.
     * @return what is left of the item stack after inserting
     */
    public static ItemStack putStackInInventory(IInventory inventory, ItemStack toInsert, boolean ignoreIsValidForSlot)
    {
        //Work around for chests having a shared inventory
        if (inventory instanceof TileEntityChest)
        {
            inventory = checkChestInv(inventory);
        }

        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (ignoreIsValidForSlot || inventory.isItemValidForSlot(slot, toInsert))
            {
                //TODO check if the follow code is valid and segment into reusable method
                ItemStack slot_stack = inventory.getStackInSlot(slot);

                if (slot_stack.isEmpty())
                {
                    inventory.setInventorySlotContents(slot, toInsert);
                    return ItemStack.EMPTY;
                }
                else if (slot_stack.isItemEqual(toInsert) && slot_stack.getCount() < slot_stack.getMaxStackSize())
                {
                    if (slot_stack.getCount() + toInsert.getCount() <= slot_stack.getMaxStackSize())
                    {
                        ItemStack toSet = toInsert.copy();
                        toSet.setCount(toSet.getCount() + slot_stack.getCount());

                        inventory.setInventorySlotContents(slot, toSet);
                        return ItemStack.EMPTY;
                    }
                    else
                    {
                        int rejects = (slot_stack.getCount() + toInsert.getCount()) - slot_stack.getMaxStackSize();

                        ItemStack toSet = toInsert.copy();
                        toSet.setCount(slot_stack.getMaxStackSize());

                        ItemStack remains = toInsert.copy();
                        remains.setCount(rejects);

                        inventory.setInventorySlotContents(slot, toSet);

                        toInsert = remains;
                    }
                }
            }
        }
        return toInsert;
    }

    /**
     * Tries to place the into a valid tile at the location. If the tile is not an inventory it will
     * return the unused toInsert stack.
     *
     * @param position - position to check for a tile
     * @param toInsert - stack to insert into the tile
     * @param side     - side to insert the item into (0-5)
     * @param force    - overrides {@link IInventory#isItemValidForSlot(int, ItemStack)} check
     * @return what is left of the toInsert stack
     */
    public static ItemStack insertStack(Location position, ItemStack toInsert, EnumFacing side, boolean force)
    {
        return insertStack(position.getTileEntity(), toInsert, side, force);
    }

    /**
     * Tries to place the into a valid tile at the location. If the tile is not an inventory it will
     * return the unused toInsert stack.
     *
     * @param tile     - tile to place the item into
     * @param toInsert - stack to insert into the tile
     * @param side     - side to insert the item into (0-5)
     * @param force    - overrides {@link IInventory#isItemValidForSlot(int, ItemStack)} check
     * @return what is left of the toInsert stack
     */
    public static ItemStack insertStack(TileEntity tile, ItemStack toInsert, EnumFacing side, boolean force)
    {
        if (tile instanceof IInventory)
        {
            return putStackInInventory((IInventory) tile, toInsert, side, force);
        }
        return toInsert;
    }


    /**
     * Called to pull an item from an inventory at the lcoation
     *
     * @param position
     * @param count
     * @param side
     * @return
     */
    public static ItemStack pullStack(Location position, int count, EnumFacing side)
    {
        return pullStack(position.getTileEntity(), count, side);
    }

    /**
     * Called to pull an item from an inventory at the lcoation
     *
     * @param tile  - tile to access
     * @param count
     * @param side
     * @return
     */
    public static ItemStack pullStack(TileEntity tile, int count, EnumFacing side)
    {
        if (tile instanceof IInventory)
        {
            return takeTopItemFromInventory(checkChestInv((IInventory) tile), side, count);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Tries to place an item into the inventory. If the inventory is not an instance of {@link ISidedInventory} it
     * will ignore the side param.
     *
     * @param inventory - inventory to insert the item into
     * @param itemStack - stack to insert
     * @param side      - side to inser the item into (0-5)
     * @param force-    overrides {@link IInventory#isItemValidForSlot(int, ItemStack)} check
     * @return what is left of the toInsert stack
     */
    public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, EnumFacing side, boolean force)
    {
        ItemStack toInsert = !itemStack.isEmpty() ? itemStack.copy() : ItemStack.EMPTY;
        if (!toInsert.isEmpty())
        {
            if (!(inventory instanceof ISidedInventory))
            {
                return putStackInInventory(inventory, toInsert, force);
            }
            else
            {
                ISidedInventory sidedInventory = (ISidedInventory) inventory;
                int[] slots = sidedInventory.getSlotsForFace(side);

                if (slots != null && slots.length != 0)
                {
                    for (int get = 0; get < slots.length; get++)
                    {
                        int slotID = slots[get];

                        if (force || sidedInventory.isItemValidForSlot(slotID, toInsert) && sidedInventory.canInsertItem(slotID, toInsert, side))
                        {
                            ItemStack inSlot = inventory.getStackInSlot(slotID);

                            if (inSlot.isEmpty())
                            {
                                inventory.setInventorySlotContents(slotID, toInsert);
                                return ItemStack.EMPTY;
                            }
                            else if (inSlot.isItemEqual(toInsert) && inSlot.getCount() < inSlot.getMaxStackSize())
                            {
                                if (inSlot.getCount() + toInsert.getCount() <= inSlot.getMaxStackSize())
                                {
                                    ItemStack toSet = toInsert.copy();
                                    toSet.setCount(toSet.getCount() + inSlot.getCount());

                                    inventory.setInventorySlotContents(slotID, toSet);
                                    return ItemStack.EMPTY;
                                }
                                else
                                {
                                    int rejects = (inSlot.getCount() + toInsert.getCount()) - inSlot.getMaxStackSize();

                                    ItemStack toSet = toInsert.copy();
                                    toSet.setCount(inSlot.getMaxStackSize());

                                    ItemStack remains = toInsert.copy();
                                    remains.setCount(rejects);

                                    inventory.setInventorySlotContents(slotID, toSet);

                                    toInsert = remains;
                                }
                            }
                        }
                    }
                }
            }
        }
        return toInsert;

    }

    /**
     * Tries to place an item into the inventory. If the inventory is not an instance of {@link ISidedInventory} it
     * will ignore the side param.
     *
     * @param inventory - inventory to insert the item into
     * @param itemStack - stack to insert
     * @param slots     - slot ids to insert into
     * @param force     - overrides {@link IInventory#isItemValidForSlot(int, ItemStack)} check
     * @return what is left of the toInsert stack
     */
    public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int[] slots, boolean force)
    {
        ItemStack toInsert = !itemStack.isEmpty() ? itemStack.copy() : ItemStack.EMPTY;
        if (!toInsert.isEmpty())
        {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;

            for (int get = 0; get < slots.length; get++)
            {
                int slotID = slots[get];

                if (force || sidedInventory.isItemValidForSlot(slotID, toInsert))
                {
                    ItemStack inSlot = inventory.getStackInSlot(slotID);

                    if (inSlot.isEmpty())
                    {
                        inventory.setInventorySlotContents(slotID, toInsert);
                        return ItemStack.EMPTY;
                    }
                    else if (stacksMatch(inSlot, toInsert) && inSlot.getCount() < inSlot.getMaxStackSize())
                    {
                        if (inSlot.getCount() + toInsert.getCount() <= inSlot.getMaxStackSize())
                        {
                            ItemStack toSet = toInsert.copy();
                            toSet.setCount(toSet.getCount() + inSlot.getCount());

                            inventory.setInventorySlotContents(slotID, toSet);
                            return ItemStack.EMPTY;
                        }
                        else
                        {
                            int rejects = (inSlot.getCount() + toInsert.getCount()) - inSlot.getMaxStackSize();

                            ItemStack toSet = toInsert.copy();
                            toSet.setCount(inSlot.getMaxStackSize());

                            ItemStack remains = toInsert.copy();
                            remains.setCount(rejects);

                            inventory.setInventorySlotContents(slotID, toSet);

                            toInsert = remains;
                        }
                    }
                }
            }
        }
        return toInsert;
    }


    public static ItemStack takeTopItemFromInventory(IInventory inventory, EnumFacing side)
    {
        return takeTopItemFromInventory(inventory, side, 1);
    }

    /**
     * Pulls the top most item out of the inventory
     *
     * @param inventory - inventory to search, will use ISidedInventory if possible
     * @param side      - side to access
     * @param stackSize - stack size limit to pull, -1 will be maxx
     * @return item or null if none found
     */
    public static ItemStack takeTopItemFromInventory(IInventory inventory, EnumFacing side, int stackSize)
    {
        final Pair<ItemStack, Integer> result = findFirstItemInInventory(inventory, side, stackSize);
        if (result != null)
        {
            inventory.decrStackSize(result.right(), result.left().getCount());
            return result.left();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gets the first slot containing items
     * <p>
     * Does not actually consume the items
     *
     * @param inventory - inventory to search for items
     * @param side      - side to access, used for {@link ISidedInventory}
     * @param stackSize - amount to remove
     * @return pair containing the removed stack, and item
     */
    public static Pair<ItemStack, Integer> findFirstItemInInventory(IInventory inventory, EnumFacing side, int stackSize)
    {
        return findFirstItemInInventory(inventory, side, stackSize, null);
    }

    /**
     * Gets the first slot containing items ignoring ISided or filter settings
     * <p>
     * Does not actually consume the items
     *
     * @param inventory - inventory to search for items
     * @param stackSize - amount to remove
     * @return pair containing the removed stack, and item
     */
    public static Pair<ItemStack, Integer> findFirstItemInInventory(IInventory inventory, int stackSize)
    {
        return findFirstItemInInventory(inventory, null, stackSize);
    }

    /**
     * Gets the first slot containing items
     * <p>
     * Does not actually consume the items
     *
     * @param inventory - inventory to search for items
     * @param side      - side to access, used for {@link ISidedInventory}
     *                  If this value is not between 0-5 it will not use
     *                  ISideInventory and instead bypass the sided checks
     * @param stackSize - amount to remove
     * @return pair containing the removed stack, and item
     */
    public static Pair<ItemStack, Integer> findFirstItemInInventory(IInventory inventory, EnumFacing side, int stackSize, IInventoryFilter filter)
    {
        if (!(inventory instanceof ISidedInventory) || side == null)
        {
            for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
            {
                final ItemStack slotStack = inventory.getStackInSlot(i);
                if (slotStack != null && (filter == null || filter.isStackInFilter(slotStack)))
                {
                    int amountToTake = stackSize <= 0 ? slotStack.getMaxStackSize() : Math.min(stackSize, slotStack.getMaxStackSize());
                    amountToTake = Math.min(amountToTake, slotStack.getCount());

                    ItemStack toSend = slotStack.copy();
                    toSend.setCount(amountToTake);
                    //inventory.decrStackSize(i, amountToTake);
                    return new Pair(toSend, i);
                }
            }
        }
        else
        {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            int[] slots = sidedInventory.getSlotsForFace(side);

            if (slots != null)
            {
                for (int get = slots.length - 1; get >= 0; get--)
                {
                    int slotID = slots[get];
                    final ItemStack slotStack = sidedInventory.getStackInSlot(slotID);
                    if (!slotStack.isEmpty() && (filter == null || filter.isStackInFilter(slotStack)))
                    {
                        int amountToTake = stackSize <= 0 ? slotStack.getMaxStackSize() : Math.min(stackSize, slotStack.getMaxStackSize());
                        amountToTake = Math.min(amountToTake, slotStack.getCount());

                        ItemStack toSend = slotStack.copy();
                        toSend.setCount(amountToTake);

                        if (sidedInventory.canExtractItem(slotID, toSend, side))
                        {
                            //sidedInventory.decrStackSize(slotID, amountToTake);
                            return new Pair(toSend, slotID);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static ItemStack takeTopBlockFromInventory(IInventory inventory, EnumFacing side)
    {
        if (!(inventory instanceof ISidedInventory))
        {
            for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
            {
                if (!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() instanceof ItemBlock)
                {
                    ItemStack toSend = inventory.getStackInSlot(i).copy();
                    toSend.setCount(1);

                    inventory.decrStackSize(i, 1);

                    return toSend;
                }
            }
        }
        else
        {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            int[] slots = sidedInventory.getSlotsForFace(side);

            if (slots != null)
            {
                for (int get = slots.length - 1; get >= 0; get--)
                {
                    int slotID = slots[get];

                    if (!sidedInventory.getStackInSlot(slotID).isEmpty() && inventory.getStackInSlot(slotID).getItem() instanceof ItemBlock)
                    {
                        ItemStack toSend = sidedInventory.getStackInSlot(slotID);
                        toSend.setCount(1);

                        if (sidedInventory.canExtractItem(slotID, toSend, side))
                        {
                            sidedInventory.decrStackSize(slotID, 1);

                            return toSend;
                        }
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static List<EntityItem> dropBlockAsItem(IWorldPosition position)
    {
        return dropBlockAsItem(position, false);
    }

    public static List<EntityItem> dropBlockAsItem(IWorldPosition position, boolean destroy)
    {
        return dropBlockAsItem(position.oldWorld(), new BlockPos(position.xi(), position.yi(), position.zi()), destroy);
    }

    public static List<EntityItem> dropBlockAsItem(World world, Pos position)
    {
        return dropBlockAsItem(world, position.toBlockPos(), false);
    }

    /**
     * Attempts to drop the block at the location as an item. Does not check what the block is
     * and can fail if the block doesn't contain items.
     *
     * @param world
     * @param pos
     * @param destroy - will break the block
     */
    public static List<EntityItem> dropBlockAsItem(World world, BlockPos pos, boolean destroy)
    {
        List<EntityItem> entities = new ArrayList();
        if (!world.isRemote)
        {
            IBlockState state = world.getBlockState(pos);

            if (state != null && state.getBlock() != Blocks.AIR)
            {
                List<ItemStack> items = state.getBlock().getDrops(world, pos, state, 0);

                for (ItemStack itemStack : items)
                {
                    EntityItem entityItem = dropItemStack(world, new Pos(pos), itemStack, 10);
                    if (entityItem != null)
                    {
                        entities.add(entityItem);
                    }
                }
            }
            if (destroy)
            {
                world.setBlockToAir(pos);
            }
        }
        return entities;
    }

    public static EntityItem dropItemStack(IWorldPosition position, ItemStack itemStack)
    {
        return dropItemStack(position.oldWorld(), position.x(), position.y(), position.z(), itemStack, 10, 0f);
    }

    /**
     * Drops an item stack on the floor.
     */
    public static EntityItem dropItemStack(World world, IPos3D position, ItemStack itemStack)
    {
        return dropItemStack(world, position, itemStack, 10);
    }

    public static EntityItem dropItemStack(World world, IPos3D position, ItemStack itemStack, int delay)
    {
        return dropItemStack(world, position, itemStack, delay, 0f);
    }

    public static EntityItem dropItemStack(World world, IPos3D position, ItemStack itemStack, int delay, float randomAmount)
    {
        return dropItemStack(world, position.x(), position.y(), position.z(), itemStack, delay, randomAmount);
    }

    public static EntityItem dropItemStack(World world, double x, double y, double z, ItemStack itemStack, int delay, float randomAmount)
    {
        //TODO fire drop events if not already done by forge
        //TODO add banned item filtering, prevent creative mode only items from being dropped
        if (world != null && !world.isRemote && !itemStack.isEmpty())
        {
            double randomX = 0;
            double randomY = 0;
            double randomZ = 0;

            if (randomAmount > 0)
            {
                randomX = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomY = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
                randomZ = world.rand.nextFloat() * randomAmount + (1.0F - randomAmount) * 0.5D;
            }

            EntityItem entityitem = new EntityItem(world, x + randomX, y + randomY, z + randomZ, itemStack);

            if (randomAmount <= 0)
            {
                entityitem.motionX = 0;
                entityitem.motionY = 0;
                entityitem.motionZ = 0;
            }

            if (itemStack.hasTagCompound())
            {
                entityitem.getItem().setTagCompound(itemStack.getTagCompound().copy());
            }

            entityitem.setPickupDelay(delay);
            world.spawnEntity(entityitem);
            return entityitem;
        }
        return null;
    }

    /**
     * Decreases the stack by a set amount
     *
     * @param stack  - starting stack
     * @param amount - amount of items
     * @return the edited stack
     */
    public static ItemStack decrStackSize(ItemStack stack, int amount)
    {
        if (!stack.isEmpty())
        {
            ItemStack itemStack = stack.copy();
            if (itemStack.getCount() <= amount)
            {
                return ItemStack.EMPTY;
            }
            else
            {
                itemStack.setCount(itemStack.getCount() - amount);

                if (itemStack.getCount() <= 0)
                {
                    return ItemStack.EMPTY;
                }
                return itemStack;
            }
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Called to consume an ItemStack in a way that is mod supported. This mainly just allows fluid
     * items to return empty versions. For example a lava bucket will be consumed turned into an
     * empty bucket. This version of consume will consume the item held in the player's hand.
     */
    public static void consumeHeldItem(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            ItemStack stack = player.inventory.getCurrentItem();
            if (player != null && !stack.isEmpty())
            {
                stack = stack.copy();
                if (stack.getItem().hasContainerItem(stack))
                {
                    if (stack.getCount() == 1)
                    {
                        stack = stack.getItem().getContainerItem(stack);
                    }
                    else
                    {
                        player.inventory.addItemStackToInventory(stack.getItem().getContainerItem(stack.splitStack(1)));
                    }
                }
                else
                {
                    if (stack.getCount() == 1)
                    {
                        stack = ItemStack.EMPTY;
                    }
                    else
                    {
                        stack.splitStack(1);
                    }
                }
                player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
            }
        }
    }

    /**
     * Called to consume an ItemStack in a way that is mod supported. This mainly just allows fluid
     * items to return empty versions. For example a lava bucket will be consumed turned into an
     * empty bucket.
     */
    public static ItemStack consumeStack(ItemStack stack)
    {
        if (stack.getCount() == 1)
        {
            if (stack.getItem().hasContainerItem(stack))
            {
                return stack.getItem().getContainerItem(stack);
            }
        }
        else
        {
            return stack.splitStack(1);
        }
        return null;
    }

    /**
     * Checks if the two item stacks match each other exactly. Item, meta, stacksize, nbt
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatchExact(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB) && stackA.getCount() == stackB.getCount();
        }
        return stackA.isEmpty() && stackB.isEmpty();
    }

    /**
     * Checks if two item stacks match each other using item, meta, and nbt to compare
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatch(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB);
        }
        return stackA.isEmpty() && stackB.isEmpty();
    }

    /**
     * Checks if two stacks match each other using item, meta, and nbt to compare. If
     * this fails then it attempts to use the object's OreDictionary name to match.
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return true if they match
     */
    public static boolean stacksMatchWithOreNames(ItemStack stackA, ItemStack stackB)
    {
        if (stacksMatch(stackA, stackB))
        {
            return true;
        }
        return stacksMatchWithOreNames2(stackA, stackB) != null;
    }

    /**
     * Compares two stack with each other using ore names.
     *
     * @param stackA - item stack a
     * @param stackB - item stack a
     * @return matched ore name
     */
    public static String stacksMatchWithOreNames2(ItemStack stackA, ItemStack stackB)
    {
        if (!stackA.isEmpty() && !stackB.isEmpty())
        {
            //TODO this might be a bad idea if an item has a lot of ids
            List<Integer> a = new ArrayList();
            for (int i : OreDictionary.getOreIDs(stackA))
            {
                a.add(i);
            }
            for (int i : OreDictionary.getOreIDs(stackB))
            {
                if (a.contains(i))
                {
                    return OreDictionary.getOreName(i);
                }
            }
        }
        return null;
    }

    /**
     * Checks if two itemStack's nbt matches exactly. Does not check item, stacksize, or damage value.
     *
     * @param stackA - item stack a, can't be null
     * @param stackB - item stack a, can't be null
     * @return true if the stack's nbt matches
     */
    public static boolean doesStackNBTMatch(ItemStack stackA, ItemStack stackB)
    {
        return NBTUtility.doTagsMatch(stackA.getTagCompound(), stackB.getTagCompound());
    }

    /**
     * Checks to see how many of the item are in the inventory.
     *
     * @param stack - stack to check against, ignores stacksize
     * @param inv   - inventory
     * @param slots - slots to checks, if null defaults to entire inventory
     * @return count of items using the stacksize of each itemstack found
     */
    public static int getStackCount(ItemStack stack, IInventory inv, int[] slots)
    {
        int count = 0;

        if (stack != null)
        {
            List<Integer> slot_list = new ArrayList<>();

            if (slots != null & slots.length > 0)
            {
                for (int i = 0; i < slots.length; i++)
                {
                    slot_list.add(slots[i]);
                }
            }

            for (int slot = 0; slot < inv.getSizeInventory(); slot++)
            {
                if (slot_list.isEmpty() || slot_list.contains(slot))
                {
                    if (!inv.getStackInSlot(slot).isEmpty() && inv.getStackInSlot(slot).isItemEqual(stack))
                    {
                        count += inv.getStackInSlot(slot).getCount();
                    }
                }
            }
        }

        return count;
    }

    public static int getStackCount(Class<?> compare, IInventory inv)
    {
        return getStackCount(compare, inv);
    }

    public static int getStackCount(Class<?> compare, IInventory inv, int[] slots)
    {
        int count = 0;

        if (compare != null)
        {
            List<Integer> slot_list = new ArrayList<>();

            if (slots != null & slots.length > 0)
            {
                for (int i = 0; i < slots.length; i++)
                {
                    slot_list.add(slots[i]);
                }
            }

            for (int slot = 0; slot < inv.getSizeInventory(); slot++)
            {
                if (slot_list.isEmpty() || slot_list.contains(slot))
                {
                    if (!inv.getStackInSlot(slot).isEmpty() && compare.isInstance(inv.getStackInSlot(slot).getItem()))
                    {
                        count += inv.getStackInSlot(slot).getCount();
                    }
                }
            }
        }

        return count;
    }

    public static ArrayList getAllItemsInPlayerInventory(EntityPlayer entity)
    {
        ArrayList<ItemStack> itemsToDrop = new ArrayList();
        for (int slot = 0; slot < entity.inventory.mainInventory.size(); slot++)
        {
            if (!entity.inventory.mainInventory.get(slot).isEmpty())
            {
                itemsToDrop.add(entity.inventory.mainInventory.get(slot));
            }
        }
        for (int slot = 0; slot < entity.inventory.armorInventory.size(); slot++)
        {
            if (!entity.inventory.armorInventory.get(slot).isEmpty())
            {
                itemsToDrop.add(entity.inventory.armorInventory.get(slot));
            }
        }
        return itemsToDrop;
    }

    /**
     * Called to handle a slot based input and output section. This handler will
     * to place the item into the slot connected with the id. If
     * it can't then it will try to remove an item from the slot.
     *
     * @param player - player who is accessing the inventory slot
     * @param inv    - inventory to access the slot from
     * @param slot   - slot ID to access
     * @return true if something happened false if nothing happened.
     */
    public static boolean handleSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot)
    {
        return handleSlot(player, inv, hand, slot, -1);
    }

    /**
     * Called to handle a slot based input and output section. This handler will
     * to place the item into the slot connected with the id. If
     * it can't then it will try to remove an item from the slot.
     *
     * @param player - player who is accessing the inventory slot
     * @param inv    - inventory to access the slot from
     * @param slot   - slot ID to access
     * @return true if something happened false if nothing happened.
     */
    public static boolean handleSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot, int items)
    {
        if (player != null && inv != null && slot >= 0 && slot < inv.getSizeInventory())
        {
            if (!addItemToSlot(player, inv, hand, slot, items))
            {
                return removeItemFromSlot(player, inv, hand, slot, items);
            }
        }
        return false;
    }

    /**
     * Called to add the item the player is holding into the slot
     *
     * @param player - player who is accessing the item
     * @param slot   - slot to place the item into
     * @return true if the item was added, false if nothing happended
     */
    public static boolean addItemToSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot)
    {
        return addItemToSlot(player, inv, hand, slot, -1);
    }

    /**
     * Called to add the item the player is holding into the slot
     *
     * @param player - player who is accessing the item
     * @param slot   - slot to place the item into
     * @return true if the item was added, false if nothing happended
     */
    public static boolean addItemToSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot, int items)
    {
        //Check if input is valid
        if (!player.getHeldItem(hand).isEmpty() && inv.isItemValidForSlot(slot, player.getHeldItem(hand)))
        {
            //Only can add items if slot is empty or matches input
            if (inv.getStackInSlot(slot).isEmpty() || stacksMatch(player.getHeldItem(hand), inv.getStackInSlot(slot)))
            {
                //Find out how much space we have left
                int roomLeftInSlot = roomLeftInSlotForStack(inv, player.getHeldItem(hand), slot);
                //Find out how many items to add to slot
                int itemsToAdd = Math.min(roomLeftInSlot, Math.min(player.getHeldItem(hand).getCount(), items == -1 ? roomLeftInSlot : items));
                //Add items already in slot since we are going to set the slot
                if (!inv.getStackInSlot(slot).isEmpty())
                {
                    itemsToAdd += inv.getStackInSlot(slot).getCount();
                }

                inv.setInventorySlotContents(slot, player.getHeldItem(hand).copy());
                inv.getStackInSlot(slot).setCount(itemsToAdd);

                //Ignore creative mode
                if (!player.capabilities.isCreativeMode)
                {
                    player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - itemsToAdd);
                    if (player.getHeldItem(hand).getCount() <= 0)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                    }
                    player.inventoryContainer.detectAndSendChanges();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds how much space is left in the inventory slot
     *
     * @param inv  - inventory to check, can't be null
     * @param slot - slot to check, needs to be a valid slot as it's not checked
     * @return amount of room left in the slot
     */
    public static int roomLeftInSlot(IInventory inv, int slot)
    {
        if (!inv.getStackInSlot(slot).isEmpty())
        {
            int maxSpace = Math.min(inv.getStackInSlot(slot).getMaxStackSize(), inv.getInventoryStackLimit());
            return maxSpace - inv.getStackInSlot(slot).getCount();
        }
        return inv.getInventoryStackLimit();
    }

    /**
     * Gets the room left in the stack
     *
     * @param stack - stack to check, can't be null
     * @return amount of room left
     */
    public static int roomLeftInStack(ItemStack stack)
    {
        return stack.getMaxStackSize() - stack.getCount();
    }

    /**
     * Checks how much space is left in the inventory for the stack
     *
     * @param inv   - inventory to check, can't be null
     * @param stack - stack to check, can't be null
     * @param slot  - slot to check, needs to be valid as not checked
     * @return amount of room left
     */
    public static int roomLeftInSlotForStack(IInventory inv, ItemStack stack, int slot)
    {
        int maxSpace = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        if (!inv.getStackInSlot(slot).isEmpty())
        {
            return maxSpace - inv.getStackInSlot(slot).getCount();
        }
        return maxSpace;
    }

    /**
     * Checks how much space is left in the player's held hand.
     * <p>
     * Does check again player's inventory limit in case
     * another mod changes it. Should prevent issues
     * with this method in inventory overhaul mods.
     *
     * @param player - player to check, can't be null
     * @return amount of room left
     */
    public static int spaceInPlayersHand(EntityPlayer player, EnumHand hand)
    {
        return player.getHeldItem(hand).isEmpty() ? player.inventory.getInventoryStackLimit() : Math.min(player.inventory.getInventoryStackLimit(), player.getHeldItem(hand).getMaxStackSize()) - player.getHeldItem(hand).getCount();
    }

    /**
     * Removes items from a slot and tries to place them into the player's inventory.
     * If the items can't be placed into the inventory they are dropped.
     *
     * @param player - player accessing the inventory
     * @param inv    - inventory being accessed
     * @param slot   - slot being accessed
     * @return true if items were removed, false if nothing happened
     */
    public static boolean removeItemFromSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot)
    {
        return removeItemFromSlot(player, inv, hand, slot, -1);
    }

    /**
     * Removes items from a slot and tries to place them into the player's inventory.
     * If the items can't be placed into the inventory they are dropped.
     *
     * @param player - player accessing the inventory
     * @param inv    - inventory being accessed
     * @param slot   - slot being accessed
     * @param items  - number of items being removed
     * @return true if items were removed, false if nothing happened
     */
    public static boolean removeItemFromSlot(EntityPlayer player, IInventory inv, EnumHand hand, int slot, int items)
    {
        if (!inv.getStackInSlot(slot).isEmpty() && items >= -1 && items != 0)
        {
            int spaceInHand = spaceInPlayersHand(player, hand);
            int itemsToMove = Math.min(Math.min(spaceInHand, inv.getStackInSlot(slot).getMaxStackSize()), items == -1 ? inv.getInventoryStackLimit() : items);

            //Create clone of slot stack, only not used in one use case
            ItemStack stack = inv.getStackInSlot(slot).copy();
            stack.setCount(itemsToMove);

            //Moves items to player
            if (player.getHeldItem(hand).isEmpty())
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
            }
            else if (spaceInHand > 0)
            {
                player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() + itemsToMove);
            }
            else if (!player.inventory.addItemStackToInventory(stack))
            {
                InventoryUtility.dropItemStack(new Location(player), stack);
            }

            //Remove items from slot
            if (itemsToMove >= inv.getStackInSlot(slot).getCount())
            {
                inv.setInventorySlotContents(slot, ItemStack.EMPTY);
            }
            else
            {
                inv.getStackInSlot(slot).setCount(player.getHeldItem(hand).getCount() + itemsToMove);
            }

            player.inventoryContainer.detectAndSendChanges();
            return true;
        }
        return false;
    }


    /**
     * Used to get the number of metal armor peices an entity is
     * wearing
     * Supports {@link EntityLiving} and {@link EntityPlayer} inventories
     * fully.
     * <p>
     * Supports {@link EntityLivingBase} held item only
     *
     * @param entity
     * @return
     */
    public static int getWornMetalCount(Entity entity)
    {
        int c = 0;
        if (entity instanceof EntityPlayer)
        {
            for (final ItemStack stack : ((EntityPlayer) entity).inventory.armorInventory)
            {
                if (stack.getItem() instanceof ItemArmor)
                {
                    final ItemArmor.ArmorMaterial mat = ((ItemArmor) stack.getItem()).getArmorMaterial();
                    if (mat != ItemArmor.ArmorMaterial.LEATHER && mat != ItemArmor.ArmorMaterial.DIAMOND)
                    {
                        c += 1;
                    }
                }
            }
        }
        else if (entity instanceof EntityCreature)
        {
            //Armor is stored in slots 1 - 4, 0 is held item and is taken care of by EntityLivingBase check
            Iterator<ItemStack> it = entity.getArmorInventoryList().iterator();
            while (it.hasNext())
            {
                final ItemStack stack = it.next();
                if (stack.getItem() instanceof ItemArmor)
                {
                    final ItemArmor.ArmorMaterial mat = ((ItemArmor) stack.getItem()).getArmorMaterial();
                    if (mat != ItemArmor.ArmorMaterial.LEATHER && mat != ItemArmor.ArmorMaterial.DIAMOND)
                    {
                        c += 1;
                    }
                }
            }
        }

        if(entity instanceof EntityLiving)
        {
            if (isHeldItemMetal((EntityLiving) entity, EnumHand.MAIN_HAND))
            {
                c += 1;
            }
            if (isHeldItemMetal((EntityLiving) entity, EnumHand.OFF_HAND))
            {
                c += 1;
            }
        }

        return c;
    }

    public static boolean isHeldItemMetal(EntityLiving entity, EnumHand hand)
    {
        //TODO convert to cache\
        if (!entity.getHeldItem(hand).isEmpty())
        {
            //TODO make a dictionary of material to item types
            ItemStack held =  entity.getHeldItem(hand);
            Item heldItem = held.getItem();
            if (heldItem instanceof ItemSword)
            {
                String mat = ((ItemSword) heldItem).getToolMaterialName();
                if (mat.equalsIgnoreCase("iron") || mat.equalsIgnoreCase("gold"))
                {
                    return true;
                }
            }
            else if (heldItem instanceof ItemTool)
            {
                String mat = ((ItemTool) heldItem).getToolMaterialName();
                if (mat.equalsIgnoreCase("iron") || mat.equalsIgnoreCase("gold"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets all slots that are contain items
     *
     * @param inventory - inventory to search
     * @return array list of slots
     */
    public static ArrayList<Integer> getFilledSlots(IInventory inventory)
    {
        ArrayList<Integer> slots = new ArrayList();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (!inventory.getStackInSlot(slot).isEmpty())
            {
                slots.add(slot);
            }
        }
        return slots;
    }

    /**
     * Gets all slots that are completely empty
     *
     * @param inventory - inventory to search
     * @return array list of slots
     */
    public static ArrayList<Integer> getEmptySlots(IInventory inventory)
    {
        ArrayList<Integer> slots = new ArrayList();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (inventory.getStackInSlot(slot).isEmpty())
            {
                slots.add(slot);
            }
        }
        return slots;
    }

    /**
     * Gets all slots that are have room for inserting items
     *
     * @param inventory - inventory to search
     * @return array list of slots
     */
    public static ArrayList<Integer> getSlotsWithSpace(IInventory inventory)
    {
        ArrayList<Integer> slots = new ArrayList();
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            if (roomLeftInSlot(inventory, slot) > 0)
            {
                slots.add(slot);
            }
        }
        return slots;
    }
}
