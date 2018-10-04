/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 * 
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.workbench.tools;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import de.unistuttgart.informatik.fius.icge.simulation.Simulation;
import de.unistuttgart.informatik.fius.icge.workbench.swing.ToolBar;

/**
 * This class coordinates all the tools
 * 
 * @author Tim Neumann
 */
public class ToolHandler {
    private final ToolBar tb;
    
    private final List<Tool> tools = new ArrayList<>();
    
    private final List<JButton> buttons = new ArrayList<>();
    
    private int currentTool = 0;
    
    /**
     * Creates a tool handler.
     * 
     * @param toolBar
     *            The ToolBar to use.
     */
    public ToolHandler(ToolBar toolBar) {
        this.tb = toolBar;
        this.addTool(new SelectionTool());
        this.addTool(new SpawnMarioTool());
        this.addTool(new SpawnCoinTool());
        this.addTool(new DespawnTool());
        this.addTool(new WallCreationTool());
        this.addTool(new WallDemolishingTool());
        resetButtonBorders();
        this.buttons.get(0).setBorder(new BevelBorder(1));
    }
    
    private void addTool(Tool t) {
        final int index = this.tools.size();
        this.tools.add(t);
        JButton button = t.getToolBarButton();
        this.buttons.add(button);
        button.addActionListener(e -> {
            this.currentTool = index;
            this.resetButtonBorders();
            button.setBorder(new BevelBorder(1));
        });
        this.tb.addButton(button, index);
    }
    
    private void resetButtonBorders() {
        for (JButton b : this.buttons) {
            b.setBorder(new BevelBorder(0));
        }
    }
    
    /**
     * Handle a mouse pressed event
     * 
     * @param sim
     *            The simulation the mouse was pressed in
     * @param row
     *            The row the mouse was pressed in
     * @param column
     *            The column the mouse was pressed in
     */
    public void onMousePressed(Simulation sim, int row, int column) {
        Tool t = this.tools.get(this.currentTool);
        if (!(t instanceof AreaTool)) {
            synchronized (sim) {
                t.apply(sim, row, column);
            }
        }
    }
    
    /**
     * Handle a mouse released event
     * 
     * @param sim
     *            The simulation the mouse was pressed and released in
     * @param startRow
     *            The row the mouse was pressed in
     * @param endRow
     *            The column the mouse was pressed in
     * @param startColumn
     *            The row the mouse was released in
     * @param endColumn
     *            The column the mouse was released in
     */
    public void onMouseRealeased(Simulation sim, int startRow, int endRow, int startColumn, int endColumn) {
        Tool t = this.tools.get(this.currentTool);
        if (t instanceof AreaTool) {
            AreaTool at = (AreaTool) t;
            synchronized (sim) {
                at.applyToAll(sim, startRow, endRow, startColumn, endColumn);
            }
        }
    }
    
    /**
     * @return Whether the current tool is a area tool.
     */
    public boolean isCurrentToolArea() {
        Tool t = this.tools.get(this.currentTool);
        return (t instanceof AreaTool);
    }
    
}