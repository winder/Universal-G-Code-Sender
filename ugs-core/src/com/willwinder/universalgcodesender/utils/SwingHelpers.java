package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import java.io.File;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author wwinder
 */
public class SwingHelpers {
  static final String[] UNIT_OPTIONS = {
    Localization.getString("mainWindow.swing.mmRadioButton"),
    Localization.getString("mainWindow.swing.inchRadioButton")
  };

  public static String[] getUnitOptions() {
    return UNIT_OPTIONS;
  }

  public static UnitUtils.Units selectedUnit(int idx) {
    return idx == 0 ? UnitUtils.Units.MM : UnitUtils.Units.INCH;
  }

  public static int unitIdx(UnitUtils.Units units) {
    if (units == UnitUtils.Units.INCH) {
      return 1;
    } else {
      return 0;
    }
  }

  // deal with casting the spinner model to a double.
  public static double getDouble(SpinnerNumberModel model) {
    return (double) model.getValue();
  }

  // deal with casting the spinner model to a double.
  public static int getInt(SpinnerNumberModel model) {
    return (int) model.getValue();
  }

  public static Optional<File> createFile(String sourceDir) {
    JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(sourceDir);
    int returnVal = fileChooser.showSaveDialog(new JFrame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return Optional.ofNullable(fileChooser.getSelectedFile());
    } else {
      return Optional.empty();
    }  
  }

  public static Optional<File> openFile(String sourceDir) {
    JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(sourceDir);
    int returnVal = fileChooser.showOpenDialog(new JFrame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return Optional.ofNullable(fileChooser.getSelectedFile());
    } else {
      return Optional.empty();
    }  
  }
}
