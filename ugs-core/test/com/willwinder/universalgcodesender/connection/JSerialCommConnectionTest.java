package com.willwinder.universalgcodesender.connection;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class JSerialCommConnectionTest {

    @Test
    public void serialEvent_shouldDispatchDisconnectEventsWhenDeviceIsClosed() {
        SerialPort serialPort = mock(SerialPort.class);
        JSerialCommConnection connection = new JSerialCommConnection(serialPort);

        IConnectionListener listener = mock(IConnectionListener.class);
        connection.addListener(listener);

        SerialPortEvent event = new SerialPortEvent(serialPort, SerialPort.LISTENING_EVENT_PORT_DISCONNECTED);
        connection.serialEvent(event);

        verify(listener, times(1)).onConnectionClosed();
        verifyNoMoreInteractions(listener);

        // This to ensure that not double disconnect commands are dispatched
        verify(serialPort, times(0)).removeDataListener();
        verify(serialPort, times(0)).closePort();
        verifyNoMoreInteractions(serialPort);
    }

    @Test
    public void serialEvent_shouldDispatchDisconnectEventsWhenDisconnected() throws Exception {
        SerialPort serialPort = mock(SerialPort.class);
        doNothing().when(serialPort).removeDataListener();
        JSerialCommConnection connection = new JSerialCommConnection(serialPort);

        IConnectionListener listener = mock(IConnectionListener.class);
        connection.addListener(listener);

        connection.closePort();

        verify(serialPort, times(1)).removeDataListener();
        verify(serialPort, times(1)).closePort();
        verifyNoMoreInteractions(serialPort);

        // This to ensure that not double disconnect commands are dispatched
        verify(listener, times(0)).onConnectionClosed();
        verifyNoMoreInteractions(listener);
    }

}