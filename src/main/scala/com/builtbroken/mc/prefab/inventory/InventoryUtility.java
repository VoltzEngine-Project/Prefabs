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
import com.builtbroken.mc.lib.helper.DummyPlayer;
import com.builtbroken.mc.lib.helper.NBTUtility;
import com.google.common.collect.Table;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.*;

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
        FMLControlledNamespacedRegistry<Item> registry = (FMLControlledNamespacedRegistry<Item>) Item.itemRegistry;
        Set set = registry.getKeys();

        Engine.logger().info("  " + set.size() + " entries detected");
        //Loop all entries
        for (Object obj : set)
        {
            if (obj instanceof String)
            {
                String name = (String) obj;
                String modID = name.substring(0, name.indexOf(":"));
                name = name.substring(name.indexOf(":") + 1, name.length()).toLowerCase();

                Object entry = registry.getObject(obj);
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

    public static Map<String, List<Item>> getModToItems()
    {
        return MOD_TO_ITEMS;
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
        FMLControlledNamespacedRegistry<Item> registry = (FMLControlledNamespacedRegistry<Item>) Item.itemRegistry;
        if (Item.itemRegistry.containsKey(name))
        {
            return registry.getObject(name);
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
     * Attempts to use the {@link GameData#customItemStacks} to
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
            is.stackSize = 1;
        }
        return is;
    }

    /**
     * Gets the mod id for the stack
     *
     * @param stack
     * @return
     */
    public static String getModID(ItemStack stack)
    {
        if (stack != null && stack.getItem() != null)
        {
            String regName = Item.itemRegistry.getNameForObject(stack.getItem());
            if (regName != null)
            {
                return regName.split(":")[0];
            }
        }
        return "null";
    }

    /**
     * Gets the display name to use when showing an item
     * <p>
     * Will attempt to use the stacks disply name, then unlocalized, and
     * finally item localzied name. If these fail it will default to unlocalzied name of the item
     *
     * @param stack
     * @return
     */
    public static String getDisplayName(ItemStack stack)
    {
        if (stack != null)
        {
            if (stack.getItem() != null)
            {
                final String regName = Item.itemRegistry.getNameForObject(stack.getItem());
                final String name = regName.split(":")[1];

                for (String value : new String[]{stack.getDisplayName(), stack.getUnlocalizedName(), name, StatCollector.translateToLocal(stack.getItem().getUnlocalizedName()) + " #" + stack.getItemDamage()})
                {
                    if (value != null)
                    {
                        value = value.trim();
                        if (!value.isEmpty() && !value.toLowerCase().startsWith("null"))
                        {
                            return value;
                        }
                    }
                }
                return stack.getItem().getUnlocalizedName() + "@" + stack.getItemDamage();
            }
            return "null item";
        }
        return "null";
    }

    /**
     * Gets the registry name for the item
     *
     * @param item
     * @return
     */
    public static String getRegistryName(ItemStack item)
    {
        return item != null ? getRegistryName(item.getItem()) : null;
    }

    /**
     * Gets the registry name for the item
     *
     * @param item
     * @return
     */
    public static String getRegistryName(Item item)
    {
        if (item != null)
        {
            return Item.itemRegistry.getNameForObject(item);
        }
        return null;
    }

    /**
     * Gets the registry name for the block
     *
     * @param block
     * @return
     */
    public static String getRegistryName(Block block)
    {
        if (block != null)
        {
            return Block.blockRegistry.getNameForObject(block);
        }
        return null;
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

        Engine.logger().info("Mapping basic recipe data...");
        Engine.logger().info("   " + CraftingManager.getInstance().getRecipeList().size() + " recipes detected.");

        long time = System.nanoTime();

        for (Object r : CraftingManager.getInstance().getRecipeList())
        {
            if (r instanceof IRecipe && ((IRecipe) r).getRecipeOutput() != null)
            {
                ItemStackWrapper wrapper = new ItemStackWrapper(((IRecipe) r).getRecipeOutput());

                //Update stack recipe list
                List<IRecipe> list = ITEMSTACK_TO_RECIPES.get(wrapper);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add((IRecipe) r);
                ITEMSTACK_TO_RECIPES.put(wrapper, list);

                //Update item recipe list
                Item item = ((IRecipe) r).getRecipeOutput().getItem();
                list = ITEM_TO_RECIPES.get(item);
                if (list == null)
                {
                    list = new ArrayList();
                }
                list.add((IRecipe) r);
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
        stack1.stackSize = stackSize;
        return stack1;
    }

    /**
     * Places the stack into the inventory in the first slot it can find
     *
     * @param inventory            - inventory to scan
     * @param stackToInsert        - stack to insert into the inventory
     * @param ignoreIsValidForSlot - ignores the {@link IInventory#isItemValidForSlot(int, ItemStack)} check on
     *                             inventories. Not normally used but is here just in case it is needed. Expected use
     *                             case is to bypass automation checks for NPCs or other special cases. In which a player
     *                             can normally insert into the slot but the API has a different result.
     * @return remaining stack, null if consumed
     */
    public static ItemStack putStackInInventory(IInventory inventory, ItemStack stackToInsert, boolean ignoreIsValidForSlot)
    {
        //Work around for chests having a shared inventory
        if (inventory instanceof TileEntityChest)
        {
            inventory = checkChestInv(inventory);
        }

        //Loop all slots
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
        {
            //Ensure slot is valid
            if (ignoreIsValidForSlot || inventory.isItemValidForSlot(slot, stackToInsert))
            {
                //Attempt to insert into slot
                stackToInsert = putStackInSlot(inventory, stackToInsert, slot);

                //Stop if stack has been consumed
                if (stackToInsert == null || stackToInsert.stackSize <= 0)
                {
                    return null;
                }
            }
        }
        return stackToInsert;
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
    public static ItemStack insertStack(Location position, ItemStack toInsert, int side, boolean force)
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
    public static ItemStack insertStack(TileEntity tile, ItemStack toInsert, int side, boolean force)
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
    public static ItemStack pullStack(Location position, int count, int side)
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
    public static ItemStack pullStack(TileEntity tile, int count, int side)
    {
        if (tile instanceof IInventory)
        {
            return takeTopItemFromInventory(checkChestInv((IInventory) tile), count, side);
        }
        return null;
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
    public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side, boolean force)
    {
        ItemStack stackToInsert = itemStack != null ? itemStack.copy() : null;
        if (stackToInsert != null)
        {
            //Run slot loop directly
            if (!(inventory instanceof ISidedInventory))
            {
                return putStackInInventory(inventory, stackToInsert, force);
            }
            //Do sided check before looping slots
            else
            {
                //inventory
                final ISidedInventory sidedInventory = (ISidedInventory) inventory;

                //Get slots for side
                final int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

                //Ensure we have slots to use
                if (slots != null && slots.length != 0)
                {
                    //Loop slots
                    for (int slotID : slots)
                    {
                        //Ensure we can insert the item
                        if (force || sidedInventory.isItemValidForSlot(slotID, stackToInsert) && sidedInventory.canInsertItem(slotID, stackToInsert, side))
                        {
                            //Attempt to insert into slot
                            stackToInsert = putStackInSlot(inventory, stackToInsert, slotID);

                            //Stop if stack has been consumed
                            if (stackToInsert == null || stackToInsert.stackSize <= 0)
                            {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return stackToInsert;

    }

    /**
     * Called to insert a stack into a slot
     *
     * @param inventory     - inventory to access
     * @param stackToInsert - stack to insert
     * @param slotID        - slot to access
     * @return what remains of the stack, null if consumed fully
     */
    public static ItemStack putStackInSlot(IInventory inventory, ItemStack stackToInsert, int slotID)
    {
        //Get stack in slot
        ItemStack stackInSlot = inventory.getStackInSlot(slotID);

        //If null just insert item
        if (stackInSlot == null)
        {
            inventory.setInventorySlotContents(slotID, stackToInsert);
            return null;
        }
        //If not null, ensure items match
        else if (stacksMatch(stackInSlot, stackToInsert))
        {
            int room = roomLeftInSlotForStack(inventory, stackToInsert, slotID);
            if (room >= stackToInsert.stackSize)
            {
                //Update slot stack size
                stackInSlot.stackSize += stackToInsert.stackSize;

                //Trigger inventory update
                inventory.setInventorySlotContents(slotID, stackInSlot);

                //Done, stack consumed
                return null;
            }
            else if (room > 0)
            {
                //Update slot stack size
                stackInSlot.stackSize += room;

                //Update insert stack size
                stackToInsert.stackSize -= room;

                //Trigger inventory update
                inventory.setInventorySlotContents(slotID, stackInSlot);
            }
        }
        return stackToInsert;
    }

    public static ItemStack takeTopItemFromInventory(IInventory inventory, int side)
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
    public static ItemStack takeTopItemFromInventory(IInventory inventory, int side, int stackSize)
    {
        final Pair<ItemStack, Integer> result = findFirstItemInInventory(inventory, side, stackSize);
        if (result != null)
        {
            inventory.decrStackSize(result.right(), result.left().stackSize);
            return result.left();
        }
        return null;
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
    public static Pair<ItemStack, Integer> findFirstItemInInventory(IInventory inventory, int side, int stackSize)
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
        return findFirstItemInInventory(inventory, -1, stackSize);
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
    public static Pair<ItemStack, Integer> findFirstItemInInventory(IInventory inventory, int side, int stackSize, IInventoryFilter filter)
    {
        if (!(inventory instanceof ISidedInventory) || side == -1 || side > 5)
        {
            for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
            {
                final ItemStack slotStack = inventory.getStackInSlot(i);
                if (slotStack != null && (filter == null || filter.isStackInFilter(slotStack)))
                {
                    int amountToTake = stackSize <= 0 ? slotStack.getMaxStackSize() : Math.min(stackSize, slotStack.getMaxStackSize());
                    amountToTake = Math.min(amountToTake, slotStack.stackSize);

                    ItemStack toSend = slotStack.copy();
                    toSend.stackSize = amountToTake;
                    //inventory.decrStackSize(i, amountToTake);
                    return new Pair(toSend, i);
                }
            }
        }
        else
        {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

            if (slots != null)
            {
                for (int get = slots.length - 1; get >= 0; get--)
                {
                    int slotID = slots[get];
                    final ItemStack slotStack = sidedInventory.getStackInSlot(slotID);
                    if (slotStack != null && (filter == null || filter.isStackInFilter(slotStack)))
                    {
                        int amountToTake = stackSize <= 0 ? slotStack.getMaxStackSize() : Math.min(stackSize, slotStack.getMaxStackSize());
                        amountToTake = Math.min(amountToTake, slotStack.stackSize);

                        ItemStack toSend = slotStack.copy();
                        toSend.stackSize = amountToTake;

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

    public static ItemStack takeTopBlockFromInventory(IInventory inventory, int side)
    {
        if (!(inventory instanceof ISidedInventory))
        {
            for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
            {
                if (inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).getItem() instanceof ItemBlock)
                {
                    ItemStack toSend = inventory.getStackInSlot(i).copy();
                    toSend.stackSize = 1;

                    inventory.decrStackSize(i, 1);

                    return toSend;
                }
            }
        }
        else
        {
            ISidedInventory sidedInventory = (ISidedInventory) inventory;
            int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

            if (slots != null)
            {
                for (int get = slots.length - 1; get >= 0; get--)
                {
                    int slotID = slots[get];

                    if (sidedInventory.getStackInSlot(slotID) != null && inventory.getStackInSlot(slotID).getItem() instanceof ItemBlock)
                    {
                        ItemStack toSend = sidedInventory.getStackInSlot(slotID);
                        toSend.stackSize = 1;

                        if (sidedInventory.canExtractItem(slotID, toSend, side))
                        {
                            sidedInventory.decrStackSize(slotID, 1);

                            return toSend;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static List<EntityItem> dropBlockAsItem(IWorldPosition position)
    {
        return dropBlockAsItem(position.oldWorld(), position.xi(), position.yi(), position.zi(), false);
    }

    public static List<EntityItem> dropBlockAsItem(IWorldPosition position, boolean destroy)
    {
        return dropBlockAsItem(position.oldWorld(), position.xi(), position.yi(), position.zi(), destroy);
    }

    public static List<EntityItem> dropBlockAsItem(World world, Pos position)
    {
        return dropBlockAsItem(world, position.xi(), position.yi(), position.zi(), false);
    }

    /**
     * Attempts to drop the block at the location as an item. Does not check what the block is
     * and can fail if the block doesn't contain items.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param destroy - will break the block
     */
    public static List<EntityItem> dropBlockAsItem(World world, int x, int y, int z, boolean destroy)
    {
        List<EntityItem> entities = new ArrayList();
        if (!world.isRemote)
        {
            Block block = world.getBlock(x, y, z);

            int meta = world.getBlockMetadata(x, y, z);
            if (block != null)
            {
                ArrayList<ItemStack> items = block.getDrops(world, x, y, z, meta, 0);

                for (ItemStack itemStack : items)
                {
                    EntityItem entityItem = dropItemStack(world, new Pos(x, y, z), itemStack, 10);
                    if (entityItem != null)
                    {
                        entities.add(entityItem);
                    }
                }
            }
            if (destroy)
            {
                world.setBlockToAir(x, y, z);
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
        if (world != null && !world.isRemote && itemStack != null)
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
                entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
            }

            entityitem.delayBeforeCanPickup = delay;
            world.spawnEntityInWorld(entityitem);
            return entityitem;
        }
        return null;
    }

    /**
     * Tries to place the item stack into the world as a block.
     *
     * @param world     - world
     * @param x         - the x-Coordinate of desired placement
     * @param y         - the y-Coordinate of desired placement
     * @param z         - the z-Coordinate of desired placement
     * @param itemStack - itemStack, should be an ItemBlock or something that can be placed
     * @param side      - the side we are trying to place on within this block space.
     * @return true if the block was created from the item, or other words placed into the world. If
     * the stack is null, if its not valid, or there is no room it returns false.
     */
    public static boolean placeItemBlock(World world, int x, int y, int z, ItemStack itemStack, int side)
    {
        //TODO implement support for micro blocks
        if (itemStack != null)
        {
            try
            {
                Pos rightClickPos = new Pos(x, y, z);

                if (world.isAirBlock(x, y, z))
                {
                    rightClickPos.add(ForgeDirection.getOrientation(side));
                }

                side ^= 1;
                return DummyPlayer.useItemAt(itemStack, world, x, y - 1, z, side);
            }
            catch (Exception e)
            {
                e.printStackTrace();

                if (world.getBlock(x, y, z) == ((ItemBlock) itemStack.getItem()).field_150939_a)
                {
                    return true;
                }
            }
        }

        return false;
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
        if (stack != null)
        {
            ItemStack itemStack = stack.copy();
            if (itemStack.stackSize <= amount)
            {
                return null;
            }
            else
            {
                itemStack.stackSize -= amount;

                if (itemStack.stackSize <= 0)
                {
                    return null;
                }
                return itemStack;
            }
        }
        else
        {
            return null;
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
            if (player != null && stack != null)
            {
                stack = stack.copy();
                if (stack.getItem().hasContainerItem(stack))
                {
                    if (stack.stackSize == 1)
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
                    if (stack.stackSize == 1)
                    {
                        stack = null;
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
     * Consumes the item in hand and then returns an empty bucket. Assumes that you
     * have already checked the item is a bucket. As well that the return of the bucket
     * is an empty bucket. If the item is not a bucket and doesn't return a bucket don't
     * use this method. Instead use #consumeHeldItem()
     *
     * @param player
     */
    public static void consumeBucketInHand(EntityPlayer player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            ItemStack bucket = new ItemStack(Items.bucket);
            if (player.getHeldItem().stackSize == 1)
            {
                player.inventory.mainInventory[player.inventory.currentItem] = bucket;
            }
            else if (player.getHeldItem().stackSize > 1)
            {
                player.getHeldItem().stackSize--;
                if (!player.inventory.addItemStackToInventory(bucket))
                {
                    InventoryUtility.dropItemStack(new Location(player), bucket);
                }
            }
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    /**
     * Called to consume an ItemStack in a way that is mod supported. This mainly just allows fluid
     * items to return empty versions. For example a lava bucket will be consumed turned into an
     * empty bucket.
     */
    public static ItemStack consumeStack(ItemStack stack)
    {
        if (stack.stackSize == 1)
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
        if (stackA != null && stackB != null)
        {
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB) && stackA.stackSize == stackB.stackSize;
        }
        return stackA == null && stackB == null;
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
        if (stackA != null && stackB != null)
        {
            return stackA.isItemEqual(stackB) && doesStackNBTMatch(stackA, stackB);
        }
        return stackA == null && stackB == null;
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
        if (stackA != null && stackB != null)
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
                    if (inv.getStackInSlot(slot) != null && inv.getStackInSlot(slot).isItemEqual(stack))
                    {
                        count += inv.getStackInSlot(slot).stackSize;
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
                    if (inv.getStackInSlot(slot) != null && compare.isInstance(inv.getStackInSlot(slot).getItem()))
                    {
                        count += inv.getStackInSlot(slot).stackSize;
                    }
                }
            }
        }

        return count;
    }

    public static ArrayList getAllItemsInPlayerInventory(EntityPlayer entity)
    {
        ArrayList<ItemStack> itemsToDrop = new ArrayList();
        for (int slot = 0; slot < entity.inventory.mainInventory.length; slot++)
        {
            if (entity.inventory.mainInventory[slot] != null)
            {
                itemsToDrop.add(entity.inventory.mainInventory[slot]);
            }
        }
        for (int slot = 0; slot < entity.inventory.armorInventory.length; slot++)
        {
            if (entity.inventory.armorInventory[slot] != null)
            {
                itemsToDrop.add(entity.inventory.armorInventory[slot]);
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
    public static boolean handleSlot(EntityPlayer player, IInventory inv, int slot)
    {
        return handleSlot(player, inv, slot, -1);
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
    public static boolean handleSlot(EntityPlayer player, IInventory inv, int slot, int items)
    {
        if (player != null && inv != null && slot >= 0 && slot < inv.getSizeInventory())
        {
            if (!addItemToSlot(player, inv, slot, items))
            {
                return removeItemFromSlot(player, inv, slot, items);
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
    public static boolean addItemToSlot(EntityPlayer player, IInventory inv, int slot)
    {
        return addItemToSlot(player, inv, slot, -1);
    }

    /**
     * Called to add the item the player is holding into the slot
     *
     * @param player - player who is accessing the item
     * @param slot   - slot to place the item into
     * @return true if the item was added, false if nothing happended
     */
    public static boolean addItemToSlot(EntityPlayer player, IInventory inv, int slot, int items)
    {
        //Check if input is valid
        if (player.getHeldItem() != null && inv.isItemValidForSlot(slot, player.getHeldItem()))
        {
            //Only can add items if slot is empty or matches input
            if (inv.getStackInSlot(slot) == null || stacksMatch(player.getHeldItem(), inv.getStackInSlot(slot)))
            {
                //Find out how much space we have left
                int roomLeftInSlot = roomLeftInSlotForStack(inv, player.getHeldItem(), slot);
                //Find out how many items to add to slot
                int itemsToAdd = Math.min(roomLeftInSlot, Math.min(player.getHeldItem().stackSize, items == -1 ? roomLeftInSlot : items));
                //Add items already in slot since we are going to set the slot
                if (inv.getStackInSlot(slot) != null)
                {
                    itemsToAdd += inv.getStackInSlot(slot).stackSize;
                }

                inv.setInventorySlotContents(slot, player.getHeldItem().copy());
                inv.getStackInSlot(slot).stackSize = itemsToAdd;

                //Ignore creative mode
                if (!player.capabilities.isCreativeMode)
                {
                    player.getHeldItem().stackSize -= itemsToAdd;
                    if (player.getHeldItem().stackSize <= 0)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
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
        if (inv.getStackInSlot(slot) != null)
        {
            int maxSpace = Math.min(inv.getStackInSlot(slot).getMaxStackSize(), inv.getInventoryStackLimit());
            return maxSpace - inv.getStackInSlot(slot).stackSize;
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
        return stack.getMaxStackSize() - stack.stackSize;
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
        if (inv.getStackInSlot(slot) != null)
        {
            return maxSpace - inv.getStackInSlot(slot).stackSize;
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
    public static int spaceInPlayersHand(EntityPlayer player)
    {
        return player.getHeldItem() == null ? player.inventory.getInventoryStackLimit() : Math.min(player.inventory.getInventoryStackLimit(), player.getHeldItem().getMaxStackSize()) - player.getHeldItem().stackSize;
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
    public static boolean removeItemFromSlot(EntityPlayer player, IInventory inv, int slot)
    {
        return removeItemFromSlot(player, inv, slot, -1);
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
    public static boolean removeItemFromSlot(EntityPlayer player, IInventory inv, int slot, int items)
    {
        if (inv.getStackInSlot(slot) != null && items >= -1 && items != 0)
        {
            int spaceInHand = spaceInPlayersHand(player);
            int itemsToMove = Math.min(Math.min(spaceInHand, inv.getStackInSlot(slot).getMaxStackSize()), items == -1 ? inv.getInventoryStackLimit() : items);

            //Create clone of slot stack, only not used in one use case
            ItemStack stack = inv.getStackInSlot(slot).copy();
            stack.stackSize = itemsToMove;

            //Moves items to player
            if (player.getHeldItem() == null)
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
            }
            else if (spaceInHand > 0)
            {
                player.getHeldItem().stackSize += itemsToMove;
            }
            else if (!player.inventory.addItemStackToInventory(stack))
            {
                InventoryUtility.dropItemStack(new Location(player), stack);
            }

            //Remove items from slot
            if (itemsToMove >= inv.getStackInSlot(slot).stackSize)
            {
                inv.setInventorySlotContents(slot, null);
            }
            else
            {
                inv.getStackInSlot(slot).stackSize -= itemsToMove;
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
                if (stack != null && stack.getItem() instanceof ItemArmor)
                {
                    final ItemArmor.ArmorMaterial mat = ((ItemArmor) stack.getItem()).getArmorMaterial();
                    if (mat != ItemArmor.ArmorMaterial.CLOTH && mat != ItemArmor.ArmorMaterial.DIAMOND)
                    {
                        c += 1;
                    }
                }
            }
        }
        else if (entity instanceof EntityCreature)
        {
            //Armor is stored in slots 1 - 4, 0 is held item and is taken care of by EntityLivingBase check
            for (int i = 1; i <= 4; i++)
            {
                final ItemStack stack = ((EntityCreature) entity).getEquipmentInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemArmor)
                {
                    final ItemArmor.ArmorMaterial mat = ((ItemArmor) stack.getItem()).getArmorMaterial();
                    if (mat != ItemArmor.ArmorMaterial.CLOTH && mat != ItemArmor.ArmorMaterial.DIAMOND)
                    {
                        c += 1;
                    }
                }
            }
        }

        if (entity instanceof EntityLivingBase)
        {
            if (((EntityLivingBase) entity).getHeldItem() != null)
            {
                //TODO make a dictionary of material to item types
                ItemStack held = ((EntityLivingBase) entity).getHeldItem();
                Item heldItem = held.getItem();
                if (heldItem instanceof ItemSword)
                {
                    String mat = ((ItemSword) heldItem).getToolMaterialName();
                    if (mat.equalsIgnoreCase("iron") || mat.equalsIgnoreCase("gold"))
                    {
                        c += 1;
                    }
                }
                else if (heldItem instanceof ItemTool)
                {
                    String mat = ((ItemTool) heldItem).getToolMaterialName();
                    if (mat.equalsIgnoreCase("iron") || mat.equalsIgnoreCase("gold"))
                    {
                        c += 1;
                    }
                }
            }
        }
        return c;
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
            if (inventory.getStackInSlot(slot) != null)
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
            if (inventory.getStackInSlot(slot) == null)
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
