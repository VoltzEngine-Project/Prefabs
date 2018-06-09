package com.builtbroken.mc.prefab.tile;

import com.builtbroken.jlib.data.Colors;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Point;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.inventory.InventoryIterator;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Wrapper class used to encapsulate all block calls to the {@link Tile} object
 * allowing for developers to focus on content creation rather than duplicate
 * method calls, tile get calls, location checks, and wrappers to the {@link TileEntity}.
 * <p>
 * This class is designed to make creating complex tiles easier and should not
 * be used for simple blocks. If the block only requires a few changes and a texture
 * please use the normal block class. As this will remove the performance overhead
 * that comes with using this system.
 * <p>
 * Created by Robert(DarkGuardsman, DarkCow) on 1/4/2015.
 */
@Deprecated
public class BlockTile extends BlockContainer
{
    /** Wrapper for any block call that does not go to the {@link TileEntity} version of the {@line Tile} object */
    public Tile staticTile = null;

    /**
     * Creates a new instance of the BlockTile
     *
     * @param tile   - static tile
     * @param prefix - mod prefix
     * @param tab    - default creative tab to use
     */
    public BlockTile(Tile tile, String prefix, CreativeTabs tab)
    {
        super(tile.material);
        this.staticTile = tile;
        this.staticTile.setBlock(this);
        this.opaque = isOpaqueCube();
        this.setBlockBounds((float) this.staticTile.bounds.min().x(), (float) this.staticTile.bounds.min().y(), (float) this.staticTile.bounds.min().z(), (float) this.staticTile.bounds.max().x(), (float) this.staticTile.bounds.max().y(), (float) this.staticTile.bounds.max().z());

        setBlockName(prefix + staticTile.name);
        setBlockTextureName(prefix + staticTile.textureName);
        setCreativeTab(staticTile.creativeTab == null ? tab : staticTile.creativeTab);
        setLightOpacity(isOpaqueCube() ? 255 : 0);
        setHardness(staticTile.hardness);
        setResistance(staticTile.resistance);
        setStepSound(staticTile.stepSound);
    }

