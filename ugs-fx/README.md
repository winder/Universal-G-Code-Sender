# UGS FX

This is a variant of the application that is intended to supersede the classic edition
and with time, maybe also the platform edition.

The motivation for using JavaFX is that it is getting harder to create flexible and nice looking UI:s 
with Java Swing. There are also a simplistic 3D scene graph and rendering API which is 
needed to create a more interactive visualizer. 

The platform edition utilizes the Netbeans Platform which provides a ton of cool features, but is 
quite heavy to work with. We are also constrained to the NetBeans way of doing things. Therefore we
are cutting ties with the old platform code base and are rewriting it in JavaFX. 

## Java version

This module is compiled and run with **Java 22 or later**, while the rest of the project still
targets Java 17. The higher floor comes from the experimental Vulkan visualizer, which binds
Vulkan through the `java.lang.foreign` APIs that were only finalized in Java 22. Building the
project with an older JDK fails on this module.

Because Vulkan is reached through the FFM API, the application has to be started with
`--enable-native-access=ALL-UNNAMED`. The native build scripts already pass it.

## Vulkan visualizer

The visualizer is rendered with Vulkan; the earlier JavaFX 3D scene graph visualizer has been
removed. JavaFX still draws everything 2D: the controls, the toolbars and the overlays. The
design and the phases it was built in are in `docs/vulkan-visualizer-editor-plan.md`.

The code is split so that only one package knows about Vulkan:

* `component/visualizer/scene` is renderer agnostic. `Scene` holds `Renderable`s and draws
  them in `SceneLayer` order through the `RenderContext` interface; `Camera` owns the view and
  projection and does the picking math (`project`, `unproject`, `intersectWorkPlane`).
  Everything here is unit tested without a GPU.
* `component/visualizer/scene/renderables` holds what is drawn: grid, axes, ruler, tool
  marker, probe marker, the G-code toolpath, the orientation cube and the
  `SceneGraphRenderable` adapter for the machine models. Components outside the visualizer
  register their own through `VisualizerService.addRenderable`.
* The orientation cube is drawn into a sub viewport of the frame
  (`RenderContext.beginSubViewport`) with its own orthographic camera that copies the main
  camera's yaw and pitch. Its labels come from the overlay canvas and clicking a face animates
  the main camera through `CameraAnimator`.
* `component/visualizer/render` is the Vulkan implementation: device, pipelines, render target
  and `VulkanRenderContext`. `VulkanFrameRenderer` renders a `Scene` into an offscreen image.
* `component/visualizer/input` turns JavaFX mouse and key events into `PointerEvent`s that
  carry the ray and the work plane point under the cursor. `InputRouter` hands them to an
  ordered stack of `InputHandler`s; the first to consume a press owns the gesture until the
  release. `CameraNavigationHandler` sits last and reads the pan, rotate and zoom mapping from
  the visualizer settings, so anything an editing tool does not claim moves the view.
* `component/visualizer/overlay/OverlayPainter` draws on a JavaFX canvas over the frame for
  text and markers Vulkan has no font for, positioned through `Camera.project`.
* `component/visualizer/VisualizerPane` is the JavaFX pane that presents the frames and owns
  the scene, the camera, the input router, the overlay canvas and the toolbars.
* `component/designer/render` draws the design of a `.ugsd` workspace. `DesignTessellator`
  flattens the designer's `java.awt.Shape`s into line lists and triangulates fills with JTS;
  `DesignRenderable` keeps one set of meshes per entity in the entity's own coordinates and
  places them with the entity transform, so editing an entity's position never rebuilds them.
* `component/designer/editor` is the graphics editor. `DesignEditor` is an `InputHandler`
  ahead of the camera navigation that claims the primary button over the work plane and hands
  the gesture to the tool the designer `Controller` says is current: `SelectTool` (move, resize,
  rotate and marquee gestures), the creation tools and `VertexTool`. Tools change the model live
  and commit the designer's own undoable actions on release, so undo, the inspector and the
  object tree work unchanged. `HandlesRenderable` draws the frame, handles, rubber band and
  previews; the legacy `Control` classes are not used. The entities live in the designer's
  `DesignModel` (`Controller.getModel()`), which has no UI toolkit attached; the Swing
  `Drawing` is only created when something asks for it.

Anything that changes what should be seen calls `Scene.requestRender()`; frames are only
rendered on demand.

