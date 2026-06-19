package com.bixis.bixismod.mob;

import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Galatasaray takımından holigan mob. */
public class GalatasarayHoliganEntity extends HoliganEntity {

    public GalatasarayHoliganEntity(EntityType<? extends GalatasarayHoliganEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Team getHoliganTeam() { return Team.GALATASARAY; }

    @Override
    protected SoundEvent getAmbientSound() { return BixisSounds.HOLIGAN_GALATASARAY_SPAWN.get(); }
}
