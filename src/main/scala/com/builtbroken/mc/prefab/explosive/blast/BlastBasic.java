package com.builtbroken.mc.prefab.explosive.blast;

import com.builtbroken.mc.api.edit.BlockEditResult;
import com.builtbroken.mc.api.edit.IWorldEdit;
import com.builtbroken.mc.api.event.blast.BlastEventBlockEdit;
import com.builtbroken.mc.api.event.blast.BlastEventBlockReplaced;
import com.builtbroken.mc.api.event.blast.BlastEventDestroyBlock;
import com.builtbroken.mc.api.explosive.IBlastEdit;
import com.builtbroken.mc.api.explosive.IExplosive;
import com.builtbroken.mc.api.explosive.IExplosiveDamageable;
import com.builtbroken.mc.api.explosive.IExplosiveHandler;
import com.builtbroken.mc.api.tile.node.ITileNodeHost;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.framework.explosive.blast.Blast;
import com.builtbroken.mc.imp.transform.sorting.Vector3DistanceComparator;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.MathUtility;
import com.builtbroken.mc.lib.world.edit.BlockEdit;
import com.builtbroken.mc.prefab.entity.selector.EntityDistanceSelector;
import com.builtbroken.mc.prefab.explosive.debug.BlastProfiler;
import com.builtbroken.mc.prefab.explosive.debug.BlastRunProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Prefab for simple blasts
 * Created by robert on 11/19/2014.
 */
//TODO use pathfinder for emp to allow for EMP shielding
//TODO replace with recursive pathfinder that doesn't keep calling expand, this will stack overflow
//TODO update pathfinding methods to be more reusable
public class BlastBasic<B extends BlastBasic> extends Blast<B>
{
    /**
     * DamageSourse to attack entities with
     */
    static DamageSource source = new DamageSource("blast").setExplosion();
    static BlastProfiler profiler = new BlastProfiler();

    /**
     * Energy to start the explosion with
     */
    public float energy = 0;
    /**
     * Median size of the explosion from center, max size is x2, min size is 0
     */
    protected double radius = 0;

    /**
     * Blocks to call after all blocks are removed in case they do updates when destroyed
     */
    protected List<IWorldEdit> postCallDestroyMethod = new ArrayList();
    /**
     * Profilier for the blast
     */
    protected BlastRunProfile profile;

    public Location center;

    public BlastBasic(IExplosiveHandler handler)
    {
        super(handler);
        profile = profiler.run(this);
    }


    @Override
    public void getEffectedBlocks(List<IWorldEdit> list)
    {
        Location c = new Location(oldWorld(), (int) x(), (int) y(), (int) z());
        center = c.add(0.5);

        //TODO disable profiler if not in debug mode
        HashMap<IBlastEdit, Float> map = new HashMap();
        profile.startSection("getEffectedBlocks");

        //Start path finder
        profile.startSection("Pathfinder");

        IBlastEdit edit = new BlockEdit(this);
        triggerPathFinder(map, edit, energy);

        profile.endSection("Pathfinder");

        //Add map keys to block list
        list.addAll(map.keySet());

        //Sort results so blocks are placed in the center first
        profile.startSection("Sorter");
        Collections.sort(list, new Vector3DistanceComparator(new Pos(x(), y(), z())));
        profile.endSection("Sorter");

        profile.endSection("getEffectedBlocks");
        //Generate debug info
        if (Engine.runningAsDev)
        {
            Engine.logger().info(profile.getOutputSimple());
        }
    }

    /**
     * Called to trigger the blast pathfinder
     *
     * @param map    - hash map to store data for block placement to energy used
     * @param vec    - starting block
     * @param energy - starting energy
     */
    protected void triggerPathFinder(HashMap<IBlastEdit, Float> map, IBlastEdit vec, float energy)
    {
        //Start pathfinder
        expand(map, vec, energy, null, 0);
    }

