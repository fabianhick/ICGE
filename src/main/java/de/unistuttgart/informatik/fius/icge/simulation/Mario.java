/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 * 
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.simulation;

import de.unistuttgart.informatik.fius.icge.simulation.inspection.InspectionAttribute;


public class Mario extends GreedyEntity {
    
    private int coinCount = 0;
    
    public Mario(Simulation sim) {
        super(sim, EntityType.MARIO);
    }
    
    @Override
    public boolean canCollectType(EntityType type) {
        return type == EntityType.COIN;
    }
    
    @Override
    public void collected(EntityType type) {
        this.coinCount++;
    }
    
    @Override
    public boolean canDropType(EntityType type) {
        return type == EntityType.COIN;
    }
    
    @Override
    public void dropped(EntityType type) {
        this.coinCount--;
    }
}
