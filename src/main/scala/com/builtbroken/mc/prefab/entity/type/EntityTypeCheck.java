package com.builtbroken.mc.prefab.entity.type;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public abstract class EntityTypeCheck implements IEntitySelector
{
    public final String key;

    public EntityTypeCheck(String key)
    {
        this.key = key;
    }

    @Override
    public abstract boolean isEntityApplicable(Entity entity);
}
