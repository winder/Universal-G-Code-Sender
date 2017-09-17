package com.willwinder.ugs.platform.probe.stateful;

/**
 * Thrown when an unexpected event is applied to a state machine.
 */
public final class UnexpectedEventTypeException extends RuntimeException {
  private final Enum<?> eventType;
  private final Enum<?> state;

  /**
   * @param state The state on which the unexpected event was applied.
   * @param eventType The event type of the unexpected event
   */
  public UnexpectedEventTypeException(Enum<?> state, Enum<?> eventType) {
    this.state = state;
    this.eventType = eventType;
  }

  @Override
  public String getMessage() {
    return "An unexpected event of type " + eventType.name() +
      " happened during the " + state.name() + " state";
  }

  /**
   * @return The state on which the unexpected event was applied
   */
  public Enum<?> getState() {
    return state;
  }

  /**
   * @return The event type of the unexpected event
   */
  public Enum<?> getEventType() {
    return eventType;
  }
}
