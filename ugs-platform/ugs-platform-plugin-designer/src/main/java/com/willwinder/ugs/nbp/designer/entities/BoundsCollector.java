package com.willwinder.ugs.nbp.designer.entities;

import com.google.common.collect.Sets;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A collector for merging the bounds of several entities to one big shape.
 *
 * @author Joacim Breiler
 */
public class BoundsCollector implements Collector<Rectangle2D, Rectangle2D, Rectangle2D> {

    public static final HashSet<Characteristics> CHARACTERISTICS = Sets.newHashSet(Characteristics.UNORDERED);

    public static BoundsCollector toBounds() {
        return new BoundsCollector();
    }

    private static boolean isIncomplete(Rectangle2D target) {
        return Double.isNaN(target.getX()) || Double.isNaN(target.getY()) || Double.isNaN(target.getWidth()) || Double.isNaN(target.getHeight());
    }

    @Override
    public Supplier<Rectangle2D> supplier() {
        return () -> new Rectangle2D.Double(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    @Override
    public BiConsumer<Rectangle2D, Rectangle2D> accumulator() {
        return (target, source) -> combiner().apply(target, source);
    }

    @Override
    public BinaryOperator<Rectangle2D> combiner() {
        return (target, source) -> {
            if (isIncomplete(target)) {
                target.setRect(source.getX(), source.getY(), source.getWidth(), source.getHeight());
            } else {
                target.add(source);
            }

            return target;
        };
    }

    @Override
    public Function<Rectangle2D, Rectangle2D> finisher() {
        return (target) -> {
            if (isIncomplete(target)) {
                return new Rectangle2D.Double(0, 0, 0, 0);
            } else {
                return target;
            }
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }
}
