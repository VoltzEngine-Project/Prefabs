package com.builtbroken.mc.prefab.module;

import com.builtbroken.mc.api.IHasMass;
import com.builtbroken.mc.api.items.IItemHasMass;
import com.builtbroken.mc.api.modules.IModule;
import com.builtbroken.mc.api.modules.IModuleHasMass;
import com.builtbroken.mc.api.modules.IModuleItem;
import com.builtbroken.mc.prefab.items.ItemAbstract;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Prefab for implementing {@link IModuleItem} that provides {@link IModule}
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/26/2015.
 */
public abstract class ItemAbstractModule<I extends IModule> extends ItemAbstract implements IModuleItem, IItemHasMass
{
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        IModule module = getModule(stack);
        if (module != null)
        {
            return module.getUnlocalizedName();
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    public I getModule(ItemStack stack)
    {
        if (stack != null)
        {
            //Copy stack to prevent issues
            ItemStack insert = stack.copy();
            insert.stackSize = 1;

            //Get module
            I module = newModule(stack);

            //Load save if present
            if (module != null)
            {
                NBTTagCompound moduleSave = getTagForModule(module, stack);
                if (moduleSave != null)
                {
                    module.load(moduleSave);
                }
            }

            //Return module
            return module;
        }
        return null;
    }

    /**
     * Allows overriding the default tag used for {@link IModule#load(NBTTagCompound)}
     * during creation of the module during {@link #getModule(ItemStack)}
     *
     * @param module - module create via {@link #newModule(ItemStack)}
     * @param stack  - stack used to create module
     * @return save tag, or null for no tag
     */
    protected NBTTagCompound getTagForModule(I module, ItemStack stack)
    {
        return stack.getTagCompound();
    }

    /**
     * Creates a new module from the stack
     * <p>
     * Is called from {@link #getModule(ItemStack)}
     * which will pass in a single count stack of the module
     * as well call {@link IModule#load(NBTTagCompound)} on the
     * module after it is created. So only use NBT to select
     * the correct module to return
     *
     * @param stack
     * @return
     */
    protected abstract I newModule(ItemStack stack);

    @Override
    public double getMass(ItemStack stack)
    {
        IModule module = getModule(stack);
        if (module instanceof IModuleHasMass)
        {
            return ((IModuleHasMass) module).getMass() + ((IModuleHasMass) module).getSubPartMass();
        }
        else if (module instanceof IHasMass)
        {
            return ((IHasMass) module).getMass();
        }
        return -1;
    }
}
