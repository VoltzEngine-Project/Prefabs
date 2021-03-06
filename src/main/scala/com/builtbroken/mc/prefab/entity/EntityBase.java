package com.builtbroken.mc.prefab.entity;

import com.builtbroken.jlib.data.network.IByteBufWriter;
import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.api.data.SensorType;
import com.builtbroken.mc.api.entity.IEntity;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketEntity;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.DamageUtility;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Base entity class to be shared by most entities
 * Created by robert on 1/24/2015.
 */
public abstract class EntityBase extends Entity implements IPacketIDReceiver, IEntity, IByteBufWriter
{
    private static final int PACKET_DESC = -1;

    /** Does the entity have HP to take damage. */
    protected boolean hasHealth = false;

    public EntityBase(World world)
    {
        super(world);
    }

    @Override
    protected void entityInit()
    {
        this.dataWatcher.addObject(6, getMaxHealth());
    }

    public void setHealth(float hp)
    {
        this.dataWatcher.updateObject(6, Float.valueOf(MathHelper.clamp_float(hp, 0.0F, this.getMaxHealth())));
    }

    public float getHealth()
    {
        return this.dataWatcher.getWatchableObjectFloat(6);
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
        this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
        this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
        this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
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
        if (worldObj.isRemote)
        {
            //Updates client if cargo changes
            if (id == PACKET_DESC)
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
        final PacketEntity entity = new PacketEntity(this).add(PACKET_DESC).add(this);
        Engine.packetHandler.sendToAllAround(entity, (IWorldPosition) this, 64);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf)
    {
        writeDescData(buf);
        return buf;
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
        return worldObj;
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
