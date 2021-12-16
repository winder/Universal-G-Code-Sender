package com.willwinder.universalgcodesender.model.events;

import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.UGSEvent;

public class AlarmEvent extends UGSEvent {
    private final Alarm alarm;

    public AlarmEvent(Alarm alarm) {
        super(EventType.ALARM_EVENT);
        this.alarm = alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }
}
