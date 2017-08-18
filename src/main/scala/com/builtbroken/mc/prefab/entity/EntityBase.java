package com.builtbroken.mc.prefab.entity;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.api.data.SensorType;
import com.builtbroken.mc.api.entity.IEntity;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketEntity;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.DamageUtility;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * Base entity class to be shared by most entities
 * Created by robert on 1/24/2015.
 */
public abstract class EntityBase extends Entity implements IPacketIDReceiver, IEntity
{
    /** Does the entity have HP to take damage. */
    protected boolean hasHealth = false;

    private static final DataParameter<Float> HEALTH = EntityDataManager.<Float>createKey(EntityLivingBase.class, DataSerializers.FLOAT);

    public EntityBase(World world)
    {
        super(world);
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(HEALTH, Float.valueOf(0));
    }

    public float getHealth()
    {
        return ((Float)this.dataManager.get(HEALTH)).floatValue();
    }

    public void setHealth(float health)
    {
        this.dataManager.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0F, this.getMaxHealth())));
    }

    public float getMaxHealth()
    {
        return 5;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        if (hasHealth && DamageUtility.canHarm(this, source, damage))
        {
            this.setHealth(Math.max(getHealth() - damage, 0));
            if (getHealth() <= 0)
            {
                onDestroyedBy(source, damage);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when the entity is killed
     */
    protected void onDestroyedBy(DamageSource source, float damage)
    {
        this.setDead();
    }

    /**
     * Sets the position based on the bounding box
     */
    protected void alignToBounds()
    {
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = this.getEntityBoundingBox().minY + (double) this.getYOffset() - (double) this.height;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }

    /**
     * Gets the predicted position
     *
     * @param t - number of ticks to predicted
     * @return predicted position of the project
     */
    public Pos getPredictedPosition(int t)
    {
        Pos newPos = new Pos((Entity) this);

        for (int i = 0; i < t; i++)
        {
            newPos.add(motionX, motionY, motionZ);
        }

        return newPos;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        setHealth(nbt.getFloat("health"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setFloat("health", this.getHealth());
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (world.isRemote)
        {
            //Updates client if cargo changes
            if (id == -1)
            {
                readDescData(buf);
                return true;
            }
        }
        return false;
    }

    /**
     * Sends basic data that describes the entity
     */
    protected void sentDescriptionPacket()
    {
        final PacketEntity entity = new PacketEntity(this, -1);
        writeDescData(entity.data());
        Engine.packetHandler.sendToAllAround(entity, (IWorldPosition) this, 64);
    }

    /**
     * Writes desc data to packet
     *
     * @param buffer - write area
     */
    public void writeDescData(ByteBuf buffer)
    {
        if (this instanceof IEntityAdditionalSpawnData)
        {
            ((IEntityAdditionalSpawnData) this).writeSpawnData(buffer);
        }
    }

    /**
     * Reads desc data from packet
     *
     * @param buffer - data
     */
    public void readDescData(ByteBuf buffer)
    {
        if (this instanceof IEntityAdditionalSpawnData)
        {
            ((IEntityAdditionalSpawnData) this).readSpawnData(buffer);
        }
    }


    @Override
    public World oldWorld()
    {
        return world;
    }

    @Override
    public double x()
    {
        return posX;
    }

    @Override
    public double y()
    {
        return posY;
    }

    @Override
    public double z()
    {
        return posZ;
    }

    @Override
    public float getGeneralSize(SensorType type)
    {
        return width > height ? width : height;
    }

    @Override
    public Pos getVelocity()
    {
        return new Pos(motionX, motionY, motionZ); //TODO make wrapper object
    }
}
