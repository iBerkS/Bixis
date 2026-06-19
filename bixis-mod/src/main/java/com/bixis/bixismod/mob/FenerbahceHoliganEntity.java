package com.bixis.bixismod.mob;

import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Fenerbahçe takımından holigan mob. */
public class FenerbahceHoliganEntity extends HoliganEntity {

    public FenerbahceHoliganEntity(EntityType<? extends FenerbahceHoliganEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Team getHoliganTeam() { return Team.FENERBAHCE; }

    @Override
    protected SoundEvent getAmbientSound() { return BixisSounds.HOLIGAN_FENERBAHCE_SPAWN.get(); }
}
