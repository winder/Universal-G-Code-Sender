import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { useAppSelector } from "../hooks/useAppSelector";
import { getToolpath, ToolpathSegment } from "../services/visualizer";
import "./Visualizer3D.scss";

const RAPID_COLOR = new THREE.Color("#6b7280");
const CUT_COLOR = new THREE.Color("#4ade80");
const ARC_COLOR = new THREE.Color("#7bdcff");

const buildToolpathGeometry = (segments: ToolpathSegment[]) => {
  const positions = new Float32Array(segments.length * 6);
  const colors = new Float32Array(segments.length * 6);

  segments.forEach((segment, i) => {
    const offset = i * 6;
    positions[offset] = segment.start.x;
    positions[offset + 1] = segment.start.y;
    positions[offset + 2] = segment.start.z;
    positions[offset + 3] = segment.end.x;
    positions[offset + 4] = segment.end.y;
    positions[offset + 5] = segment.end.z;

    const color = segment.rapid ? RAPID_COLOR : segment.arc ? ARC_COLOR : CUT_COLOR;
    colors[offset] = color.r;
    colors[offset + 1] = color.g;
    colors[offset + 2] = color.b;
    colors[offset + 3] = color.r;
    colors[offset + 4] = color.g;
    colors[offset + 5] = color.b;
  });

  const geometry = new THREE.BufferGeometry();
  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));
  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));
  return geometry;
};

const Visualizer3D = () => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const toolMarkerRef = useRef<THREE.Mesh | null>(null);
  const [isEmpty, setIsEmpty] = useState(false);
  const workCoord = useAppSelector((state) => state.status.workCoord);

  useEffect(() => {
    if (toolMarkerRef.current) {
      toolMarkerRef.current.position.set(workCoord.x, workCoord.y, workCoord.z);
    }
  }, [workCoord]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const scene = new THREE.Scene();
    scene.background = new THREE.Color("#111213");

    const camera = new THREE.PerspectiveCamera(50, 1, 0.1, 10000);
    camera.up.set(0, 0, 1);

    const renderer = new THREE.WebGLRenderer({ antialias: true });
    container.appendChild(renderer.domElement);

    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;

    const grid = new THREE.GridHelper(200, 20, 0x2f3132, 0x2f3132);
    grid.rotation.x = Math.PI / 2;
    scene.add(grid);

    const toolMarker = new THREE.Mesh(
      new THREE.SphereGeometry(1.5, 16, 16),
      new THREE.MeshBasicMaterial({ color: "#ff6b6b" })
    );
    scene.add(toolMarker);
    toolMarkerRef.current = toolMarker;

    let toolpathLines: THREE.LineSegments | null = null;

    getToolpath().then((segments) => {
      if (segments.length === 0) {
        setIsEmpty(true);
        return;
      }

      const geometry = buildToolpathGeometry(segments);
      const material = new THREE.LineBasicMaterial({ vertexColors: true });
      toolpathLines = new THREE.LineSegments(geometry, material);
      scene.add(toolpathLines);

      geometry.computeBoundingSphere();
      const sphere = geometry.boundingSphere;
      if (sphere) {
        const distance = sphere.radius * 2.2 || 100;
        camera.position.set(sphere.center.x + distance, sphere.center.y - distance, sphere.center.z + distance);
        controls.target.copy(sphere.center);
        controls.update();
      }
    });

    const resize = () => {
      const { clientWidth, clientHeight } = container;
      if (clientWidth === 0 || clientHeight === 0) return;
      camera.aspect = clientWidth / clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(clientWidth, clientHeight);
    };
    resize();
    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(container);

    let animationFrame: number;
    const animate = () => {
      controls.update();
      renderer.render(scene, camera);
      animationFrame = requestAnimationFrame(animate);
    };
    animate();

    return () => {
      cancelAnimationFrame(animationFrame);
      resizeObserver.disconnect();
      controls.dispose();
      renderer.dispose();
      toolpathLines?.geometry.dispose();
      container.removeChild(renderer.domElement);
    };
  }, []);

  return (
    <div className="visualizer3D">
      {isEmpty && <div className="visualizer3DEmpty">No file loaded to visualize.</div>}
      <div className="visualizer3DCanvas" ref={containerRef} />
    </div>
  );
};

export default Visualizer3D;
