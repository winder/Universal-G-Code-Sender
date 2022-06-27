package com.willwinder.universalgcodesender.utils;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;

import org.junit.Test;

public class SemanticVersionTest {

	@Test
	public void testEquality() throws ParseException {
		SemanticVersion v1 = new SemanticVersion("1.2.3");
		SemanticVersion v2 = new SemanticVersion("1.2.3");
		assertEquals(v1, v2);
	}

	@Test(expected = NumberFormatException.class)
	public void testWontParse() throws ParseException {
		new SemanticVersion("won't parse");
	}

	@Test(expected = NumberFormatException.class)
	public void testEmptyMajor() throws ParseException {
		new SemanticVersion(".1.2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOutOfBounds() {
		new SemanticVersion(-1, 0, 0);
	}

	@Test
	public void testParsePlain() throws ParseException {
		SemanticVersion v = new SemanticVersion("1.2.3");
		assertEquals(1, v.getMajor());
		assertEquals(2, v.getMinor());
		assertEquals(3, v.getPatch());
		assertEquals("1.2.3", v.toString());

		v = new SemanticVersion("11.22.33");
		assertEquals(11, v.getMajor());
		assertEquals(22, v.getMinor());
		assertEquals(33, v.getPatch());
		assertEquals("11.22.33", v.toString());
	}

	@Test
	public void testNewer() {
		SemanticVersion[] inorder = { new SemanticVersion(0, 1, 4),
				new SemanticVersion(1, 1, 1), new SemanticVersion(1, 2, 1),
				new SemanticVersion(1, 2, 3) };

		SemanticVersion[] wrongorder = { inorder[0], inorder[3], inorder[1],
				inorder[2] };

		Arrays.sort(wrongorder);
		assertArrayEquals(inorder, wrongorder);
	}
}
