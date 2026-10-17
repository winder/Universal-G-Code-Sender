package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.listeners.MessageType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ControllerErrorNotifierTest {

    @Test
    public void onMessage_shouldShowErrorMessagesAsToast() {
        List<String> toasts = new ArrayList<>();
        ControllerErrorNotifier notifier = new ControllerErrorNotifier((message, _) -> toasts.add(message), () -> {});

        notifier.onMessage(MessageType.ERROR, "An error was detected while sending 'G0': (error:1) Letter was not found.");

        assertThat(toasts).containsExactly("An error was detected while sending 'G0': (error:1) Letter was not found.");
    }

    @Test
    public void onMessage_shouldOpenConsoleWhenToastIsClicked() {
        AtomicBoolean consoleOpened = new AtomicBoolean(false);
        List<Runnable> clickHandlers = new ArrayList<>();
        ControllerErrorNotifier notifier = new ControllerErrorNotifier((_, onClick) -> clickHandlers.add(onClick), () -> consoleOpened.set(true));

        notifier.onMessage(MessageType.ERROR, "error:20");

        assertThat(clickHandlers).hasSize(1);
        clickHandlers.getFirst().run();
        assertThat(consoleOpened).isTrue();
    }

    @Test
    public void onMessage_shouldIgnoreMessagesThatAreNotErrors() {
        List<String> toasts = new ArrayList<>();
        ControllerErrorNotifier notifier = new ControllerErrorNotifier((message, _) -> toasts.add(message), () -> {});

        notifier.onMessage(MessageType.INFO, "ok");
        notifier.onMessage(MessageType.VERBOSE, "<Idle|MPos:0,0,0>");

        assertThat(toasts).isEmpty();
    }

    @Test
    public void onMessage_shouldIgnoreEmptyErrorMessages() {
        List<String> toasts = new ArrayList<>();
        ControllerErrorNotifier notifier = new ControllerErrorNotifier((message, _) -> toasts.add(message), () -> {});

        notifier.onMessage(MessageType.ERROR, "");

        assertThat(toasts).isEmpty();
    }
}
