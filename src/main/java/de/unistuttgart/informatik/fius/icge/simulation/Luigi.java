/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 *
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.simulation;

public class Luigi extends GreedyEntity {

    public Luigi(Simulation sim) {
        super(sim, EntityType.LUIGI);
    }

    @Override
    public boolean canCollectType(EntityType type) {
        return type == EntityType.COIN;
    }

    @Override
    public boolean canDropType(EntityType type) {
        return type == EntityType.COIN;
    }

}