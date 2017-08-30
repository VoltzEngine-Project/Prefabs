package com.builtbroken.mc.prefab.explosive.blast;

import com.builtbroken.mc.api.edit.IWorldChangeLayeredAction;
import com.builtbroken.mc.api.edit.IWorldEdit;
import com.builtbroken.mc.api.explosive.IExplosiveDamageable;
import com.builtbroken.mc.api.explosive.IExplosiveHandler;
import com.builtbroken.mc.api.tile.node.ITileNodeHost;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.framework.explosive.blast.Blast;
import com.builtbroken.mc.imp.transform.vector.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Very simple version of the the BasicBlast the will
 * path find in all directions. Doesn't do any extra
 * checks beyond distance and can path.
 * Created by robert on 1/28/2015.
 */
public abstract class BlastSimplePath<B extends BlastSimplePath> extends Blast<B> implements IWorldChangeLayeredAction
{
    protected long lastUpdate = -1;
    /**
     * List of locations already check by the pathfinder, used to prevent infinite loops
     */
    protected List<BlockPos> pathed_locations = new ArrayList();

    /** Set to use recursive pathfinder. */
    public boolean recursive = false;

    public Queue<BlockPos> stack = new LinkedList();
    protected int layers = 1;
    protected int blocksPerLayer = 1000;

    public BlastSimplePath(IExplosiveHandler handler)
    {
        super(handler);
    }

    public BlastSimplePath(IExplosiveHandler handler, World world, int x, int y, int z, int size)
    {
        super(handler, world, x, y, z, size);
    }

