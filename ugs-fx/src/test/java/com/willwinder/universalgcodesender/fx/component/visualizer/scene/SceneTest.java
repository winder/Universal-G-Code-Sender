package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class SceneTest {
    private RecordingRenderContext context;
    private Scene scene;

    @Before
    public void setUp() {
        context = new RecordingRenderContext();
        scene = new Scene(context);
    }

    @Test
    public void render_shouldDrawLayersInOrderAndInsertionOrderWithinLayer() {
        scene.add(new StubRenderable("machine", SceneLayer.MACHINE));
        scene.add(new StubRenderable("grid1", SceneLayer.GRID));
        scene.add(new StubRenderable("handles", SceneLayer.HANDLES));
        scene.add(new StubRenderable("grid2", SceneLayer.GRID));

        scene.render();

        assertThat(context.draws).containsExactly("grid1", "grid2", "machine", "handles");
    }

    @Test
    public void render_shouldDrawTheToolpathBeforeTheDesignSoTranslucentFillsBlendOverIt() {
        scene.add(new StubRenderable("fill", SceneLayer.DESIGN_FILL));
        scene.add(new StubRenderable("outline", SceneLayer.DESIGN_OUTLINE));
        scene.add(new StubRenderable("toolpath", SceneLayer.GCODE));

        scene.render();

        assertThat(context.draws).containsExactly("toolpath", "fill", "outline");
    }

    @Test
    public void render_shouldSkipInvisibleRenderables() {
        StubRenderable hidden = new StubRenderable("hidden", SceneLayer.GCODE);
        hidden.visible = false;
        scene.add(hidden);
        scene.add(new StubRenderable("shown", SceneLayer.GCODE));

        scene.render();

        assertThat(context.draws).containsExactly("shown");
    }

    @Test
    public void render_shouldDisableDepthTestForOverlayLayers() {
        scene.add(new StubRenderable("gcode", SceneLayer.GCODE));
        scene.add(new StubRenderable("handles", SceneLayer.HANDLES));

        scene.render();

        assertThat(context.depthTests).containsExactly(true, false);
    }

    @Test
    public void add_shouldAttachOnceAndRequestRender() {
        AtomicInteger renders = new AtomicInteger();
        scene.addRenderListener(renders::incrementAndGet);
        StubRenderable renderable = new StubRenderable("a", SceneLayer.GRID);

        scene.add(renderable);
        scene.add(renderable);

        assertThat(renderable.attached).isEqualTo(1);
        assertThat(renders.get()).isEqualTo(1);
        assertThat(scene.renderables()).containsExactly(renderable);
    }

    @Test
    public void clear_shouldDetachEverything() {
        StubRenderable a = new StubRenderable("a", SceneLayer.GRID);
        StubRenderable b = new StubRenderable("b", SceneLayer.GCODE);
        scene.add(a);
        scene.add(b);

        scene.clear();

        assertThat(a.detached).isEqualTo(1);
        assertThat(b.detached).isEqualTo(1);
        assertThat(scene.renderables()).isEmpty();
    }

    @Test
    public void bounds_shouldUnionVisibleRenderablesOnly() {
        StubRenderable a = new StubRenderable("a", SceneLayer.GCODE);
        a.bounds = new Bounds3(0, 0, 0, 10, 10, 10);
        StubRenderable b = new StubRenderable("b", SceneLayer.GCODE);
        b.bounds = new Bounds3(-5, 20, 0, 0, 30, 1);
        StubRenderable hidden = new StubRenderable("hidden", SceneLayer.GCODE);
        hidden.bounds = new Bounds3(-100, -100, -100, 100, 100, 100);
        hidden.visible = false;
        scene.add(a);
        scene.add(b);
        scene.add(hidden);

        Optional<Bounds3> bounds = scene.bounds();

        assertThat(bounds).contains(new Bounds3(-5, 0, 0, 10, 30, 10));
    }

    private static final class StubRenderable implements Renderable {
        private final String name;
        private final SceneLayer layer;
        private boolean visible = true;
        private Bounds3 bounds;
        private int attached;
        private int detached;

        private StubRenderable(String name, SceneLayer layer) {
            this.name = name;
            this.layer = layer;
        }

        @Override
        public SceneLayer layer() {
            return layer;
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public Optional<Bounds3> bounds() {
            return Optional.ofNullable(bounds);
        }

        @Override
        public void onAttached(Scene scene) {
            attached++;
        }

        @Override
        public void onDetached(Scene scene) {
            detached++;
        }

        @Override
        public void render(RenderContext context) {
            ((RecordingRenderContext) context).draws.add(name);
        }
    }

    /**
     * Records what the scene asks for instead of drawing, so layer order and depth state can be
     * asserted without a GPU.
     */
    private static final class RecordingRenderContext implements RenderContext {
        private final List<String> draws = new ArrayList<>();
        private final List<Boolean> depthTests = new ArrayList<>();
        private final Camera camera = new Camera();

        @Override
        public Camera camera() {
            return camera;
        }

        @Override
        public Viewport viewport() {
            return Viewport.EMPTY;
        }

        @Override
        public MeshHandle upload(float[] vertices, VertexLayout layout) {
            return new MeshHandle() {
                @Override
                public VertexLayout layout() {
                    return layout;
                }

                @Override
                public int vertexCount() {
                    return vertices.length / layout.floatsPerVertex();
                }
            };
        }

        @Override
        public void release(MeshHandle mesh) {
        }

        @Override
        public void drawLines(MeshHandle mesh, float[] model, float[] rgba, float widthPx) {
        }

        @Override
        public void drawColoredLines(MeshHandle mesh, float[] model, float widthPx) {
        }

        @Override
        public void drawToolpath(MeshHandle mesh, float[] model, float widthPx, int completedCommand, float[] completedRgba) {
        }

        @Override
        public void drawTriangles(MeshHandle mesh, float[] model, float[] rgba, boolean lit) {
        }

        @Override
        public TextureHandle uploadTexture(int width, int height, int[] argb) {
            return new TextureHandle() {
                @Override
                public int width() {
                    return width;
                }

                @Override
                public int height() {
                    return height;
                }
            };
        }

        @Override
        public void release(TextureHandle texture) {
        }

        @Override
        public void drawTextured(MeshHandle mesh, float[] model, TextureHandle texture, float opacity) {
        }

        @Override
        public void setDepthTest(boolean enabled) {
            depthTests.add(enabled);
        }

        @Override
        public void beginSubViewport(int x, int y, int width, int height, float[] viewProjection) {
        }

        @Override
        public void endSubViewport() {
        }
    }
}
