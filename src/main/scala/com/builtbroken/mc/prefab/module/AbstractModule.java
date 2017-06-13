package com.builtbroken.mc.prefab.module;

import com.builtbroken.mc.api.modules.IModule;
import com.builtbroken.mc.api.modules.IModuleHasMass;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

/**
 * Created by robert on 12/28/2014.
 */
public abstract class AbstractModule implements IModule, IModuleHasMass
{
    /** ItemStack that represents this module */
    protected ItemStack item;
    protected final String name;

    public AbstractModule(ItemStack item, String name)
    {
        this.setItem(item != null ? item.copy() : null);
        this.name = name;
    }

    @Override
    public double getMass()
    {
        return 1;
    }

    @Override
    public String getUnlocalizedName()
    {
        return "module." + getName();
    }

    /** Loads from the item's NBT */
    public final AbstractModule load()
    {
        if (getItem() != null)
        {
            load(getItem());
        }
        return this;
    }

    /** Loads from an ItemStack's NBT, can be used to clone modules */
    public void load(ItemStack stack)
    {
        if (stack.getTagCompound() != null)
        {
            load(stack.getTagCompound());
        }
    }

    @Override
    public void save(ItemStack stack)
    {
        if (resetTagOnSave())
        {
            //Clear old data
            stack.setTagCompound(null);
        }

        //Collect new data
        NBTTagCompound tagCompound = new NBTTagCompound();
        save(tagCompound);

        //Add save tag if allowed
        if (saveTag())
        {
            tagCompound.setString(ModuleBuilder.SAVE_ID, getSaveID());
        }

        //Only save to item if we have data to save
        if (!tagCompound.hasNoTags())
        {
            stack.setTagCompound(tagCompound);
        }
    }

    /**
     * Checks if the tag of the item
     * should be reset each save with
     * a fresh save tag.
     *
     * @return true to reset
     */
    protected boolean resetTagOnSave()
    {
        return true;
    }

    /**
     * Called to check if the save tag ID
     * should be saved to the items NBT
     *
     * @return
     */
    protected boolean saveTag()
    {
        return true;
    }

    @Override
    public ItemStack toStack()
    {
        ItemStack stack = getItem().copy();
        save(stack);
        return stack;
    }

    /** Does the same thing as {@link #toStack()} */
    public ItemStack save()
    {
        ItemStack stack = getItem().copy();
        save(stack);
        return stack;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {

    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        return nbt;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {

    }

    @Override
    public String toString()
    {
        return getClass().getName() + "@" + hashCode();
    }

    public ItemStack getItem()
    {
        return item;
    }

    public void setItem(ItemStack item)
    {
        this.item = item;
    }

    public String getName()
    {
        return name;
    }
}
