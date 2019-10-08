package com.willwinder.ugs.nbp.editor.parser.errors;

import com.willwinder.ugs.nbp.editor.lexer.GcodeTokenId;
import com.willwinder.ugs.nbp.editor.parser.GcodeError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.lib.lexer.token.TextToken;
import org.openide.filesystems.FileObject;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FeedRateMissingErrorParserTest {
    private FeedRateMissingErrorParser parser;

    @Mock
    private FileObject fileObject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parser = new FeedRateMissingErrorParser(fileObject);
    }

    @Test
    public void returnEmptyErrorListWhenNoMovementIsDefined() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

    @Test
    public void returnEmptyErrorListWhenOnlyFeedRateIsDefined() {
        parser.handleToken(generateToken(GcodeTokenId.PARAMETER, "F100"), 0);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

    @Test
    public void returnEmptyErrorListWhenMovementG0() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G0"), 1);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

    @Test
    public void returnErrorWhenMovementG1WithoutFeedRate() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G1"), 1);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(1, errors.size());
    }

    @Test
    public void returnErrorWhenMovementG3WithoutFeedRate() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G3"), 1);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(1, errors.size());
    }

    @Test
    public void returnEmptyErrorWhenMovementWithFeedRateOnSameLine() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G1"), 1);
        parser.handleToken(generateToken(GcodeTokenId.PARAMETER, "F100"), 1);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

    @Test
    public void returnEmptyErrorWhenFeedRateBeforeMovement() {
        parser.handleToken(generateToken(GcodeTokenId.COMMENT, "; Hello!"), 0);
        parser.handleToken(generateToken(GcodeTokenId.PARAMETER, "F100"), 1);
        parser.handleToken(generateToken(GcodeTokenId.MOVEMENT, "G1"), 2);

        List<GcodeError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

    private Token<GcodeTokenId> generateToken(GcodeTokenId tokenId, String text) {
        return new TextToken<>(new WrapTokenId<>(tokenId), text);
    }
}