/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 * 
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.workbench.tools;

import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.unistuttgart.informatik.fius.icge.event.Event;
import de.unistuttgart.informatik.fius.icge.event.EventDispatcher;
import de.unistuttgart.informatik.fius.icge.simulation.Entity.EntityEvent;
import de.unistuttgart.informatik.fius.icge.simulation.Simulation;
import de.unistuttgart.informatik.fius.icge.simulation.Simulation.PauseEvent;
import de.unistuttgart.informatik.fius.icge.simulation.Simulation.ResumeEvent;
import de.unistuttgart.informatik.fius.icge.simulation.Simulation.SimulationEvent;
import de.unistuttgart.informatik.fius.icge.workbench.Workbench.SetSimulationEvent;
import de.unistuttgart.informatik.fius.icge.workbench.WorkbenchView;
import de.unistuttgart.informatik.fius.icge.workbench.swing.Images;
import de.unistuttgart.informatik.fius.icge.workbench.swing.ToolBar;

/**
 * This class controls simulation playback
 * 
 */
public class SimulationController {
    private final ToolBar tb;
    private final WeakReference<WorkbenchView> _view;

    private final JButton playButton;

    private final JButton stepButton;
    private int _pendingSteps = 0;

    private final Image playImage;
    private final Image pauseImage;

    /**
     * Creates a tool handler.
     * 
     * @param toolBar
     *            The ToolBar to use.
     * @param view
     *            The view to use.
     */
    public SimulationController(ToolBar toolBar, WorkbenchView view) {
        this.tb = toolBar;
        this._view = new WeakReference<>(view);
        this.playButton = new JButton();
        this.playButton.addActionListener(this::playButtonPressed);
        this.playButton.setMargin(new Insets(0, 0, 0, 0));
        Image stepImage = Images.image("step.png");
        this.tb.addButton(this.playButton, -1);

        this.stepButton = new JButton();
        this.stepButton.addActionListener(this::stepButtonPressed);
        if (stepImage != null) {
            this.stepButton.setIcon(new ImageIcon(stepImage));
        }
        this.stepButton.setToolTipText("Simulate unitil next event");
        this.stepButton.setMargin(new Insets(0, 0, 0, 0));
        this.tb.addButton(this.stepButton, -1);

        this.playImage = Images.image("play.png");
        this.pauseImage = Images.image("pause.png");

        this.updatePlayButton(false);

        EventDispatcher.addListener(SetSimulationEvent.class, this::handleSetSimulation);
        EventDispatcher.addListener(EntityEvent.class, this::handleEntityEvent);
        EventDispatcher.addListener(PauseEvent.class, this::handlePause);
        EventDispatcher.addListener(ResumeEvent.class, this::handleResume);
    }

    private boolean handleSetSimulation(Event ev) {
        WorkbenchView view = this._view.get();
        if (view == null) {
            return false; // Unregister in case the view doesn't exist anymore
        }
        if (view == ((SetSimulationEvent) ev).view) {
            Simulation sim = view.simulation();
            updatePlayButton(sim != null && sim.running());
        }
        return true;
    }

    private boolean handleEntityEvent(Event ev) {
        WorkbenchView view = this._view.get();
        if (view == null) return false;
        synchronized (this) {
            Simulation sim = view.simulation();
            if (sim == null || this._pendingSteps == 0) return true;
            --this._pendingSteps;
            if (this._pendingSteps == 0) {
                EventDispatcher.afterwards(sim::pause);
            }
        }
        return true;
    }

    private boolean handleResume(Event ev) {
        WorkbenchView view = this._view.get();
        if (view == null) {
            return false; // Unregister in case the view doesn't exist anymore
        }
        SimulationEvent sev = (SimulationEvent) ev;
        if (view.simulation() == sev.simulation) this.updatePlayButton(true);
        return true;
    }

    private boolean handlePause(Event ev) {
        WorkbenchView view = this._view.get();
        if (view == null) {
            return false; // Unregister in case the view doesn't exist anymore
        }
        SimulationEvent sev = (SimulationEvent) ev;
        if (view.simulation() == sev.simulation) this.updatePlayButton(false);
        return true;
    }

    private synchronized void updatePlayButton(boolean isRunning) {
        if (isRunning) {
            if (this.pauseImage != null) {
                this.playButton.setIcon(new ImageIcon(this.pauseImage));
            }
            this.playButton.setToolTipText("Pause simulation");
        } else {
            this._pendingSteps = 0; // if something pauses the simulation, we wanna drop pending steps
            if (this.playImage != null) {
                this.playButton.setIcon(new ImageIcon(this.playImage));
            }
            this.playButton.setToolTipText("Start simulation");
        }
    }

    private synchronized void playButtonPressed(ActionEvent e) {
        WorkbenchView view = this._view.get();
        Simulation sim = view == null ? null : view.simulation();
        if (sim == null) return;
        this._pendingSteps = 0;
        synchronized (sim) {
            if (sim.running()) {
                sim.pause();
            } else {
                sim.resume();
            }
        }
    }

    private synchronized void stepButtonPressed(ActionEvent e) {
        WorkbenchView view = this._view.get();
        Simulation sim = view == null ? null : view.simulation();
        if (sim == null) return;
        synchronized (sim) {
            if (sim.running()) {
                ++this._pendingSteps;
            } else {
                this._pendingSteps = 1;
                sim.resume();
            }
        }
    }

}
