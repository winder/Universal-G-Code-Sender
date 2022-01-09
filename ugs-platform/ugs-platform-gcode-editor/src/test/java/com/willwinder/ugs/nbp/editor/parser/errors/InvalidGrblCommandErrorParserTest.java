package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvalidGrblCommandErrorParserTest {

    private InvalidGrblCommandErrorParser parser;

    @Before
    public void setupParser() {
        FileObject fileObject = mock(FileObject.class);
        BackendAPI backendAPI = mock(BackendAPI.class);
        IController grblController = mock(GrblController.class);
        when(backendAPI.getController()).thenReturn(grblController);
        when(backendAPI.isConnected()).thenReturn(true);

        parser = new InvalidGrblCommandErrorParser(fileObject, backendAPI);
    }

    @Test
    public void shouldGenerateWarningOnUnknownMovementCommand() {
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G05"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G5"), 0);

        assertEquals(2, parser.getErrors().size());
        assertEquals(Severity.WARNING, parser.getErrors().get(0).getSeverity());
    }

    @Test
    public void shouldGenerateWarningOnUnknownMachineCommand() {
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M6"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M06"), 0);

        assertEquals(2, parser.getErrors().size());
        assertEquals(Severity.WARNING, parser.getErrors().get(0).getSeverity());
    }

    @Test
    public void shouldNotGenerateWarningOnValidCommandPatterns() {
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G0"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G00"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "g00"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "g0"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G1"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G01"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M0"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M00"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M1"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MACHINE, "M01"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "L2"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "L02"), 0);
        assertEquals(0, parser.getErrors().size());
    }

    @Test
    public void shouldNotGenerateWarningsOnValidMovementCommand() {
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G38.2"), 0);
        assertEquals(0, parser.getErrors().size());
    }

    @Test
    public void shouldNotGenerateWarningsOnValidMachineCommand() {
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "M0"), 0);
        assertEquals(0, parser.getErrors().size());
    }

    @Test
    public void shouldNotGenerateWarningsOnToolCommands() {
        parser.handleToken(generateToken(GcodeTokenId.TOOL, "XXX"), 0);
        assertEquals(0, parser.getErrors().size());
    }

    private Token<GcodeTokenId> generateToken(final GcodeTokenId tokenId, final String command) {
        return new AbstractToken<GcodeTokenId>(new WrapTokenId<>(tokenId)) {
            @Override
            public CharSequence text() {
                return command;
            }

            @Override
            public int length() {
                return command.length();
            }
        };
    }

}