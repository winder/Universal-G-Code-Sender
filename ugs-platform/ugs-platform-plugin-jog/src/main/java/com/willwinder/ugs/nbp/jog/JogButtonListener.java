package com.willwinder.ugs.nbp.jog;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class JogButtonListener implements MouseListener {

    private final long longPressDelay;
    private final long longPressInterval;
    private final IJogButtonAction onLongPressAction;
    private final IJogButtonAction onLongPressReleaseAction;
    private final IJogButtonAction onClickAction;
    private final ScheduledExecutorService longPressExecutor;
    private ScheduledFuture<?> longPressFuture;
    private boolean isLongPressed;

    /**
     * Constructor for creating a JogButtonListener
     *
     * @param longPressDelay           time in milliseconds before a button press will be
     *                                 considered a long press
     * @param longPressInterval        in what interval in milliseconds should the
     *                                 {@link JogButtonListener#onLongPressAction} be executed
     *                                 once the button is considered to be long press
     * @param onClickAction            the action to execute if the button was clicked
     *                                 (without a long press)
     * @param onLongPressAction        the action to execute when the button is considered long
     *                                 pressed. The action will be executed multiple times with
     *                                 {@link JogButtonListener#longPressInterval} as interval.
     * @param onLongPressReleaseAction the action to execute when the long press is released.
     */
    public JogButtonListener(long longPressDelay, long longPressInterval, IJogButtonAction onClickAction, IJogButtonAction onLongPressAction, IJogButtonAction onLongPressReleaseAction) {
        this.longPressDelay = longPressDelay;
        this.longPressInterval = longPressInterval;
        this.onClickAction = onClickAction;
        this.onLongPressAction = onLongPressAction;
        this.onLongPressReleaseAction = onLongPressReleaseAction;
        this.longPressExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * If the button was pressed and released.
     * @param e the event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!isLongPressed) {
            onClickAction.execute();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        isLongPressed = false;
        if (longPressFuture == null || longPressFuture.isDone()) {
            longPressFuture = longPressExecutor.scheduleAtFixedRate(
                    () -> {
                        isLongPressed = true;
                        onLongPressAction.execute();
                    },
                    longPressDelay,
                    longPressInterval,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if( longPressFuture != null ) {
            longPressFuture.cancel(true);
            longPressFuture = null;
        }

        if (isLongPressed) {
            try {
                onLongPressReleaseAction.execute();
            } catch (Exception ignored) {
                // Never mind
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
