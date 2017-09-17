package com.willwinder.ugs.platform.probe.stateful;

/**
 * A simple event based state machine.
 *
 * @param <State> The state of the entity
 * @param <EventType> The event type to be handled
 * @param <Context> A context object which may optionally be provided when applying an event.
 */
public final class StateMachine<State extends Enum<State>, EventType extends Enum<EventType>, Context> {
  private Node<State, EventType, Context> root;
  private boolean throwOnNoOpApply;

  StateMachine(Node<State, EventType, Context> root, boolean throwOnNoOpApply) {
    this.root = root;
    this.throwOnNoOpApply = throwOnNoOpApply;
  }

  /**
   * Apply an event to the state machine.
   *
   * @param eventType The event type to be handled
   */
  public void apply(EventType eventType) {
      apply(eventType, null);
  }

  public void apply(EventType eventType, Context c) {
    Node<State, EventType, Context> nextNode = root.getNeighbor(eventType);

    if (nextNode == null) {
      if (throwOnNoOpApply) {
          throw new UnexpectedEventTypeException(root.getState(), eventType);
      }
      else {
          return;
      }
    }

    root.onExit(c);
    root = nextNode;
    root.onEnter(c);
  }

  /**
   * @return The current state of the state machine
   */
  public State getState() {
    return root.getState();
  }
}
