package com.willwinder.ugs.platform.probe.stateful;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class Node<State extends Enum<State>, EventType extends Enum<EventType>, Context> {
  private final Map<EventType, Node<State, EventType, Context>> neighbors;
  private final List<StatefulFunction<Context>> onEnterListeners;
  private final List<StatefulFunction<Context>> onExitListeners;
  private final State state;

  Node(State state) {
    this.state = state;
    neighbors = new HashMap<>();
    onEnterListeners = new LinkedList<>();
    onExitListeners = new LinkedList<>();
  }

  public State getState() {
    return state;
  }

  public Node<State, EventType, Context> getNeighbor(EventType eventType) {
    return neighbors.get(eventType);
  }

  public void onEnter(Context c) {
    onEnterListeners.forEach(f -> f.transition(c));
  }

  public void onExit(Context c) {
    onExitListeners.forEach(f -> f.transition(c));
  }

  public void addNeighbor(EventType eventType, Node<State, EventType, Context> destination) {
    neighbors.put(eventType, destination);
  }

  public void addOnEnterListener(StatefulFunction<Context> onEnterListener) {
    onEnterListeners.add(onEnterListener);
  }

  public void addOnExitListener(StatefulFunction<Context> onExitListener) {
    onExitListeners.add(onExitListener);
  }
}
