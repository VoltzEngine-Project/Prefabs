package com.builtbroken.mc.prefab.entity.damage;

/**
 * Damage source where the enitty takes damage threw blood loss
 * Created by robert on 1/5/2015.
 */
public class DamageBleeding extends DamageSourceAbstract
{
    public DamageBleeding()
    {
        super("Bleeding");
        setDamageBypassesArmor();
        setDamageIsAbsolute();
    }
}
