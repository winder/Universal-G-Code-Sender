package com.willwinder.ugs.nbp.visualizer3.renderable;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

public class OrientationCube extends Node implements ActionListener, AnalogListener {
    public final static String INPUT_LEFT_MOUSE_CLICK = "OrientationCubePickTarget";
    public final static String INPUT_MOUSE_MOVE = "OrientationCubeHover";

    private final static String CUBE_NAME = "cube";

    private final static ColorRGBA BORDER_COLOR = new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f);
    private final static ColorRGBA FACE_COLOR = new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f);

    private final String FACE_NODE_NAME_BACK = "FaceNodeNameBack";
    private final String FACE_NODE_NAME_TOP = "FaceNodeNameTop";
    private final String FACE_NODE_NAME_RIGHT = "FaceNodeNameRight";
    private final String FACE_NODE_NAME_BOTTOM = "FaceNodeNameBottom";
    private final String FACE_NODE_NAME_LEFT = "FaceNodeNameLeft";
    private final String FACE_NODE_NAME_FRONT = "FaceNodeNameFront";

    private InputManager inputManager;

    private Camera camForMousePick;

    public OrientationCube(AssetManager assetManager, BitmapFont bitmapFont, InputManager inputManager,
            Camera camForMousePick) {
        super("OrientationCube");

        this.inputManager = inputManager;
        this.camForMousePick = camForMousePick;

        inputManager.addMapping(INPUT_LEFT_MOUSE_CLICK, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(INPUT_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(INPUT_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(INPUT_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(INPUT_MOUSE_MOVE, new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addListener(this, INPUT_LEFT_MOUSE_CLICK, INPUT_MOUSE_MOVE);

        // OriginAxis axis = new OriginAxis(assetManager, bitmapFont);
        // axis.setLocalTranslation(-0.5f, -0.5f, -0.5f);
        // attachChild(axis);

        Box box = new Box(0.5f, 0.5f, 0.5f);
        Geometry child = new Geometry(CUBE_NAME, box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/fakeLighting.j3md");
        mat.setColor("Color", BORDER_COLOR);
        child.setMaterial(mat);
        // Make the labels, etc act as a shell around the box so there's no Z-fighting.
        // This could probably be avoided by setting a different depth test, but I'm not
        // sure how to modify the materials of BitmapTexts.
        child.scale(0.9999f);
        attachChild(child);

        class Face {
            public String faceNodeName;
            public String label;
            public Vector3f normal;

            public Face(String faceNodeName, String label, Vector3f normal) {
                this.faceNodeName = faceNodeName;
                this.label = label;
                this.normal = normal;
            }
        }

        Face[] faces = {
                new Face(FACE_NODE_NAME_BACK, "Y+", Vector3f.UNIT_Y),
                new Face(FACE_NODE_NAME_TOP, "Z+", Vector3f.UNIT_Z),
                new Face(FACE_NODE_NAME_RIGHT, "X+", Vector3f.UNIT_X),
                new Face(FACE_NODE_NAME_BOTTOM, "Z-", Vector3f.UNIT_Z.negate()),
                new Face(FACE_NODE_NAME_LEFT, "X-", Vector3f.UNIT_X.negate()),
                new Face(FACE_NODE_NAME_FRONT, "Y-", Vector3f.UNIT_Y.negate()),
        };

        for (var face : faces) {
            // TODO the bitmap font is relatively low res; we could creat a higher res
            // version of just the glyphs needed for this cube
            var label = new BitmapText(bitmapFont);
            label.setColor(ColorRGBA.Black);
            label.setSize(0.5f);
            label.setText(face.label);
            label.move(new Vector3f(-label.getLineWidth() / 2, label.getLineHeight() / 2, 0.0001f));

            // var scaleLabelDown = new Node("scale");
            // scaleLabelDown.scale(1.0f / label.getSize());
            // scaleLabelDown.attachChild(label);

            var faceQuad = new Geometry(face.faceNodeName, new Quad(1.0f, 1.0f));
            Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/fakeLighting.j3md");
            mat2.setColor("Color", FACE_COLOR);
            faceQuad.setMaterial(mat2);
            faceQuad.setLocalTranslation(-0.5f, -0.5f, 0.0f);

            // Scale down about origin
            Node faceQuadScaled = new Node();
            faceQuadScaled.attachChild(faceQuad);
            faceQuadScaled.setLocalScale(0.8f);

            var faceNode = new Node(name + "_Face_" + face.label);
            faceNode.lookAt(face.normal,
                    face.normal.getZ() == 0 ? Vector3f.UNIT_Z : new Vector3f(0.0f, face.normal.getZ(), 0.0f));
            faceNode.move(face.normal.mult(0.5f));
            faceNode.attachChild(faceQuadScaled);
            faceNode.attachChild(label);
            // faceNode.attachChild(scaleLabelDown);

            attachChild(faceNode);
        }

        // TODO home button icon? to draw this icon, rotate the cube itself rather than
        // moving the camera around it? or add a guiNode to the orientation cube's
        // viewport
    }

    // @Override
    // public void updateLogicalState(float tpf) {
    // super.updateLogicalState(tpf);
    // }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        // TODO light up faces to pick on hover?
        // TODO allow rotate cube on mouse held
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(INPUT_LEFT_MOUSE_CLICK)) {
            CollisionResults results = new CollisionResults();
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = camForMousePick.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = camForMousePick.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f)
                    .subtractLocal(click3d)
                    .normalizeLocal();
            Ray ray = new Ray(click3d, dir);
            this.collideWith(ray, results);

            for (int i = 0; i < results.size(); i++) {
                Geometry target = results.getCollision(i).getGeometry();
                if (target.getName() == "BitmapText") {
                    // Ignore the label text nodes
                    continue;
                }
                if (target.getName() == CUBE_NAME) {
                    // Stop processing when we hit the orientation cube itself to avoid processing
                    // the back faces.

                    // TODO prevent mouse interactions on orbit camera until release

                    break;
                }

                if (!isPressed) {
                    switch (target.getName()) {
                        case FACE_NODE_NAME_BACK:
                            System.out.println("TODO move camera to BACK preset");
                            break;

                        case FACE_NODE_NAME_TOP:
                            System.out.println("TODO move camera to TOP preset");
                            break;

                        case FACE_NODE_NAME_RIGHT:
                            System.out.println("TODO move camera to RIGHT preset");
                            break;

                        case FACE_NODE_NAME_BOTTOM:
                            System.out.println("TODO move camera to BOTTOM preset");
                            break;

                        case FACE_NODE_NAME_LEFT:
                            System.out.println("TODO move camera to LEFT preset");
                            break;

                        case FACE_NODE_NAME_FRONT:
                            System.out.println("TODO move camera to FRONT preset");
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
}
