package com.willwinder.ugs.nbp.visualizer3;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbp.visualizer3.renderable.OrientationCube;
import com.willwinder.ugs.nbp.visualizer3.renderable.GcodeVisualizer;
import com.willwinder.ugs.nbp.visualizer3.renderable.IPreferencesListener;
import com.willwinder.ugs.nbp.visualizer3.renderable.OriginAxis;
import com.willwinder.ugs.nbp.visualizer3.renderable.Tool;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Position;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_BG;

import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class GcodeRenderer extends SimpleApplication implements PreferenceChangeListener {

    private static final Logger logger = Logger.getLogger(GcodeRenderer.class.getSimpleName());

    private OrbitCamera orbitCam;

    private GcodeVisualizer gcode;

    private Tool tool;

    private Node orientationCubeNode;

    public GcodeRenderer() {
        stateManager.attach(new MouseCursorState());

        // Adjust the graphics settings for low power machines or to optimize battery
        // life.
        boolean lowPowerGraphics = false;

        setShowSettings(false);
        AppSettings settings = new AppSettings(true);
        settings.setAudioRenderer(null);
        if (lowPowerGraphics) {
            // Limit the framerate to lower CPU/GPU work.
            // TODO if we can manually run the JME3 update loop, we can drive frames from
            // user input/UGS events instead of constantly rendering. It looks like this
            // might require writing our own JmeCanvasContext (set with
            // settings.setCustomRenderer) since all the existing ones run update in an
            // infinite loop.
            settings.setFrameRate(20);
        } else {
            // These settings use more GPU memory.
            settings.setSamples(8);
            settings.setUseRetinaFrameBuffer(true);
        }
        setSettings(settings);

        setPauseOnLostFocus(false);
        createCanvas();
        startCanvas(true);

        setDisplayStatView(false);
        setDisplayFps(false);
    }

    public java.awt.Canvas getCanvas() {
        return ((JmeCanvasContext) getContext()).getCanvas();
    }

    @Override
    public void simpleInitApp() {
        // https://wiki.jmonkeyengine.org/docs/3.4/core/input/input_handling.html#2-remove-default-trigger-mappings
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_CAMERA_POS);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_MEMORY);
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);

        flyCam.setEnabled(false);
        orbitCam = new OrbitCamera(assetManager, cam, getInputManager());
        // new CameraControl(cam, ControlDirection.CameraToSpatial);

        gcode = new GcodeVisualizer(assetManager);
        rootNode.attachChild(gcode);

        OriginAxis axis = new OriginAxis(assetManager, guiFont);
        axis.addControl(new ConstantScreenSizeControl(40.0f));
        rootNode.attachChild(axis);

        tool = new Tool(assetManager);
        // Initially hide the tool since we don't know where it is.
        tool.setCullHint(CullHint.Always);
        rootNode.attachChild(tool);

        rootNode.setCullHint(CullHint.Never);

        {
            Camera orientationCubeCam = cam.clone();

            orientationCubeNode = new Node("OrientationCubeRoot");
            OrientationCube child = new OrientationCube(assetManager, guiFont, getInputManager(), orientationCubeCam);
            orientationCubeNode.attachChild(child);

            ViewPort viewPort2 = renderManager.createMainView("PiP", orientationCubeCam);
            viewPort2.setClearFlags(false, true, true);
            viewPort2.attachScene(orientationCubeNode);
            viewPort2.addProcessor(
                    new OrientationCubeCameraProcessor(cam, orientationCubeCam, orientationCubeNode));
        }

        // TODO hook up enable/disable settings
        // TODO size display
        // TODO grid (at machine lowest extent?)
        // TODO zoom to rectengular selection
        // TODO machine bounds
        // TODO job bounds?
        // TODO handle units?

        reloadPreferences();
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    public void setGcode(GcodeModel model) {
        gcode.setModel(model);

        Vector3f min = Convert.ToVector3f(model.getMin());
        Vector3f max = Convert.ToVector3f(model.getMax());
        Vector3f focalPoint = min.add(max.subtract(min).divide(2.0f));

        orbitCam.setFocalPoint(focalPoint);
    }

    public void setCurrentCommandNumber(long currentCommandNumber) {
        gcode.setCurrentCommandNumber(currentCommandNumber);
    }

    public void setControllerState(ControllerState state) {
        if (state == ControllerState.DISCONNECTED || state == ControllerState.UNKNOWN) {
            tool.setCullHint(CullHint.Always);
        }
    }

    public void setMachineCoordinate(Position position) {
    }

    public void setWorkCoordinate(Position position) {
        // Display the tool only when we know where it is.
        tool.setCullHint(CullHint.Never);
        tool.setLocalTranslation(Convert.ToVector3f(position));
    }

    private void reloadPreferences() {
        VisualizerOptions vo = new VisualizerOptions();

        viewPort.setBackgroundColor(Convert.ToColorRGBA(vo.getOptionForKey(VISUALIZER_OPTION_BG).value));

        rootNode.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                if (spatial instanceof IPreferencesListener) {
                    ((IPreferencesListener) spatial).reloadPreferences(vo);
                }
            }
        });
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        reloadPreferences();
    }
}
