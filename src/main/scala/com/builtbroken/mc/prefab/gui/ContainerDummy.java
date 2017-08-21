package com.builtbroken.mc.prefab.gui;

import com.builtbroken.mc.api.tile.IPlayerUsing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

/**
 * Allows the use of a tile inventory without the need for a container class.
 *
 * @author DarkGuardsman
 */
public class ContainerDummy extends Container
{
	public Object tile;

	public ContainerDummy()
	{

	}

	public ContainerDummy(Object tile)
	{
		this.tile = tile;
	}

	public ContainerDummy(EntityPlayer player, Object tile)
	{
		this(tile);

		if (tile instanceof IPlayerUsing)
		{
			((IPlayerUsing) tile).getPlayersUsing().add(player);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if (tile instanceof IPlayerUsing)
		{
			((IPlayerUsing) tile).getPlayersUsing().remove(player);
		}

		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		return !(tile instanceof IInventory) || ((IInventory) tile).isUsableByPlayer(par1EntityPlayer);
	}

}
