package com.willwinder.ugs.platform.probe.stateful;

/**
 * A callback to execute during state transition.
 *
 * @param <Context> A context object which may be provided when applying an event.
 */
@FunctionalInterface
public interface StatefulFunction<Context> {
    public void transition(Context c);
}
