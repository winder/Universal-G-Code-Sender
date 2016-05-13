package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 2/12/14
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrblSettingsListener implements ControllerListener {
    
    public boolean inParsingMode;
    private boolean firstSettingReceived;
    public boolean sending;
    private final Object refreshLock = new Object();

    public final List<String> settings = new ArrayList<>();

    private final AbstractController controller;

    public GrblSettingsListener(AbstractController controller) {
        this.controller = controller;
        this.controller.addListener(this);
    }

    public List<String> getSettings() {

        if (settings.size() == 0) {
            synchronized (refreshLock) {
                if (settings.size() == 0)
                    refreshSettings();
            }
        }

        return settings;
    }

    public void refreshSettings() {
        try {
            this.sending = true;
            boolean ready;
            do {
                try {
                    this.controller.isReadyToStreamFile();
                    ready = true;
                } catch (Exception e) {
                    ready = false;
                }

            } while(!ready);

            GcodeCommand command = controller.createCommand(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND);
            controller.sendCommandImmediately(command);
            while (this.sending) {
                Thread.sleep(10);
            }
            while (this.inParsingMode) {
                Thread.sleep(1);
            }
        } catch (Exception e) {
            return;
        }

        return;
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {
        if (command.getCommandString().startsWith("$$")) {
            this.inParsingMode = true;
            this.firstSettingReceived = false;
            if (this.sending)
                this.sending = false;
            settings.clear();
        }
    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
        if (type == MessageType.VERBOSE)
            return;
        if (this.inParsingMode) {
            if (firstSettingReceived && msg.startsWith("ok")) {
                this.inParsingMode = false;
            } else if (msg.startsWith("$"))  {
                firstSettingReceived = true;
                settings.add(msg);
            }
        }
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
    }

    @Override
    public void postProcessData(int numRows) {
    }
}