    @Override
    public void getEffectedBlocks(List<IWorldEdit> list)
    {
        BlockPos c = new BlockPos(xi(), yi(), zi());

        if (shouldPath(c))
        {
            if (recursive)
            {
                pathNext(c, list);
            }
            else
            {
                pathEntire(c, list);
            }
        }
        else
        {
            //Temp fix to solve if center is an air block
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                BlockPos location = new BlockPos(xi() + dir.offsetX, yi() + dir.offsetY, zi() + dir.offsetZ);
                if (shouldPath(location))
                {
                    if (recursive)
                    {
                        pathNext(location, list);

                    }
                    else
                    {
                        pathEntire(location, list);
                    }
                }
            }
        }
    }


    @Override
    public void getEffectedBlocks(List<IWorldEdit> list, int layer)
    {
        recursive = false;
        if (layer == 0)
        {
            getEffectedBlocks(list);
        }
        else
        {
            continuePathEntire(list, blocksPerLayer);
        }
    }

    @Override
    public B setYield(double size)
    {
        double prev = this.size;
        super.setYield(size);
        if (prev != size)
        {
            calculateLayers();
        }
        return (B) this;
    }

    public void calculateLayers()
    {
        double volume = 4 / 3 * Math.PI * size * size * size;
        int i = (int) (volume / (double) blocksPerLayer);
        if (i > 0)
        {
            layers = i;
        }
        else
        {
            layers = 1;
        }
    }

    @Override
    public int getLayers()
    {
        return layers;
    }

    @Override
    public boolean shouldContinueAction(int layer)
    {
        return !killExplosion && (layer == 0 || !stack.isEmpty());
    }


    /**
     * Does the entire pathfinder in one go instead of recursing onto itself.
     *
     * @param startNode - starting point
     * @param list      - list of edits
     */
    public void pathEntire(final BlockPos startNode, final List<IWorldEdit> list)
    {
        pathEntire(startNode, list, blocksPerLayer);
    }

    /**
     * Does the entire pathfinder in one go instead of recursing onto itself.
     *
     * @param startNode - starting point
     * @param list      - list of edits
     */
    public void pathEntire(final BlockPos startNode, final List<IWorldEdit> list, final int count)
    {
        if (shouldPath(startNode))
        {
            if (stack.isEmpty())
            {
                //Get first edit
                IWorldEdit edit = getEditForLocation(startNode, null);
                if (edit != null)
                {
                    list.add(edit);
                }

                //Create stack to store current path nodes
                stack.offer(startNode);
                pathed_locations.add(startNode);
            }
            continuePathEntire(list, count);
        }
    }

    /**
     * Called to continue pathfinding
     *
     * @param list  - list of edits
     * @param count - number of loops to run
     */
    public void continuePathEntire(final List<IWorldEdit> list, final int count)
    {
        int currentCount = 0;

        //Loop until we run out of nodes
        boolean shouldExit = false;
        while (!stack.isEmpty() && !shouldExit && currentCount < count)
        {
            shouldExit = shouldKillAction();
            //Pop a node off the stack each iteration
            BlockPos currentNode = stack.poll();
            currentCount++;

            for (Direction dir : Direction.DIRECTIONS)
            {
                BlockPos nextNode = new BlockPos(currentNode, dir);

                //Check if we can path to the node from the current node
                if (shouldPathTo(currentNode, nextNode, dir))
                {
                    //Add to already pathed list
                    pathed_locations.add(nextNode);

                    //Check if we can path the node
                    if (shouldPath(nextNode))
                    {
                        //Add next node to path stack
                        stack.offer(nextNode);

                        IWorldEdit edit = getEditForLocation(nextNode, dir);

                        //Avoid adding empty edits or existing edits
                        if (edit != null && !list.contains(edit) && edit.hasChanged())
                        {
                            list.add(edit);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called to get the edit for the location of the blast
     *
     * @param pos       - node being pathed
     * @param blastDirection - direction the blast is traveling in, null or unknown is starting node
     * @return edit, or null for no edit
     */
    public IWorldEdit getEditForLocation(BlockPos pos, Direction blastDirection)
    {
        //Do logic for damageable blocks
        TileEntity tileEntity = pos.getTileEntity(oldWorld);
        IExplosiveDamageable damageableTile = null;
        if (tileEntity instanceof IExplosiveDamageable)
        {
            damageableTile = (IExplosiveDamageable) tileEntity;
        }
        else if (tileEntity instanceof ITileNodeHost && ((ITileNodeHost) tileEntity).getTileNode() instanceof IExplosiveDamageable)
        {
            damageableTile = (IExplosiveDamageable) ((ITileNodeHost) tileEntity).getTileNode();
        }
        if (damageableTile != null)
        {
            float distance = (float) blockCenter.distance(pos.xi() + 0.5, pos.yi() + 0.5, pos.zi() + 0.5);
            float energy = getEnergy(pos, distance);
            float energyCost = damageableTile.getEnergyCostOfTile(explosiveHandler, this, blastDirection, energy, distance);
            if (energyCost > 0)
            {
                return damageableTile.getBlockEditOnBlastImpact(explosiveHandler, this, blastDirection, energy, distance);
            }
        }
        //Get Block edit for the location that we can path
        return changeBlock(pos);
    }

    /**
     * Gets the energy of the blast the location
     * <p>
     * Only used for {@link IExplosiveDamageable} tiles and blocks
     *
     * @param location
     * @param distance
     * @return
     */
    public float getEnergy(BlockPos location, double distance)
    {
        return -1;
    }

    /**
     * Called to path the node location in all directions looking
     * for blocks to edit
     *
     * @param node - node, should not be null
     * @param list - list to add edits to, should not be null
     */
    public void pathNext(final BlockPos node, final List<IWorldEdit> list)
    {
        if (!shouldKillAction())
        {
            //Prevent re-adding the same node again
            if (!pathed_locations.contains(node))
            {
                pathed_locations.add(node);
            }

            //Check if we can path the current node
            if (shouldPath(node))
            {
                //Get Block edit for the location that we can path
                final IWorldEdit edit = changeBlock(node);

                //Avoid adding empty edits or existing edits
                if (edit != null && !list.contains(edit) && edit.hasChanged())
                {
                    list.add(edit);
                }

                //Loop over all 6 sides
                for (Direction dir : Direction.DIRECTIONS)
                {
                    //Generated next node
                    final BlockPos next = new BlockPos(node, dir);
                    //Check if we can path to next node from this node
                    if (shouldPathTo(node, next, dir))
                    {
                        pathNext(next, list);
                    }
                }
            }
        }
    }

    /**
     * Called to see what block the location's block will change to
     *
     * @param location - location to get data from
     * @return null for ignore, or BlockEdit for anything else
     */
    public abstract IWorldEdit changeBlock(BlockPos location);

    /**
     * Called to check if the location should be pathed
     * Can be used for any check including distance, and block infomration
     *
     * @param location - location to check
     * @return true if it can be pathed
     */
    public boolean shouldPath(BlockPos location)
    {
        double distance = blockCenter.distance(location.xi() + 0.5, location.yi() + 0.5, location.zi() + 0.5);
        return distance <= size;
    }

    /**
     * Called to see if the pathfinder should go to the next block
     *
     * @param last - last block pathed
     * @param next - block being pathed next
     * @param dir  - direction traveled
     * @return true if can path
     */
    public boolean shouldPathTo(BlockPos last, BlockPos next, Direction dir)
    {
        return shouldPathTo(last, next);
    }

    /**
     * Legacy for older versions of ICBM
     *
     * @deprecated Use the above method instead
     */
    @Deprecated
    public boolean shouldPathTo(BlockPos last, BlockPos next)
    {
        return next.y() >= 0 && next.y() <= 255 && !pathed_locations.contains(next);
    }
}
