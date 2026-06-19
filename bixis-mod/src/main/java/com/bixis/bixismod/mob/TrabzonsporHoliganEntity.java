package com.bixis.bixismod.mob;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Trabzonspor takımından holigan mob. Ambient sesi yok. */
public class TrabzonsporHoliganEntity extends HoliganEntity {

    public TrabzonsporHoliganEntity(EntityType<? extends TrabzonsporHoliganEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Team getHoliganTeam() { return Team.TRABZONSPOR; }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() { return null; }
}
