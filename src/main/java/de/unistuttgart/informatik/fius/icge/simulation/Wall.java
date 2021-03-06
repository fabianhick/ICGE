/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 * 
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.simulation;

import de.unistuttgart.informatik.fius.icge.territory.EntityState;
import de.unistuttgart.informatik.fius.icge.territory.WorldObject.Direction;

/**
 * Solid entity with the sprite of a wall
 */
public class Wall extends Entity {
    /**
     * The entity state for walls
     */
    public static class WallState implements EntityState {
        @Override
        public Entity createEntity(Simulation sim) {
            return new Wall(sim);
        }

        @Override
        public boolean isSolid() {
            return true;
        }
    }

    /**
     * Creates a new wall in the given simulation
     * 
     * @param sim
     *            The simulation to create the wall in
     */
    public Wall(Simulation sim) {
        super(sim);
    }

    @Override
    public EntityState state() {
        return new WallState();
    }

    /**
     * @see de.unistuttgart.informatik.fius.icge.simulation.Entity#spawn(int, int,
     *      de.unistuttgart.informatik.fius.icge.territory.WorldObject.Direction)
     */
    @Override
    public void spawn(int column, int row, Direction direction) throws EntityAlreadyAlive, CellBlockedBySolidEntity {
        if (!this.simulation().entitiesAt(column, row).isEmpty()) {
            throw new CellNotEmpty();
        }

        super.spawn(column, row, direction);
    }

    /**
     * A exception for when a cell is not empty
     */
    public static class CellNotEmpty extends RuntimeException {
        private static final long serialVersionUID = -4741977214724813309L;
    }
}