    @Override
    public TileEntity createTileEntity(World world, int meta)
    {
        return staticTile.newTile(world, meta);
    }


    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return staticTile.newTile(world, meta);
    }

    @Override
    public void fillWithRain(World world, int x, int y, int z)
    {
        Tile tile = inject(world, x, y, z);
        tile.onFillRain();
        eject();
    }

    @Override
    public float getExplosionResistance(Entity entity)
    {
        return staticTile.getExplosionResistance(entity);
    }

    @Override
    public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
        Tile tile = inject(world, x, y, z);
        float resistance = tile.getExplosionResistance(entity, new Pos(explosionX, explosionY, explosionZ));
        eject();
        return resistance;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
    {
        Tile tile = inject(world, x, y, z);
        tile.onPlayerLeftClick(player);
        eject();
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        Tile tile = inject(world, x, y, z);
        tile.onAdded();
        eject();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        Tile tile = inject(world, x, y, z);
        tile.onPlaced(entityLiving, itemStack);
        eject();
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int metadata)
    {
        Tile tile = inject(world, x, y, z);
        tile.onPostPlaced(metadata);
        eject();
    }

    /**
     * Called upon the block being destroyed by an explosion
     */
    @Override
    public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion ex)
    {
        Tile tile = inject(world, x, y, z);
        tile.onDestroyedByExplosion(ex);
        eject();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6)
    {
        Tile tile = inject(world, x, y, z);

        //Drop inventory
        IInventory inventory = null;
        if (tile instanceof IInventory)
        {
            inventory = (IInventory) tile;
        }
        else if (tile instanceof IInventoryProvider)
        {
            inventory = ((IInventoryProvider) tile).getInventory();
        }

        if (inventory != null)
        {
            InventoryIterator inventoryIterator = new InventoryIterator(inventory, true);
            while (inventoryIterator.hasNext())
            {
                ItemStack stack = inventoryIterator.next();
                InventoryUtility.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, stack, 0, 0);
                inventory.setInventorySlotContents(inventoryIterator.slot(), null);
            }
        }

        //Ask tile to do remove action
        tile.onRemove(block, par6);
        eject();

        //Super, removes tile so ignore
        //super.breakBlock(world, x, y, z, block, par6);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta)
    {
        super.harvestBlock(world, player, x, y, z, meta);
        world.removeTileEntity(x, y, z);
        world.setBlockToAir(x, y, z);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        Tile tile = inject(world, x, y, z);
        boolean b = tile.removeByPlayer(player, willHarvest);
        eject();
        return b;
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random)
    {
        return staticTile.quantityDropped(meta, fortune);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        Tile tile = inject(world, x, y, z);
        tile.onNeighborChanged(block);
        eject();
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
    {
        Tile tile = inject(world, x, y, z);
        boolean b = tile.canPlaceBlockOnSide(ForgeDirection.getOrientation(side));
        eject();
        return b;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        Tile tile = inject(world, x, y, z);
        boolean b = tile.canPlaceBlockAt();
        eject();
        return b;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
    {
        if (!(world instanceof World) || !((World) world).isRemote)
        {
            Tile tile = inject(world, x, y, z);
            tile.onNeighborChanged(new Pos(tileX, tileY, tileZ));
            eject();
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        try
        {
            Tile tile = inject(world, x, y, z);
            boolean value = tile.onPlayerActivated(player, side, new Pos(hitX, hitY, hitZ));
            eject();
            return value;
        }
        catch (Exception e)
        {
            outputError(world, x, y, z, "while right click block on side " + side, e);
            player.addChatComponentMessage(new ChatComponentText(Colors.RED.code + LanguageUtility.getLocal("blockTile.error.onBlockActivated")));
        }
        return false;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random par5Random)
    {
        Tile tile = inject(world, x, y, z);
        tile.blockUpdate();
        eject();
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
        Tile tile = inject(world, x, y, z);
        tile.randomDisplayTick();
        eject();
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
        Tile tile = inject(world, x, y, z);
        tile.onCollide(entity);
        eject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity)
    {
        Tile tile = inject(world, x, y, z);
        Iterable<Cube> bounds = tile.getCollisionBoxes(new Cube(aabb).subtract(new Pos(x, y, z)), entity);
        eject();
        if (bounds != null)
        {
            for (Cube cuboid : bounds)
            {
                AxisAlignedBB bb = cuboid.toAABB().offset(x, y, z);
                if (aabb.intersectsWith(bb))
                {
                    list.add(bb);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
        Tile tile = inject(world, x, y, z);
        AxisAlignedBB value = tile.getSelectBounds().clone().add(tile.x(), tile.y(), tile.z()).toAABB();
        eject();
        return value;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        Tile tile = inject(world, x, y, z);
        AxisAlignedBB value = tile.getCollisionBounds().clone().add(tile.x(), tile.y(), tile.z()).toAABB();
        eject();
        return value;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side)
    {
        Tile tile = inject(access, x, y, z);
        boolean value = tile.shouldSideBeRendered(side);
        eject();
        return value;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess access, int x, int y, int z, int side)
    {
        Tile tile = inject(access, x, y, z);
        boolean value = tile.isSolid(side);
        eject();
        return value;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
    {
        return isBlockSolid(world, x, y, z, side.ordinal());
    }

    @Override
    public int getLightValue(IBlockAccess access, int x, int y, int z)
    {
        int value = 0;
        if (access != null)
        {
            Tile tile = inject(access, x, y, z);
            value = tile.getLightValue();
            eject();
        }
        return value;
    }

    @Override
    public boolean hasComparatorInputOverride()
    {
        //TODO, add support for comparators
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return staticTile == null || staticTile.isOpaque;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return staticTile.renderNormalBlock;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType()
    {
        return staticTile.renderNormalBlock ? 0 : staticTile.renderType;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side)
    {
        Tile tile = inject(access, x, y, z);
        IIcon value = tile.getIcon(side, access.getBlockMetadata(x, y, z));
        eject();
        return value;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        return staticTile.getIcon(side, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        staticTile.registerIcons(iconRegister);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int colorMultiplier(IBlockAccess access, int x, int y, int z)
    {
        Tile tile = inject(access, x, y, z);
        int value = tile.getColorMultiplier();
        eject();
        return value;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBlockColor()
    {
        return staticTile.getBlockColor();
    }

    /**
     * Returns the color this block should be rendered. Used by leaves.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderColor(int i)
    {
        return staticTile.getRenderColor(i);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
    {
        Tile tile = inject(world, x, y, z);
        ItemStack value = tile.getPickBlock(target);
        eject();
        return value;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        Tile tile = inject(world, x, y, z);
        ArrayList<ItemStack> value = tile.getDrops(metadata, fortune);
        eject();
        return value != null ? value : new ArrayList<ItemStack>();
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list)
    {
        staticTile.getSubBlocks(item, creativeTabs, list);
    }

    /**
     * Redstone interaction
     */
    @Override
    public boolean canProvidePower()
    {
        return staticTile.canEmmitRedstone;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side)
    {
        Tile tile = inject(access, x, y, z);
        int value = tile.getWeakRedstonePower(side);
        eject();
        return value;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side)
    {
        Tile tile = inject(access, x, y, z);
        int value = tile.getStrongRedstonePower(side);
        eject();
        return value;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z)
    {
        Tile tile = inject(access, x, y, z);
        Cube cube = tile.getBlockBounds();
        if (cube != null)
        {
            setBlockBounds(cube.min().xf(), cube.min().yf(), cube.min().zf(), cube.max().xf(), cube.max().yf(), cube.max().zf());
        }
        eject();
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        eject();
        Cube cube = staticTile.getBlockBounds();
        if (cube != null)
        {
            setBlockBounds(cube.min().xf(), cube.min().yf(), cube.min().zf(), cube.max().xf(), cube.max().yf(), cube.max().zf());
        }
    }

    @Override
    protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack itemStack)
    {
        if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
        {
            InventoryUtility.dropItemStack(world, new Pos(x, y, z), itemStack);
        }
    }

    @Override
    public int getRenderBlockPass()
    {
        return staticTile.getRenderBlockPass();
    }

    @Override
    public int tickRate(World world)
    {
        Tile tile = inject(world, 0, 0, 0);
        int t = tile.tickRate();
        eject();
        return t;
    }

    /**
     * Converts the 3D location data of the face being clicked
     * into a 2D {@link Point} for easier calculations
     *
     * @param hitSide - side of the block clicked
     * @param hitX    - location
     * @param hitY    - location
     * @param hitZ    - location
     * @return {@link Point}
     */
    public static Point getClickedFace(Byte hitSide, float hitX, float hitY, float hitZ)
    {
        switch (hitSide)
        {
            case 0:
                return new Point(1 - hitX, hitZ);
            case 1:
                return new Point(hitX, hitZ);
            case 2:
                return new Point(1 - hitX, 1 - hitY);
            case 3:
                return new Point(hitX, 1 - hitY);
            case 4:
                return new Point(hitZ, 1 - hitY);
            case 5:
                return new Point(1 - hitZ, 1 - hitY);
            default:
                return new Point(0.5, 0.5);
        }
    }


    /**
     * Injects and eject();s data from the TileEntity.
     */
    public Tile inject(IBlockAccess access, int x, int y, int z)
    {
        if (access instanceof World)
        {
            staticTile.setWorldObj(((World) access));
        }

        staticTile.setAccess(access);
        staticTile.xCoord = x;
        staticTile.yCoord = y;
        staticTile.zCoord = z;

        TileEntity tileEntity = access.getTileEntity(x, y, z);
        if (tileEntity instanceof Tile)
        {
            ((Tile) tileEntity).setBlock(this);
        }

        return tileEntity instanceof Tile ? (Tile) tileEntity : staticTile;
    }

    /**
     * Resets the cached location data to nil
     */
    public void eject()
    {
        staticTile.setWorldObj(null);
        staticTile.xCoord = 0;
        staticTile.yCoord = 0;
        staticTile.zCoord = 0;
    }

    /**
     * Outputs an error to console with location data
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param msg
     * @param e
     */
    protected void outputError(World world, int x, int y, int z, String msg, Throwable e)
    {
        String dim = "null";
        if (world != null && world.provider != null)
        {
            dim = "" + world.provider.dimensionId;
        }
        Engine.logger().error("Error: " + msg + " \nLocation[" + dim + "w " + x + "x " + y + "y " + z + "z" + "]", e);
    }
}
