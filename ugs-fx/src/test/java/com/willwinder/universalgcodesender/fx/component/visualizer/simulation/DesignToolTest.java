package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DesignToolTest {

    @Test
    public void shouldUseTheDiameterAndShapeFromTheSettings() {
        Settings settings = new Settings();
        settings.setToolDiameter(6);
        settings.setToolShape(EndmillShape.UPCUT);
        ToolProfile flat = DesignTool.fromSettings(settings);
        assertThat(flat.kind()).isEqualTo(ToolProfile.Kind.FLAT);
        assertThat(flat.diameter()).isCloseTo(6, within(1e-9));

        settings.setToolShape(EndmillShape.V_BIT);
        settings.setVBitAngle(60);
        ToolProfile vBit = DesignTool.fromSettings(settings);
        assertThat(vBit.kind()).isEqualTo(ToolProfile.Kind.V_BIT);
        assertThat(vBit.heightAt(1)).isCloseTo(Math.sqrt(3), within(1e-9));

        settings.setToolShape(EndmillShape.BALL);
        assertThat(DesignTool.fromSettings(settings).kind()).isEqualTo(ToolProfile.Kind.BALL);
    }

    @Test
    public void settingsShouldWinOverAStaleLibrarySnapshot() {
        // The tool paths are generated from the settings, so a snapshot that was edited away from
        // must not decide what the simulation cuts with
        Settings settings = new Settings();
        settings.setToolDiameter(6);
        settings.setToolShape(EndmillShape.UPCUT);
        ToolDefinition snapshot = new ToolDefinition();
        snapshot.setShape(EndmillShape.BALL);
        snapshot.setDiameter(0.25);
        snapshot.setDiameterUnit(UnitUtils.Units.INCH);
        settings.setCurrentToolSnapshot(snapshot);

        ToolProfile profile = DesignTool.fromSettings(settings);
        assertThat(profile.kind()).isEqualTo(ToolProfile.Kind.FLAT);
        assertThat(profile.diameter()).isCloseTo(6, within(1e-9));
    }

    @Test
    public void snapshotShouldBeTheFallbackWithoutADiameter() {
        Settings settings = new Settings();
        settings.setToolDiameter(0);
        ToolDefinition snapshot = new ToolDefinition();
        snapshot.setShape(EndmillShape.BALL);
        snapshot.setDiameter(0.25);
        snapshot.setDiameterUnit(UnitUtils.Units.INCH);
        settings.setCurrentToolSnapshot(snapshot);

        ToolProfile profile = DesignTool.fromSettings(settings);
        assertThat(profile.kind()).isEqualTo(ToolProfile.Kind.BALL);
        assertThat(profile.diameter()).isCloseTo(6.35, within(1e-9));
    }

    @Test
    public void shouldDescribeTheDesignTool() {
        Settings settings = new Settings();
        settings.setToolDiameter(6);
        settings.setToolShape(EndmillShape.V_BIT);
        settings.setVBitAngle(60);
        assertThat(DesignTool.describe(settings)).isEqualTo("6mm V-bit 60°");

        settings.setToolShape(EndmillShape.BALL);
        settings.setToolDiameter(3.175);
        assertThat(DesignTool.describe(settings)).isEqualTo("3.175mm Ball");

        ToolDefinition snapshot = new ToolDefinition();
        snapshot.setName("Roughing bit");
        snapshot.setToolNumber(3);
        snapshot.setDiameter(6);
        settings.setCurrentToolSnapshot(snapshot);
        assertThat(DesignTool.describe(settings)).isEqualTo("T3 · Roughing bit");
        assertThat(DesignTool.resolver(settings).describe(new CutSegment(0, 0, 0, 1, 0, 0, 0, 1)).source())
                .isEqualTo(ResolvedTool.Source.DESIGN);
    }

    @Test
    public void resolverShouldUseTheSameToolForEverySegment() {
        Settings settings = new Settings();
        settings.setToolDiameter(4);
        ToolResolver resolver = DesignTool.resolver(settings);
        assertThat(resolver.resolve(new CutSegment(0, 0, 0, 1, 0, 0, 0, 1)).diameter()).isCloseTo(4, within(1e-9));
        assertThat(resolver.resolve(new CutSegment(0, 0, 0, 1, 0, 0, 7, 9)).diameter()).isCloseTo(4, within(1e-9));
    }
}
