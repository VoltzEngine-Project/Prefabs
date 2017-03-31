package com.builtbroken.mc.prefab.items;

import com.builtbroken.jlib.data.Colors;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 12/20/2016.
 */
public class ItemBlockAbstract extends ItemBlock
{
    //Make sure to mirro all changes to other abstract class
    public ItemBlockAbstract(Block p_i45328_1_)
    {
        super(p_i45328_1_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
    {
        try
        {
            //Generic info
            String translationKey = getUnlocalizedName() + ".info";
            String translation = LanguageUtility.getLocal(translationKey);
            if (!translation.isEmpty() && !translation.equals(translationKey))
            {
                list.add(translation);
            }

            if (hasDetailedInfo(stack, player))
            {
                getDetailedInfo(stack, player, list);
            }

            if (hasShiftInfo(stack, player))
            {
                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                {
                    list.add(LanguageUtility.getLocal("info.voltzengine:tooltip.noShift").replace("#0", Colors.AQUA.toString()).replace("#1", Colors.GREY.toString()));
                }
                else
                {
                    getShiftDetailedInfo(stack, player, list);
                }
            }
        }
        catch (Exception e)
        {
            //TODO display tooltip if error happens to often
            e.printStackTrace();
        }
    }

    /**
     * Gets the detailed information for the item shown after the
     * global generic item details.
     *
     * @param stack
     * @param player
     * @param list
     */
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        //Per item detailed info
        String translationKey = getUnlocalizedName(stack) + ".info";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            if (translation.contains(","))
            {
                String[] split = translation.split(",");
                for (String s : split)
                {
                    list.add(s.trim());
                }
            }
            else
            {
                list.add(translation);
            }
        }
    }

    /**
     * Gets the detailed when shift is held information for the item shown after the
     * global generic item details.
     * <p>
     * This is in addition to normal details
     *
     * @param stack
     * @param player
     * @param list
     */
    protected void getShiftDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        //Per item detailed info
        String translationKey = getUnlocalizedName(stack) + ".info.detailed";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            if (translation.contains(","))
            {
                String[] split = translation.split(",");
                for (String s : split)
                {
                    list.add(s.trim());
                }
            }
            else
            {
                list.add(translation);
            }
        }
    }

    /**
     * Does the item have detailed information to be shown
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        String translationKey = getUnlocalizedName() + ".info";
        String translationKey2 = getUnlocalizedName(stack) + ".info";
        return !translationKey.equals(translationKey2);
    }

    /**
     * Does the item have detailed information to be shown when
     * shift is held
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasShiftInfo(ItemStack stack, EntityPlayer player)
    {
        return false;
    }
}
