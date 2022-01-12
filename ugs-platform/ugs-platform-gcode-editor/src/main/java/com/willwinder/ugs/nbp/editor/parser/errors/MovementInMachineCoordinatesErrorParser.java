package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.List;

public class MovementInMachineCoordinatesErrorParser implements ErrorParser {

    private final FileObject fileObject;
    private final BackendAPI backend;
    private final List<GcodeError> errorList = new ArrayList<>();

    public MovementInMachineCoordinatesErrorParser(FileObject fileObject) {
        this.fileObject = fileObject;

        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    @Override
    public void handleToken(Token<?> token, int line) {
        if (StringUtils.equalsIgnoreCase("G53", token.text()) && !isHomingEnabled()) {
            int offset = token.offset(null);
            GcodeError error = new GcodeError("movement-in-machine-coordinates-without-homing",
                    "Using movement in machine coordinates without homing enabled",
                    "Using movement in machine coordinates without homing enabled could be hazardous as there is no known machine reference point.",
                    fileObject,
                    offset,
                    offset + token.length(),
                    true,
                    Severity.WARNING);

            errorList.add(error);
        }
    }

    private boolean isHomingEnabled() {
        try {
            return backend.getController() == null ||
                    backend.getController().getFirmwareSettings() == null ||
                    backend.getController().getFirmwareSettings().isHomingEnabled();
        } catch (FirmwareSettingsException e) {
            return false;
        }
    }

    @Override
    public List<GcodeError> getErrors() {
        return errorList;
    }
}
