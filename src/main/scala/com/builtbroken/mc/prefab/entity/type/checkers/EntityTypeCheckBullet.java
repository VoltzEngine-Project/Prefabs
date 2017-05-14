package com.builtbroken.mc.prefab.entity.type.checkers;

import com.builtbroken.mc.api.data.EnumProjectileTypes;
import com.builtbroken.mc.api.entity.IBullet;
import com.builtbroken.mc.prefab.entity.type.EntityTypeCheck;
import net.minecraft.entity.Entity;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class EntityTypeCheckBullet extends EntityTypeCheck
{
    public EntityTypeCheckBullet()
    {
        super("bullets");
    }

    @Override
    public boolean isEntityApplicable(Entity entity)
    {
        if (entity instanceof IBullet)
        {
            return ((IBullet) entity).getProjectileType() == EnumProjectileTypes.BULLET;
        }
        return false;
    }
}
