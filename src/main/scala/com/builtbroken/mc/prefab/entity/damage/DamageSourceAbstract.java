package com.builtbroken.mc.prefab.entity.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

/**
 * Extend this class to create more custom damage sources.
 */
public abstract class DamageSourceAbstract extends DamageSource
{
    /** Source of the damage (can be an entity, tile, or the world itself) */
	protected Object damageSource;

	public DamageSourceAbstract(String damageType)
	{
		super(damageType);
	}

	public DamageSourceAbstract(String damageType, Object source)
	{
		this(damageType);
		this.damageSource = source;
	}

	@Override
	public Entity getTrueSource()
	{
		return damageSource instanceof Entity ? ((Entity) damageSource) : null;
	}

	public TileEntity getTileEntity()
	{
		return damageSource instanceof TileEntity ? ((TileEntity) damageSource) : null;
	}

	@Override
	public ITextComponent getDeathMessage(EntityLivingBase victum)
	{
        //TODO JUnit test to see if this method outputs the correct data
		EntityLivingBase attacker = victum.getAttackingEntity();
		String deathTranslation = "death.attack." + this.damageType;
		String playerKillTranslation = deathTranslation + ".player";
		String machineKillTranslation = deathTranslation + ".machine";
		if (damageSource instanceof TileEntity)
		{
			if (I18n.canTranslate(machineKillTranslation))
			{
				return new TextComponentTranslation(machineKillTranslation, victum.getDisplayName());
			}
		}
		else if (attacker != null)
		{
			if (I18n.canTranslate(playerKillTranslation))
			{
				return new TextComponentTranslation(playerKillTranslation, victum.getDisplayName(), attacker.getDisplayName());
			}
		}
		else if (I18n.canTranslate(deathTranslation))
		{
			return new TextComponentTranslation(deathTranslation, victum.getDisplayName());
		}
		return null;
	}

}
