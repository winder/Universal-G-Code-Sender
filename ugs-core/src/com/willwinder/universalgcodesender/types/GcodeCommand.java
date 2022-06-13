/*
    Copyright 2012-2022 Will Winder

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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An object representing a single GcodeCommand.
 *
 * @author wwinder
 */
public class GcodeCommand {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private Set<CommandListener> listeners;
    private String command;
    private String originalCommand;
    private String response;

    private boolean isSent = false;
    private boolean isOk = false;
    private boolean isError = false;
    private boolean isSkipped = false;
    private boolean isDone = false;
    private int commandNum;

    /**
     * If this is a generated command not apart of any program such as jog or settings commands
     */
    private boolean isGenerated;
    private String comment;
    private boolean isTemporaryParserModalChange = false;
    private int id = ID_GENERATOR.getAndIncrement();

    public GcodeCommand(String command) {
        this(command, -1);
    }

    public GcodeCommand(String command, int commandNumber) {
        this(command, null, GcodePreprocessorUtils.parseComment(command), commandNumber, true);
    }

    /**
     * @param command         the command that will be sent to the controller
     * @param originalCommand the
     * @param comment         either a comment
     * @param commandNumber   the index of command, usually the line number in a file
     * @param isGenerated     if this is a generated command not a part of any program (ie. jog, action or settings commands).
     */
    public GcodeCommand(String command, String originalCommand, String comment, int commandNumber, boolean isGenerated) {
        this.command = command;
        this.originalCommand = originalCommand;
        this.comment = comment;
        this.commandNum = commandNumber;
        this.isGenerated = isGenerated;
    }

    /** Setters. */
    public void setCommand(String command) {
        this.command = command;
    }
    
    public void setCommandNumber(int i) {
        this.commandNum = i;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }

    public void appendResponse(String response) {
        if (this.response == null) {
            this.response = response;
        } else {
            this.response += "\n" + response;
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
            listeners.forEach(commandListener -> commandListener.onDone(this));
            listeners.clear();
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

    public void setGenerated(boolean generated) {
        isGenerated = generated;
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
