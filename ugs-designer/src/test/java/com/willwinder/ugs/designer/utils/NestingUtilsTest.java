package com.willwinder.ugs.designer.utils;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class NestingUtilsTest {

    @Test
    public void layout_shouldPlaceEntitiesSideBySideFromTheOrigin() {
        Rectangle first = new Rectangle(50, 50, 10, 10);
        Rectangle second = new Rectangle(-30, 70, 10, 10);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(first, second), new Point2D.Double(5, 7), 100, 2, false);

        assertPosition(5, 7, layout.placements().get(first));
        assertPosition(17, 7, layout.placements().get(second));
        assertEquals(22, layout.width(), 0.01);
        assertEquals(10, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldStartNewRowWhenWidthIsExceeded() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(0, 0, 10, 10);
        Rectangle third = new Rectangle(0, 0, 10, 10);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(first, second, third), new Point2D.Double(0, 0), 25, 2, false);

        assertPosition(0, 0, layout.placements().get(first));
        assertPosition(12, 0, layout.placements().get(second));
        assertPosition(0, 12, layout.placements().get(third));
        assertEquals(22, layout.width(), 0.01);
        assertEquals(22, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldPlaceLongestEntitiesFirst() {
        Rectangle small = new Rectangle(0, 0, 10, 5);
        Rectangle tall = new Rectangle(0, 0, 10, 20);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(small, tall), new Point2D.Double(0, 0), 100, 0, false);

        assertPosition(0, 0, layout.placements().get(tall));
        assertPosition(10, 0, layout.placements().get(small));
    }

    @Test
    public void layout_shouldFillTheRowBeforeStackingOnTop() {
        Rectangle wide = new Rectangle(0, 0, 30, 10);
        Rectangle tall = new Rectangle(0, 0, 10, 20);
        Rectangle narrow = new Rectangle(0, 0, 10, 10);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(wide, tall, narrow), new Point2D.Double(0, 0), 50, 0, false);

        assertPosition(0, 0, layout.placements().get(wide));
        assertPosition(30, 0, layout.placements().get(tall));
        assertPosition(40, 0, layout.placements().get(narrow));
        assertEquals(50, layout.width(), 0.01);
        assertEquals(20, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldPlaceEntityWiderThanTheAreaOnItsOwnRow() {
        Rectangle tooWide = new Rectangle(0, 0, 50, 10);
        Rectangle fits = new Rectangle(0, 0, 10, 10);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(fits, tooWide), new Point2D.Double(0, 0), 20, 1, false);

        assertPosition(0, 0, layout.placements().get(tooWide));
        assertPosition(0, 11, layout.placements().get(fits));
        assertFalse(layout.fitsWithin(20, 20));
    }

    @Test
    public void fitsWithin_shouldBeTrueWhenTheEntitiesFitTheArea() {
        List<Entity> entities = List.of(new Rectangle(0, 0, 10, 10), new Rectangle(0, 0, 10, 10));

        NestingUtils.NestingLayout layout = NestingUtils.layout(entities, new Point2D.Double(0, 0), 22, 2, false);

        assertTrue(layout.fitsWithin(22, 10));
        assertFalse(layout.fitsWithin(22, 9));
    }

    @Test
    public void layout_shouldLayTallEntityFlatWhenThatIsLowest() {
        Rectangle square = new Rectangle(0, 0, 10, 10);
        Rectangle tall = new Rectangle(0, 0, 5, 30);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(square, tall), new Point2D.Double(0, 0), 40, 0, true);

        assertEquals(90, layout.placements().get(tall).rotation() % 180, 0.01);
        assertPosition(0, 0, layout.placements().get(tall));
        assertEquals(0, layout.placements().get(square).rotation() % 90, 0.01);
        assertPosition(30, 0, layout.placements().get(square));
        assertEquals(40, layout.width(), 0.01);
        assertEquals(10, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldNotRotateWhenRotationIsNotAllowed() {
        Rectangle square = new Rectangle(0, 0, 10, 10);
        Rectangle tall = new Rectangle(0, 0, 5, 30);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(square, tall), new Point2D.Double(0, 0), 40, 0, false);

        assertEquals(0, layout.placements().get(tall).rotation(), 0.01);
        assertEquals(30, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldNotRotateEntitiesThatGainNothingFromIt() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(0, 0, 10, 10);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(first, second), new Point2D.Double(0, 0), 100, 2, true);

        assertEquals(0, layout.placements().get(first).rotation(), 0.01);
        assertEquals(0, layout.placements().get(second).rotation(), 0.01);
        assertEquals(22, layout.width(), 0.01);
        assertEquals(10, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldRotateInStepsOfFortyFiveDegreesFromTheAlignedOrientation() {
        Path diagonal = new Path();
        diagonal.moveTo(0, 0);
        diagonal.lineTo(20, 20);
        diagonal.lineTo(21, 20);
        diagonal.lineTo(1, 0);
        diagonal.close();

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(diagonal), new Point2D.Double(0, 0), 100, 0, true);

        double rotation = layout.placements().get(diagonal).rotation();
        assertEquals("A diagonal sliver is laid flat by a 45 degree turn", 0, rotation % 45, 0.01);
        assertTrue(layout.height() < 2);
    }

    @Test
    public void layout_shouldAlignEntitiesRotatedByAnyAngle() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 30);
        rectangle.rotate(74);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(rectangle), new Point2D.Double(0, 0), 100, 0, true);

        rectangle.rotate(rectangle.getCenter(), layout.placements().get(rectangle).rotation());
        Rectangle2D bounds = rectangle.getBounds();
        assertEquals("The rectangle is laid flat", 30, bounds.getWidth(), 0.01);
        assertEquals(10, bounds.getHeight(), 0.01);
        assertEquals(10, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldTuckEntitiesIntoNotchesUsingTheOutline() {
        Path lShape = new Path();
        lShape.moveTo(0, 0);
        lShape.lineTo(20, 0);
        lShape.lineTo(20, 10);
        lShape.lineTo(10, 10);
        lShape.lineTo(10, 20);
        lShape.lineTo(0, 20);
        lShape.close();
        Rectangle square = new Rectangle(50, 50, 8, 8);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(square, lShape), new Point2D.Double(0, 0), 20, 0, false);

        assertPosition(0, 0, layout.placements().get(lShape));
        assertPosition(10, 10, layout.placements().get(square));
        assertEquals(20, layout.width(), 0.01);
        assertEquals(20, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldKeepTheSpacingBetweenOutlines() {
        Path lShape = new Path();
        lShape.moveTo(0, 0);
        lShape.lineTo(20, 0);
        lShape.lineTo(20, 10);
        lShape.lineTo(10, 10);
        lShape.lineTo(10, 20);
        lShape.lineTo(0, 20);
        lShape.close();
        Rectangle square = new Rectangle(50, 50, 6, 6);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(square, lShape), new Point2D.Double(0, 0), 20, 2, false);

        assertPosition(12, 12, layout.placements().get(square));
        assertEquals(20, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldTurnEntityWiderThanTheAreaNinetyDegreesSoItFits() {
        Rectangle tall = new Rectangle(4, 14, 17, 24);
        Rectangle small = new Rectangle(16, 42, 9, 7);
        Rectangle smaller = new Rectangle(4, 42, 8, 9);
        Rectangle wide = new Rectangle(25, 14, 26, 19);
        Rectangle bar = new Rectangle(4, 55, 51, 6);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(tall, small, smaller, wide, bar), new Point2D.Double(4, 14), 50, 4, true);

        assertEquals(90, layout.placements().get(bar).rotation() % 180, 0.01);
        assertTrue("The bar on its side fits the width", layout.width() <= 50);
        assertEquals("The bar stands on the first row and the rest pack next to it", 51, layout.height(), 0.01);
    }

    @Test
    public void layout_shouldStraightenEntitiesThatWereRotatedBeforePackingThem() {
        Rectangle tall = rotated(new Rectangle(4, 14, 17, 24), 74);
        Rectangle small = rotated(new Rectangle(16, 42, 9, 7), -70);
        Rectangle smaller = rotated(new Rectangle(4, 42, 8, 9), 180);
        Rectangle wide = rotated(new Rectangle(25, 14, 26, 19), 15);
        Rectangle bar = rotated(new Rectangle(4, 55, 51, 6), -160);

        NestingUtils.NestingLayout layout = NestingUtils.layout(List.of(tall, small, smaller, wide, bar), new Point2D.Double(0, 0), 50, 4, true);

        assertTrue(layout.fitsWithin(50, 60));
        assertEquals(51, layout.height(), 0.01);
    }

    private static Rectangle rotated(Rectangle rectangle, double angle) {
        rectangle.rotate(angle);
        return rectangle;
    }

    private static void assertPosition(double x, double y, NestingUtils.Placement placement) {
        assertEquals(x, placement.position().getX(), 0.01);
        assertEquals(y, placement.position().getY(), 0.01);
    }
}