It draws the base scene (a grid on the work plane, the X and Y axes and a cone marking the tool
position), the G-code toolpath and the machine model. It does **not** yet draw the ruler or the
designer shapes, and it has none of the overlays the JavaFX visualizer has (the orientation cube,
the toolbars, entity picking).

### Scene graph walker

The machine model is not reimplemented for Vulkan. `SceneGraphWalker` walks the ordinary JavaFX
scene graph and turns it into draw calls, so every machine part class keeps working unchanged:
their nested groups, their `Rotate` and `Translate` transforms and their bindings to the machine
position all behave as they do under JavaFX, because JavaFX composes transforms and propagates
bindings on a tree that was never attached to a `Scene`. Only the drawing is taken over.

`MeshConverter` flattens the geometry, since JavaFX indexes points, normals and texture
coordinates separately per face corner. `Box` and `Cylinder` keep their meshes private to the
JavaFX pipeline, so those are rebuilt from their dimensions instead. Vertices are uploaded the
first time a shape is seen and then cached, so a frame only re-reads transforms and colours.

Walking reads the scene graph, so it has to happen on the JavaFX application thread, which is
where the render pulse already runs.

### G-code toolpath

`GcodeLines` builds two vertices per segment rather than a triangulated tube, and gives each
vertex its colour and the number of the command that produced it. Showing how far a program has
run is then a single number in a push constant that the shader compares against — no texture, no
change to the geometry, and nothing to re-upload while streaming.

For reference, a 100k segment program builds a 5.3 MB buffer and renders in about 0.6 ms per
frame on a discrete GPU, with the progress marker moving on every frame.

Lines wider than one pixel cannot use `lineWidth`, which needs the `wideLines` device feature
that MoltenVK does not have. `wideline.vert` instead expands each segment into a screen space
quad, reading the same vertex buffer one segment per instance, so the width can change without
a new upload. Widths at or below one pixel go through the plain line pipeline.

Vulkan cannot render into a JavaFX `SubScene`, so `VulkanVisualizer` renders to an offscreen
image, copies it back to host memory and presents it through a `PixelBuffer`. That keeps it an
ordinary `Pane`, so it can sit anywhere the JavaFX visualizer does.

Frames are rendered on demand rather than on every pulse: the camera controls, the animated
tool position, the background colour and a resize all call `requestRender()`. Redrawing a
static scene every pulse measured about 37% of a CPU core, against about 2.5% on demand and
1.9% for an empty JavaFX window, so anything new that changes the picture has to request a
frame or it will not appear until the next thing does.

The shaders live in `src/main/resources/shaders/vulkan`; `push.glsl` holds the push constant
block every pipeline shares and `VulkanRenderContext` mirrors its layout. The compiled SPIR-V is
committed alongside the GLSL sources; run `compile.sh` in that directory after editing a shader
(needs `glslang` from `glslang-tools`, the Vulkan SDK or a glslang GitHub release).

### macOS

macOS has no native Vulkan driver, so Vulkan is reached through
[MoltenVK](https://github.com/KhronosGroup/MoltenVK), which translates it to Metal. The native
build downloads the MoltenVK release and bundles `libMoltenVK.dylib` into the application
directory, pointing at it with `-Dugs.vulkan.library=$APPDIR/libMoltenVK.dylib`. The released
dylib is a universal binary, so the same file serves both the x64 and the aarch64 build.

Note that MoltenVK is a portability driver, which is left out of `vkEnumeratePhysicalDevices`
unless the instance enables `VK_KHR_portability_enumeration`, and requires
`VK_KHR_portability_subset` to be enabled on the device. `VulkanDevice` opts into both whenever
they are advertised, so a Mac would otherwise report no Vulkan capable device at all.

When running from Maven rather than from a native build, install MoltenVK (the Vulkan SDK or
`brew install molten-vk`) and it will be found in the usual locations. `-Dugs.vulkan.library`
overrides the search with an explicit path on any platform.

## Icons

Whenever a new icon is needed, look at the filled Phosphor icons:
https://phosphoricons.com/

Simply add them as SVG:s and use the SvgLoader to take care of the loading. It will also
take care of the color tinting and sizing.

## Settings

The settings are stored using Javas preference system and are located here on each platform:
* **Linux**: `~/.java/.userPrefs/com/willwinder/universalgcodesender`
* **Windows**: `HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\willwinder\universalgcodesender`
* **MacOSX**: `~/Library/Preferences/com.willwinder.universalgcodesender`