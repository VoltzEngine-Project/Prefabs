package com.builtbroken.mc.prefab.entity.selector;

import net.minecraft.entity.Entity;

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
}
