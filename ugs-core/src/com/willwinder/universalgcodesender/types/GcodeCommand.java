/*
    Copyright 2012-2019 Will Winder

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

import java.util.concurrent.atomic.AtomicInteger;


/**
 * An object representing a single GcodeCommand.
 *
 * @author wwinder
 */
public class GcodeCommand {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private String command;
    private String originalCommand;
    private String response;
    private boolean sent = false;
    private boolean isOk = false;
    private boolean isError = false;
    private Integer commandNum = -1;
    private boolean isSkipped = false;

    /**
     * If this is a generated command not apart of any program such as jog or settings commands
     */
    private boolean isGenerated = true;
    private String comment;
    private boolean isTemporaryParserModalChange = false;
    private Integer id = ID_GENERATOR.getAndIncrement();

    public GcodeCommand(String command) {
        this(command, -1);
    }
    
    public GcodeCommand(String command, int num) {
        this.command = command;
        this.commandNum = num;
        this.comment = GcodePreprocessorUtils.parseComment(command);
    }

    /**
     *
     * @param command
     * @param originalCommand
     * @param comment
     * @param num
     * @param isGenerated If this is a generated command not a part of any program (ie. jog, action or settings commands).
     */
    public GcodeCommand(String command, String originalCommand, String comment, int num, boolean isGenerated) {
        this.command = command;
        this.originalCommand = originalCommand;
        this.comment = comment;
        this.commandNum = num;
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
    
    public void setSent(Boolean sent) {
        this.sent = sent;
    }
    
    public void setSkipped(Boolean skipped) {
        this.isSkipped = skipped;
    }
    
    /** Getters. */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,  ToStringStyle.SHORT_PREFIX_STYLE);
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
        return this.sent;
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
    
    public boolean isDone() {
        return (this.response != null);
    }

    public void setOk(boolean isOk) {
        this.isOk = isOk;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

    public Integer getId() {
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
