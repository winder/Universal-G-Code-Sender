/*
    Copyright 2017 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.core.windows;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.willwinder.universalgcodesender.gcode.util.Code.*;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbp.core.windows//State//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "StateTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "top_left", openAtStartup = false)
@ActionID(
        category = LocalizingService.StateCategory,
        id = LocalizingService.StateActionId)
@ActionReference(path = LocalizingService.StateWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:StateAction>",
        preferredID = "StateTopComponent")
public final class StateTopComponent extends TopComponent implements UGSEventListener {
  private static final Duration REFRESH_RATE = Duration.ofSeconds(1);
  private final BackendAPI backend;
  private final Timer statePollTimer;

  private static final Map<Code, String> codes = new HashMap<>();

  static {
    // Motion
    codes.put(G0, Localization.getString("gcode.g0"));
    codes.put(G1, Localization.getString("gcode.g1"));
    codes.put(G2, Localization.getString("gcode.g2"));
    codes.put(G3, Localization.getString("gcode.g3"));
    codes.put(G38_2, Localization.getString("gcode.g38.2"));
    codes.put(G38_4, Localization.getString("gcode.g38.4"));
    codes.put(G38_5, Localization.getString("gcode.g38.5"));
    codes.put(G80, Localization.getString("gcode.g80"));

    // Units
    codes.put(G20, Localization.getString("gcode.g20"));
    codes.put(G21, Localization.getString("gcode.g21"));

    // Active Plane
    codes.put(G17, Plane.lookup(G17).toString());
    codes.put(G18, Plane.lookup(G18).toString());
    codes.put(G19, Plane.lookup(G19_1).toString());
    codes.put(G17_1, Plane.lookup(G17_1).toString());
    codes.put(G18_1, Plane.lookup(G18_1).toString());
    codes.put(G19_1, Plane.lookup(G19_1).toString());

    // Distance Mode
    codes.put(G90, Localization.getString("gcode.g90"));
    codes.put(G91, Localization.getString("gcode.g91"));

    // Feed Rate Mode
    codes.put(G93, Localization.getString("gcode.g93"));
    codes.put(G94, Localization.getString("gcode.g94"));
    codes.put(G95, Localization.getString("gcode.g95"));
  }

  private JComboBox<String> motionBox = new JComboBox<>(new String[]{
    "G0", "G1", "G2", "G3", "G38_2", "G38_3", "G38_4", "G38_5", "G80"});
  private JComboBox<String> unitBox = new JComboBox<>(new String[]{get(G20), get(G21)});
  private JComboBox<String> feedModeBox = new JComboBox<>(new String[]{get(G93), get(G94), get(G95)});
  private JComboBox<String> distanceModeBox = new JComboBox<>(new String[]{get(G90), get(G91)});
  private JComboBox<String> workOffsetBox = new JComboBox<>(new String[]{
      get(G54), get(G55), get(G56), get(G57), get(G58), get(G59), get(G59_1), get(G59_2), get(G59_3)});
  private JComboBox<String> planeBox = new JComboBox<>(new String[]{get(G17), get(G18), get(G19), get(G17_1), get(G18_1), get(G19_1)});

  private JTextField feedBox = new JTextField("0");
  private JTextField speedBox = new JTextField("0");

  private volatile boolean loading = true;

  public StateTopComponent() {
    initComponents();
    setName(LocalizingService.StateTitle);
    setToolTipText(LocalizingService.StateTooltip);

    backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    backend.addUGSEventListener(this);

    statePollTimer = createTimer();

    setLayout(new MigLayout("wrap 2, fillx"));

    add(new JLabel(Localization.getString("gcode.current-motion")));
    add(motionBox, "growx");
    motionBox.setEnabled(false);

    add(new JLabel(Localization.getString("gcode.setting.units")));
    add(unitBox, "growx");

    add(new JLabel(Localization.getString("gcode.distance-mode")));
    add(distanceModeBox, "growx");

    //add(new JLabel(Localization.getString("gcode.feed-rate-mode")));
    //add(feedModeBox, "growx");

    add(new JLabel(Localization.getString("gcode.work-offset")));
    add(workOffsetBox, "growx");

    add(new JLabel(Localization.getString("gcode.plane")));
    add(planeBox, "growx");

    add(new JLabel(Localization.getString("gcode.setting.feed")));
    add(feedBox, "growx");

    add(new JLabel(Localization.getString("overrides.spindle.short")));
    add(speedBox, "growx");

    ItemListener codeComboListener = (ItemEvent ie) -> {
      // Only interested in the new item, don't fire when programatically setting selection.
      if (loading || ie.getStateChange() == ItemEvent.DESELECTED) return;

      String item = ie.getItem().toString();

      System.out.println("item");
      String code = item;
      if (item.contains("(")) {
        code = item.substring(item.lastIndexOf('(') + 1, item.lastIndexOf(')'));
      }

      if (backend.isIdle()) {
        try {
          backend.sendGcodeCommand(code);
        } catch (Exception ex) {
          GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
      }
    };

    motionBox.addItemListener(codeComboListener);
    unitBox.addItemListener(codeComboListener);
    feedModeBox.addItemListener(codeComboListener);
    distanceModeBox.addItemListener(codeComboListener);
    workOffsetBox.addItemListener(codeComboListener);
    planeBox.addItemListener(codeComboListener);

    feedBox.addActionListener((al) -> executeNumber('F', feedBox));
    speedBox.addActionListener((al) -> executeNumber('S', speedBox));
  }

  private void setFeedAndSpeed(Double feed, Double speed) {
    if (!feedBox.hasFocus()) {
      feedBox.setText(Integer.toString(feed.intValue()));
    }
    if (!speedBox.hasFocus()) {
      speedBox.setText(Integer.toString(speed.intValue()));
    }
  }

  private void executeNumber(char word, JTextField value) {
    if (!StringUtils.isNumeric(value.getText())) {
      value.setText("0");
      GUIHelpers.displayErrorDialog("Provide numeric input.");
      return;
    }
    if (backend.isIdle()) {
      try {
        backend.sendGcodeCommand(word + value.getText());
      } catch (Exception ex) {
        GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
      }
    }
  }

  private void enableBoxes(boolean enabled) {
    unitBox.setEnabled(enabled);
    feedModeBox.setEnabled(enabled);
    distanceModeBox.setEnabled(enabled);
    workOffsetBox.setEnabled(enabled);
    planeBox.setEnabled(enabled);

    feedBox.setEnabled(enabled);
    speedBox.setEnabled(enabled);
  }

  @Override
  public void UGSEvent(UGSEvent evt) {
      enableBoxes(backend.isIdle());
  }


  private String get(Code c) {
    if (codes.containsKey(c)) {
      return codes.get(c) + " (" + c + ")";
    } else {
      return c.toString();
    }
  }

  private Timer createTimer() {
    return new Timer((int) REFRESH_RATE.toMillis(), (ae) -> {
      java.awt.EventQueue.invokeLater(() -> {
        GcodeState state = backend.getGcodeState();
        if (state == null) return;

        try {
          loading = true;

          motionBox.setSelectedItem(state.currentMotionMode.toString());
          unitBox.setSelectedItem(get(state.units));
          feedModeBox.setSelectedItem(get(state.feedMode));
          distanceModeBox.setSelectedItem(get(state.distanceMode));
          workOffsetBox.setSelectedItem(get(state.offset));
          planeBox.setSelectedItem(get(state.plane.code));

          this.setFeedAndSpeed(state.speed, state.spindleSpeed);
        } finally {
          loading = false;
        }
      });
    });
  }

  private void initComponents() {
  }

  @Override
  public void componentOpened() {
    statePollTimer.start();
  }

  @Override
  public void componentClosed() {
    statePollTimer.stop();
  }

  public void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
  }

  public void readProperties(java.util.Properties p) {
  }
}
