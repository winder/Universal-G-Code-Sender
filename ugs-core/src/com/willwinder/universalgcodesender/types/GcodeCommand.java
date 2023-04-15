/*
    Copyright 2012-2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willwinder.universalgcodesender.types;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.willwinder.universalgcodesender.GrblUtils.isAlarmResponse;
import static com.willwinder.universalgcodesender.GrblUtils.isErrorResponse;
import static com.willwinder.universalgcodesender.GrblUtils.isOkResponse;


/**
 * An object representing a single command that expects it to end with a command status "ok" or "error" to
 * consider the command done.
 *
 * @author wwinder
 */
public class GcodeCommand {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    /**
     * A unique id for the command
     */
    private final int id = ID_GENERATOR.getAndIncrement();

    private final String command;
    private final String originalCommand;
    private final int commandNum;
    private final String comment;

    /**
     * If this is a generated command not a part of any program, typically used with jog or settings commands
     */
    private final boolean isGenerated;

    private Set<CommandListener> listeners;
    private String response;

    /**
     * If the command has been sent to the controller
     */
    private boolean isSent = false;

    /**
     * If controller response for the command was ok
     */
    private boolean isOk = false;

    /**
     * If controller response for the command resulted in an error
     */
    private boolean isError = false;

    /**
     * If the command was skipped and not sent to the controller
     */
    private boolean isSkipped = false;

    /**
     * If the command is done and no more controller processing is needed
     */
    private boolean isDone = false;

    private boolean isTemporaryParserModalChange = false;

    public GcodeCommand(String command) {
        this(command, -1);
    }

    public GcodeCommand(String command, int commandNumber) {
        this(command, null, GcodePreprocessorUtils.parseComment(command), commandNumber, true);
    }

    /**
     * Constructor for creating a gcode command that is a part of a gcode program
     *
     * @param command         the command that will be sent to the controller
     * @param originalCommand the original command before it was preprocessed
     * @param comment         either a comment
     * @param commandNumber   the index of command, usually the line number in a file
     */
    public GcodeCommand(String command, String originalCommand, String comment, int commandNumber) {
        this(command, originalCommand, comment, commandNumber, false);
    }

    /**
     * Constructor for creating a gcode command
     *
     * @param command         the command that will be sent to the controller
     * @param originalCommand the original command before it was preprocessed
     * @param comment         either a comment
     * @param commandNumber   the index of command, usually the line number in a file
     * @param isGenerated     if this is a generated command not a part of any program (ie. jog, action or settings commands).
     */
    protected GcodeCommand(String command, String originalCommand, String comment, int commandNumber, boolean isGenerated) {
        this.command = command.trim();
        this.originalCommand = originalCommand;
        this.comment = comment;
        this.commandNum = commandNumber;
        this.isGenerated = isGenerated;
    }

    /** Setters. */
    public void setResponse(String response) {
        this.response = null;
        appendResponse(response);
    }

    public void appendResponse(String response) {
        if (this.response == null) {
            this.response = response;
        } else {
            this.response += "\n" + response;
        }

        if (isOkResponse(response)) {
            setDone(true);
            setOk(true);
        } else if (isErrorResponse(response)|| isAlarmResponse(response)) {
            setDone(true);
            setOk(false);
            setError(true);
        }
    }

    public void setSent(boolean sent) {
        this.isSent = sent;
    }

    public void setSkipped(boolean skipped) {
        this.isSkipped = skipped;
    }

    public void addListener(CommandListener commandListener) {
        if (listeners == null) {
            listeners = Collections.synchronizedSet(new HashSet<>());
        }
        listeners.add(commandListener);
    }

    /**
     * Releases any resources allocated for this, making it eligible for garbage collection
     */
    public void dispose() {
        if (listeners != null) {
            listeners.clear();
        }
    }

    /**
     * Getters.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getCommandString() {
        return this.command;
    }

    public String getOriginalCommandString() {
        return this.originalCommand == null ? this.command : this.originalCommand;
    }

    public int getCommandNumber() {
        return this.commandNum;
    }

    public String getResponse() {
        return this.response;
    }

    public boolean isSent() {
        return this.isSent;
    }

    public boolean isOk() {
        return this.isOk;
    }

    public boolean isError() {
        return this.isError;
    }

    public boolean isSkipped() {
        return this.isSkipped;
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean hasComment() {
        return this.comment != null && this.comment.length() != 0;
    }

    public String getComment() {
        return this.comment;
    }

    /**
     * True for things like Jogging, false for commands from a gcode file
     */
    public boolean isTemporaryParserModalChange() {
        return isTemporaryParserModalChange;
    }

    /**
     * True for things like Jogging, false for commands from a gcode file
     */
    public void setTemporaryParserModalChange(boolean isGUICommand) {
        this.isTemporaryParserModalChange = isGUICommand;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
        if (isDone && listeners != null) {
            ThreadHelper.invokeLater(() -> {
                listeners.forEach(commandListener -> commandListener.onDone(this));
                listeners.clear();
            });
        }
    }

    public void setOk(boolean isOk) {
        this.isOk = isOk;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

    public int getId() {
        return id;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object);
    }
}
