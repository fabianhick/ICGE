/*
 * This source file is part of the FIUS ICGE project.
 * For more information see github.com/neumantm/ICGE
 *
 * Copyright (c) 2018 the ICGE project authors.
 */

package de.unistuttgart.informatik.fius.icge.event;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class EventDispatcher {

    private static ArrayList<Listening> _listenings = new ArrayList<>();
    private static ArrayDeque<Runnable> _afterwards = new ArrayDeque<>();
    private static int _raiseRecursionDepth = 0;

    public static synchronized EventListener addListener(Class<?> listensFor, EventListener listener) {
        if (!Event.class.isAssignableFrom(listensFor)) throw new IllegalArgumentException();
        _listenings.add(new Listening(listensFor, listener));
        return listener;
    }

    public static synchronized boolean removeListener(EventListener listener) {
        return _listenings.removeIf(entry -> entry.listener == listener);
    }

    public static synchronized void raise(Event e) throws RaiseAlreadyActive {
        EventDispatcher.raise(e, () -> {});
    }

    /**
     * Raises an event, then runs an `afterTask` and finally runs the
     * 
     * @param e
     * @param afterTask
     * @throws RaiseAlreadyActive
     */
    public static synchronized void raise(Event e, Runnable afterTask) throws RaiseAlreadyActive {
        if (EventDispatcher._raiseRecursionDepth != 0) {
            throw new RaiseAlreadyActive(); // recursive raise is not supported for now
        }

        // actual event hadling
        ++EventDispatcher._raiseRecursionDepth;
        try {
            for (Listening entry : new ArrayList<>(_listenings)) { // new list cause it might get modified concurrently
                if (entry.listensFor.isAssignableFrom(e.getClass())) {
                    if (!entry.listener.handle(e)) {
                        removeListener(entry.listener);
                    }
                }
            }
        } finally {
            --EventDispatcher._raiseRecursionDepth;
        }

        // First run the passed `afterTask` and then the tasks that have been scheduled via `EventDispatcher.afterwards()`
        afterTask.run();
        while (!_afterwards.isEmpty()) {
            _afterwards.pop().run();
        }
    }

    /**
     * Schedules a runnable that is run synchronously after the handling of the next event.
     * If an `EventHandler.raise()` call is currently active (i.e. an event is currently handled)
     * AND this method is called from the same thread where that event is raised (and handled) in,
     * the runnable is run after the handling of that event.
     * 
     * @param rn
     *            The runnable to schedule
     */
    public static synchronized void afterwards(Runnable rn) {
        if (EventDispatcher._raiseRecursionDepth == 0) {
            throw new RaiseNotActive(); // must have an active raise to schedule via `afterwards()`
        }

        _afterwards.add(rn);
    }

    private static class Listening {
        public Listening(Class<?> listensFor, EventListener listener) {
            this.listensFor = listensFor;
            this.listener = listener;
        }

        public final Class<?> listensFor;
        public final EventListener listener;
    }

    // Exceptions

    public static class RaiseAlreadyActive extends RuntimeException {
        private static final long serialVersionUID = 7713141366627046771L;
    }

    public static class RaiseNotActive extends RuntimeException {
        private static final long serialVersionUID = 2178481422042432110L;
    }

}
