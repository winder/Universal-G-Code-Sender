/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.services.interceptor;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

/**
 * A {@link CommandInterceptor} that pauses the stream on tool change commands ({@code M6}), moves the
 * machine to a safe height and to a tool change location, waits for the operator to change the tool and
 * optionally runs a tool length probe before the stream is resumed.
 *
 * <p>The {@code M6} word is stripped from the triggering command, but any tool selection ({@code T2}) is
 * issued directly to the controller so the gcode state reflects the requested tool, which is used to tell
 * the operator which tool to change to.
 *
 * @author Joacim Breiler
 */
public class ToolChangeInterceptor implements CommandInterceptor {
    public static final String STEP_CHANGE_TOOL = "toolChange.changeTool";
    public static final String STEP_CONTINUE = "toolChange.continue";

    // Matches an M6/M06 tool change word. It is not preceded by a letter (so it is a word on its own and not
    // part of e.g. a comment) and not followed by a digit (so M60, M61, ... are not treated as tool changes).
    // The word may be directly followed by another word such as a tool selection, as in "M6T1" or "M06T1".
    private static final Pattern TOOL_CHANGE_PATTERN = Pattern.compile("(?i)(?<![A-Z])M0?6(?![0-9])");
    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(2);

    private final BooleanSupplier enabled;

    public ToolChangeInterceptor(BooleanSupplier enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean matches(GcodeCommand command) {
        if (!enabled.getAsBoolean()) {
            return false;
        }

        return TOOL_CHANGE_PATTERN.matcher(command.getCommandString()).find()
                || TOOL_CHANGE_PATTERN.matcher(command.getOriginalCommandString()).find();
    }

    @Override
    public void execute(InterceptContext context) throws InterceptException {
        IController controller = context.getController();

        try {
            BackendAPI backend = context.getBackend();

            // Issue any tool selection (e.g. "T2") with the M6 stripped so the gcode state reflects the
            // requested tool, which is used to tell the operator which tool to change to.
            String toolCommand = stripToolChangeWord(context.getTriggerCommand().getCommandString());
            if (!StringUtils.isBlank(toolCommand)) {
                sendAndWait(controller, toolCommand);
            }

            // Move to a safe height and turn of spindle
            String safeZCommand = "G90 G0 " + PartialPosition.builder(UnitUtils.Units.MM).setZ(backend.getGcodeStats().getMax().getZ()).build().getFormattedGCode();
            sendAndWait(controller, safeZCommand);
            sendAndWait(controller, "M5");

            context.awaitUserPrompt(new InterceptorPrompt(
                    STEP_CHANGE_TOOL,
                    List.of(UserResponse.CONTINUE, UserResponse.ABORT)));

            context.awaitUserPrompt(new InterceptorPrompt(
                    STEP_CONTINUE,
                    List.of(UserResponse.CONTINUE, UserResponse.ABORT)));

            // Move to safe height again and resume stream
            sendAndWait(controller, safeZCommand);
        } catch (InterceptAbortedException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterceptException("Tool change routine was interrupted", e);
        } catch (Exception e) {
            throw new InterceptException("Tool change routine failed: " + e.getMessage(), e);
        }
    }

    private static String stripToolChangeWord(String command) {
        String stripped = TOOL_CHANGE_PATTERN.matcher(command).replaceAll(" ");
        return stripped.replaceAll("\\s+", " ").trim();
    }

    private void sendAndWait(IController controller, String gcode) throws Exception {
        GcodeCommand command = controller.createCommand(gcode);
        ControllerUtils.sendAndWaitForCompletion(controller, command, COMMAND_TIMEOUT);
        if (command.isError()) {
            throw new InterceptException("Controller rejected command: " + gcode);
        }
    }
}
