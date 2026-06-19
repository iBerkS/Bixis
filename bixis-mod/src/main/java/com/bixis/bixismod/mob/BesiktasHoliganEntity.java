package com.bixis.bixismod.mob;

import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Beşiktaş takımından holigan mob. */
public class BesiktasHoliganEntity extends HoliganEntity {

    public BesiktasHoliganEntity(EntityType<? extends BesiktasHoliganEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Team getHoliganTeam() { return Team.BESIKTAS; }

    @Override
    protected SoundEvent getAmbientSound() { return BixisSounds.HOLIGAN_BESIKTAS_SPAWN.get(); }
}
