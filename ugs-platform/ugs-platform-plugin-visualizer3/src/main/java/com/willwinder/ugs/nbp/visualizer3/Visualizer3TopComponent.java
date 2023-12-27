package com.willwinder.ugs.nbp.visualizer3;

import com.jogamp.opengl.GLException;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptionsPanel;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbp.core.actions.OpenLogDirectoryAction;
import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

@TopComponent.Description(preferredID = "Visualizer3TopComponent")
@TopComponent.Registration(mode = Mode.LEFT_BOTTOM, openAtStartup = false)
@ActionID(category = LocalizingService.CATEGORY_WINDOW, id = Visualizer3TopComponent.ACTION_ID)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(displayName = "Visualizer3", preferredID = "Visualizer3TopComponent")
public final class Visualizer3TopComponent
        extends TopComponent implements UGSEventListener {

    public static final String ACTION_ID = "com.willwinder.ugs.nbp.visualizer3.Visualizer3TopComponent";
    private final transient BackendAPI backend;

    GcodeRenderer gcodeRenderer;
    GcodeModel gcodeModel;

    public Visualizer3TopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        initComponents();

        setName(Localization.getString("platform.plugin.visualizer3.name"));
        setToolTipText(
                Localization.getString("platform.plugin.visualizer3.tooltip"));
        backend.addUGSEventListener(this);
    }

    private void initComponents() {
        removeAll();
        setLayout(new BorderLayout());

        // add(new VisualizerToolBar(), BorderLayout.NORTH);
        JPanel borderedPanel = new JPanel();
        borderedPanel.setLayout(new BorderLayout());
        borderedPanel.setBorder(
                BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 1));
        borderedPanel.add(initializeVisualizationPanel(), BorderLayout.CENTER);
        add(borderedPanel, BorderLayout.CENTER);
    }

    private Component initializeVisualizationPanel() {
        try {
            gcodeModel = new GcodeModel("gcode model");
            gcodeRenderer = new GcodeRenderer();

            Preferences pref = NbPreferences.forModule(VisualizerOptionsPanel.class);
            pref.addPreferenceChangeListener(this.gcodeRenderer);

            File f = (backend.getProcessedGcodeFile() != null) ? backend.getProcessedGcodeFile()
                    : backend.getGcodeFile();
            if (f != null) {
                this.gcodeModel.setGcodeFile(f.getAbsolutePath());
            }

            return gcodeRenderer.getCanvas();
        } catch (GLException exception) {
            JLabel errorMessage = new JLabel(
                    "<html>Could not initialize OpenGL visualization, please check the log file for details <a href='#'>messages.log</a></html>",
                    JLabel.CENTER);
            errorMessage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new OpenLogDirectoryAction().actionPerformed(null);
                }
            });
            return errorMessage;
        }
    }

    public void setGcodeFile(String file) {
        gcodeModel.setGcodeFile(file);
        // gcodeRenderer.setObjectSize(gcodeModel.getMin(), gcodeModel.getMax());

        gcodeRenderer.setGcode(gcodeModel);

        // updateBounds(gcodeModel.getMin(), gcodeModel.getMax());
    }

    /**
     * Pass new bounds (after interpolating arcs) in case of weird arcs.
     */
    private void updateBounds(Position min, Position max) {
        // Update bounds.
        // FileStats fs = settings.getFileStats();
        // fs.minCoordinate = new Position(min.x, min.y, min.z, Units.MM);
        // fs.maxCoordinate = new Position(max.x, max.y, max.z, Units.MM);
        // settings.setFileStats(fs);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            // animator.pause();
            FileStateEvent fileStateEvent = (FileStateEvent) event;
            switch (fileStateEvent.getFileState()) {
                case FILE_UNLOADED:
                    setGcodeFile(null);
                    break;
                case FILE_LOADED:
                    setGcodeFile(backend.getProcessedGcodeFile().getAbsolutePath());
                    break;
                case FILE_STREAM_COMPLETE:
                    gcodeModel.setCurrentCommandNumber(0);
                    break;
            }

            // animator.resume();
        } else if (event instanceof SettingChangedEvent) {
            // sizeDisplay.setUnits(settings.getPreferredUnits());
        } else if (event instanceof ControllerStateEvent) {
            ControllerStateEvent controllerStateEvent = (ControllerStateEvent) event;
            gcodeRenderer.setControllerState(controllerStateEvent.getState());
        } else if (event instanceof ControllerStatusEvent) {
            ControllerStatusEvent controllerStatusEvent = (ControllerStatusEvent) event;
            gcodeRenderer.setMachineCoordinate(controllerStatusEvent.getStatus().getMachineCoord());
            gcodeRenderer.setWorkCoordinate(controllerStatusEvent.getStatus().getWorkCoord());
        } else if (event instanceof CommandEvent) {
            CommandEvent commandEvent = (CommandEvent) event;
            if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_COMPLETE
                    && !commandEvent.getCommand().isGenerated()) {
                gcodeRenderer.setCurrentCommandNumber(commandEvent.getCommand().getCommandNumber());
            }
        }
    }
}
