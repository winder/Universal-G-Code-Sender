package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class EntityTreeTextTest {

    @Test
    public void detail_shouldShowDepthsFeedRateAndSpindleSpeed() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setCutType(CutType.POCKET);
        rectangle.setStartDepth(1.5);
        rectangle.setTargetDepth(5);
        rectangle.setFeedRate(800);
        rectangle.setSpindleSpeed(75);

        String text = EntityTreeText.detail(rectangle, UnitUtils.Units.MM);

        Assertions.assertThat(text).isEqualTo("1.5 - 5 mm, 800 mm/min, 75%");
    }

    @Test
    public void detail_shouldConvertDepthsToPreferredUnits() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setCutType(CutType.POCKET);
        rectangle.setStartDepth(0);
        rectangle.setTargetDepth(25.4);
        rectangle.setFeedRate(800);
        rectangle.setSpindleSpeed(75);

        String text = EntityTreeText.detail(rectangle, UnitUtils.Units.INCH);

        Assertions.assertThat(text).isEqualTo("0 - 1 \", 800 mm/min, 75%");
    }

    @Test
    public void detail_shouldNameTheCutTypeWhenThereIsNone() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.setCutType(CutType.NONE);

        String text = EntityTreeText.detail(rectangle, UnitUtils.Units.MM);

        Assertions.assertThat(text).isEqualTo("None");
    }

    @Test
    public void detail_shouldCountTheObjectsOfAGroup() {
        EntityGroup group = new EntityGroup();
        group.addChild(new Rectangle(0, 0, 10, 10));
        group.addChild(new Rectangle(20, 0, 10, 10));

        String text = EntityTreeText.detail(group, UnitUtils.Units.MM);

        Assertions.assertThat(text).isEqualTo("2 objects");
    }

    @Test
    public void detail_shouldUseSingularForAGroupWithOneObject() {
        EntityGroup group = new EntityGroup();
        group.addChild(new Rectangle(0, 0, 10, 10));

        String text = EntityTreeText.detail(group, UnitUtils.Units.MM);

        Assertions.assertThat(text).isEqualTo("1 object");
    }

    @Test
    public void isOpenPath_shouldBeTrueForAPathThatIsNotClosed() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);

        boolean openPath = EntityTreeText.isOpenPath(path);

        Assertions.assertThat(openPath).isTrue();
    }

    @Test
    public void isOpenPath_shouldBeFalseForAPathEndingAtItsStart() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 0);

        boolean openPath = EntityTreeText.isOpenPath(path);

        Assertions.assertThat(openPath).isFalse();
    }

    @Test
    public void isOpenPath_shouldBeFalseForAShape() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);

        boolean openPath = EntityTreeText.isOpenPath(rectangle);

        Assertions.assertThat(openPath).isFalse();
    }
}
