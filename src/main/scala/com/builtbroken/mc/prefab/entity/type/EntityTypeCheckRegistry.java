package com.builtbroken.mc.prefab.entity.type;

import com.builtbroken.mc.prefab.entity.type.checkers.EntityTypeCheckBullet;
import com.builtbroken.mc.prefab.entity.type.checkers.EntityTypeCheckMob;
import com.builtbroken.mc.prefab.entity.type.checkers.EntityTypeCheckRocket;

import java.util.HashMap;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/23/2017.
 */
public class EntityTypeCheckRegistry
{
    public static HashMap<String, EntityTypeCheck> typeCheckers = new HashMap();

    static
    {
        register(new EntityTypeCheckMob());
        register(new EntityTypeCheckBullet());
        register(new EntityTypeCheckRocket());
    }

    public static void register(EntityTypeCheck typeCheck)
    {
        typeCheckers.put(typeCheck.key.toLowerCase(), typeCheck);
    }
}
