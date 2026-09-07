package com.willwinder.universalgcodesender.fx.component.clipart;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.junit.Test;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ClipartPreviewFactoryTest {

    @Test
    public void createPreview_shouldScaleAndCenterShapeInsideBox() {
        Rectangle2D shape = new Rectangle2D.Double(100, 200, 10, 20);

        Path path = ClipartPreviewFactory.createPreview(shape, 100);

        List<Point2D> points = getPoints(path);
        assertThat(points).extracting(Point2D::getX).allSatisfy(x -> assertThat(x).isBetween(25 - 1e-6, 75 + 1e-6));
        assertThat(points).extracting(Point2D::getY).allSatisfy(y -> assertThat(y).isBetween(-1e-6, 100 + 1e-6));
        assertThat(points).extracting(Point2D::getX).contains(25.0, 75.0);
        assertThat(points).extracting(Point2D::getY).contains(0.0, 100.0);
    }

    @Test
    public void createPreview_shouldFlipYAxisSoTopOfShapeIsAtTopOfBox() {
        Path2D.Double triangle = new Path2D.Double();
        triangle.moveTo(0, 0);
        triangle.lineTo(10, 0);
        triangle.lineTo(0, 20);
        triangle.closePath();

        Path path = ClipartPreviewFactory.createPreview(triangle, 100);

        MoveTo start = (MoveTo) path.getElements().get(0);
        LineTo apex = (LineTo) path.getElements().get(2);
        assertThat(start.getY()).isCloseTo(100, within(1e-6));
        assertThat(apex.getY()).isCloseTo(0, within(1e-6));
    }

    @Test
    public void createPreview_shouldKeepEvenOddFillRule() {
        Path2D.Double ring = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        ring.append(new Rectangle2D.Double(0, 0, 10, 10), false);
        ring.append(new Rectangle2D.Double(2, 2, 4, 4), false);

        Path path = ClipartPreviewFactory.createPreview(ring, 100);

        assertThat(path.getFillRule()).isEqualTo(FillRule.EVEN_ODD);
    }

    @Test
    public void createPreview_shouldNotFailOnDegenerateShape() {
        Path path = ClipartPreviewFactory.createPreview(new Rectangle2D.Double(5, 5, 0, 0), 100);

        assertThat(path.getElements()).isNotEmpty();
    }

    private static List<Point2D> getPoints(Path path) {
        return path.getElements().stream()
                .map(ClipartPreviewFactoryTest::toPoint)
                .filter(Objects::nonNull)
                .toList();
    }

    private static Point2D toPoint(PathElement element) {
        if (element instanceof MoveTo moveTo) {
            return new Point2D.Double(moveTo.getX(), moveTo.getY());
        } else if (element instanceof LineTo lineTo) {
            return new Point2D.Double(lineTo.getX(), lineTo.getY());
        }
        return null;
    }
}
