package com.builtbroken.mc.prefab.entity.selector;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Very basic entity selector designed for use with World.selectEntitiesWithinAABB or Entity AI target selectors.
 * Created by robert on 2/10/2015.
 */
public abstract class EntitySelector implements Predicate<Entity>
{
    protected boolean lock;

    @Override
    public boolean test(Entity entity)
    {
        return isEntityApplicable(entity);
    }

    public boolean isEntityApplicable(Entity entity)
    {
        return entity != null;
    }

    public void lock()
    {
        this.lock = true;
    }

    public List<Entity> getEntities(Entity entity, double range)
    {
        //Expand entity bounds to get check area
        AxisAlignedBB bounds = entity.getEntityBoundingBox().expand(range, range, range);

        //Get entities in area
        List<Entity> list = entity.getEntityWorld().getEntitiesWithinAABB(Entity.class, bounds);

        //Remove entities that do not apply
        Iterator<Entity> it = list.iterator();
        while (it.hasNext())
        {
            if (!test(it.next()))
            {
                it.remove();
            }
        }
        return list;
    }
}
