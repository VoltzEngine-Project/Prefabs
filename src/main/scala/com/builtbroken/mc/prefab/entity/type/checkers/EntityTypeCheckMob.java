package com.builtbroken.mc.prefab.entity.type.checkers;

import com.builtbroken.mc.prefab.entity.type.EntityTypeCheck;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class EntityTypeCheckMob extends EntityTypeCheck
{
    public EntityTypeCheckMob()
    {
        super("mobs");
    }

    @Override
    public boolean isEntityApplicable(Entity entity)
    {
        return entity instanceof IMob;
    }
}