    /**
     * Called to map another iteration to expand outwards from the center of the explosion
     *
     * @param map       - hash map to store data for block placement to energy used
     * @param vec       - next block to expand from, and to log to map
     * @param energy    - current energy at block
     * @param side      - side not to expand in, and direction we came from
     * @param iteration - current iteration count from center, use this to stop the iteration if they get too far from center
     */
    protected void expand(HashMap<IBlastEdit, Float> map, IBlastEdit vec, float energy, Direction side, int iteration)
    {
        //Keep track of iterations
        profile.tilesPathed++;

        long timeStart = System.nanoTime();
        if (iteration < size * 2)
        {
            //Calculate energy lost on block path
            float energyCost = Math.abs(getEnergyCostOfTile(vec, energy));
            float energyLeft = energy - energyCost;

            //Only path block if cost is not great
            if (energyCost >= 0 && energyLeft > 1)
            {
                //Set energy pathed through block
                vec.setEnergy(energy);

                //Get edit result of pathed block
                IBlastEdit blastEdit = onBlockMapped(vec, energyCost, energyLeft);

                //Only continue pathing if result is not empty
                if (blastEdit != null)
                {
                    map.put(blastEdit, energyCost);

                    //Only iterate threw sides if we have more energy
                    if (energyLeft > 1)
                    {
                        //Get valid sides to iterate threw
                        List<IBlastEdit> sides = new ArrayList();
                        for (Direction dir : Direction.DIRECTIONS)
                        {
                            if (dir != side)
                            {
                                IBlastEdit v = new BlockEdit(oldWorld, vec, dir);
                                v.doDrops();
                                v.setBlastDirection(dir);
                                v.logPrevBlock();
                                sides.add(v);
                            }
                        }

                        //Sort by distance
                        Collections.sort(sides, new Vector3DistanceComparator(new Pos(x(), y(), z())));

                        profile.blockIterationTimes.add(System.nanoTime() - timeStart);

                        //Iterate threw sides expending energy outwards
                        for (IBlastEdit editSides : sides)
                        {
                            float eToSpend = (energyLeft / sides.size()) + (energyLeft % sides.size());
                            energyLeft -= eToSpend;
                            Direction face = side == null ? getOpposite(editSides.getBlastDirection()) : side;
                            if (!map.containsKey(editSides) || map.get(editSides) < eToSpend)
                            {
                                editSides.setBlastDirection(face);
                                expand(map, editSides, eToSpend, face, iteration + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    //TODO move to helper class later, and PR into forge if its not already there
    private Direction getOpposite(Direction face)
    {
        if (face != null)
        {
            switch (face)
            {
                case UP:
                    return Direction.DOWN;
                case DOWN:
                    return Direction.UP;
                case NORTH:
                    return Direction.SOUTH;
                case SOUTH:
                    return Direction.NORTH;
                case EAST:
                    return Direction.WEST;
                case WEST:
                    return Direction.EAST;
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Called to see how much energy is lost effecting the block at the location
     *
     * @param vec    - location
     * @param energy - energy to expend on the location
     * @return amount of energy used to destroy the block, -1 is ignore
     */
    protected float getEnergyCostOfTile(IBlastEdit vec, float energy)
    {
        Block block = vec.getBlock();
        TileEntity tileEntity = vec.getTileEntity();
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
            return damageableTile.getEnergyCostOfTile(explosiveHandler, this, vec.getBlastDirection(), energy, (float) center.distance(vec.xi() + 0.5, vec.yi() + 0.5, vec.zi() + 0.5));
        }
        //Update debug info
        if (block.isAir(oldWorld, vec.xi(), vec.yi(), vec.zi()))
        {
            profile.airBlocksPathed++;
        }
        else
        {
            profile.blocksRemoved++;
        }
        //Get cost
        return (block.getBlockHardness(oldWorld, vec.xi(), vec.yi(), vec.zi()) >= 0 ? (float) Math.max(block.getExplosionResistance(explosionBlameEntity, oldWorld, vec.xi(), vec.yi(), vec.zi(), x, y, z), 0.5) : -1);

    }

    @Override
    public void handleBlockPlacement(IWorldEdit vec)
    {
        if (vec != null && vec.hasChanged() && prePlace(vec))
        {
            final Block block = vec.getBlock();
            //TODO add energy value of explosion to this explosion if it is small
            //TODO maybe trigger explosion inside this thread allowing for controlled over lap
            //TODO if we trigger the explosive move most of the energy in the same direction
            //the current explosion is running in with a little bit in the opposite direction

            //TODO check that the block was destroyed (If not modify events fired)
            //TODO add a damage event for blocks changed instead of destroyed
            //Trigger break event so blocks can do X action
            if (!(block instanceof BlockTNT) && !(vec.getTileEntity() instanceof IExplosive))
            {
                block.onBlockDestroyedByExplosion(oldWorld, (int) vec.x(), (int) vec.y(), (int) vec.z(), wrapperExplosion);
            }
            else
            {
                //Add explosives to post call to allow the thread to finish before generating more explosions
                postCallDestroyMethod.add(vec);
            }

            BlockEditResult result = vec.place();
            if (Engine.runningAsDev)
            {
                System.out.println("Result: " + result + "   Edit: " + vec + "  Block: " + vec.getBlock() + "  Tile: " + vec.getTileEntity());
            }
            postPlace(vec);
        }
    }

    /**
     * Called to give the blast a chance to override what
     * the block at the location will turn into
     *
     * @param change         - location and placement data
     *                       change.setBlock(Block)
     *                       change.setMeta(meta)
     *                       to update the placement info
     * @param energyExpended - energy expended on the block to change it
     * @return new placement info, never change the location or you will
     * create a duplication issue as the original block will not be removed
     */
    protected IBlastEdit onBlockMapped(IBlastEdit change, float energyExpended, float energyLeft)
    {
        TileEntity tileEntity = change.getTileEntity();
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
            return damageableTile.getBlockEditOnBlastImpact(explosiveHandler, this, change.getBlastDirection(), energy, (float) center.distance(change.xi() + 0.5, change.yi() + 0.5, change.zi() + 0.5));
        }

        if (energyExpended > energyLeft)
        {
            change.doDrops();
        }
        return change;
    }

    @Override
    public void doEffectOther(boolean beforeBlocksPlaced)
    {
        if (!beforeBlocksPlaced)
        {
            //TODO wright own version of getEntitiesWithinAABB that takes a filter and cuboid(or Vector3 to Vector3)
            //TODO ensure that the entity is in line of sight
            //TODO ensure that the entity can be pathed by the explosive
            AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(x - size - 1, y - size - 1, z - size - 1, x + size + 1, y + size + 1, z + size + 1);
            List list = oldWorld.selectEntitiesWithinAABB(Entity.class, bounds, new EntityDistanceSelector(new Pos(x, y, z), size + 1, true));
            if (list != null && !list.isEmpty())
            {
                damageEntities(list, source);
            }
        }
    }


    @Override
    public B setYield(double size)
    {
        super.setYield(size);
        //Most of the time radius equals size of the explosion
        radius = size;
        calcStartingEnergy();
        return (B) this;
    }

    /**
     * Calculates the starting energy based on the size of the explosion
     */
    protected void calcStartingEnergy()
    {
        energy = (float) (MathUtility.getSphereVolume(radius) * eUnitPerBlock);
    }

    @Override
    protected void postPlace(final IWorldEdit vec)
    {
        MinecraftForge.EVENT_BUS.post(new BlastEventDestroyBlock.Post(this, BlastEventDestroyBlock.DestructionType.FORCE, oldWorld, vec.getBlock(), vec.getBlockMetadata(), (int) vec.x(), (int) vec.y(), (int) vec.z()));
    }

    @Override
    protected boolean prePlace(final IWorldEdit vec)
    {
        BlastEventBlockEdit event = new BlastEventDestroyBlock.Pre(this, BlastEventDestroyBlock.DestructionType.FORCE, oldWorld, vec.getBlock(), vec.getBlockMetadata(), (int) vec.x(), (int) vec.y(), (int) vec.z());

        boolean result = MinecraftForge.EVENT_BUS.post(event);
        if (vec instanceof IBlastEdit && event instanceof BlastEventBlockReplaced.Pre && ((BlastEventBlockReplaced.Pre) event).newBlock != null)
        {
            vec.set(((BlastEventBlockReplaced.Pre) event).newBlock, ((BlastEventBlockReplaced.Pre) event).newMeta);
        }
        return !result;
    }
}
