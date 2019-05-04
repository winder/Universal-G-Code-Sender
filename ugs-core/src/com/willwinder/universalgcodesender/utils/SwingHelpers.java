package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author wwinder
 */
public class SwingHelpers {
  private static final String[] UNIT_OPTIONS = {
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

  public static Optional<File> openDirectory(String title, File defaultDirectory) {
    JFileChooser chooser = new JFileChooser();
    if(defaultDirectory != null && defaultDirectory.isDirectory()) {
      chooser.setCurrentDirectory(defaultDirectory);
    }
    chooser.setDialogTitle(title);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory();
      }

      @Override
      public String getDescription() {
        return "Directories";
      }
    });

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      return Optional.of(chooser.getSelectedFile());
    }

    return Optional.empty();
  }

  @FunctionalInterface
  public interface ApplyToComponent {
    public void apply(JComponent component);
  }

  /**
   * Provides component hierarchy traversal.
   *
   * @param aContainer start node for the traversal.
   */
  public static void traverse(Container aContainer, ApplyToComponent applicator) {
      for (final Component comp : aContainer.getComponents()) {
          if (comp instanceof JComponent) {
            applicator.apply((JComponent) comp);
          }
          if (comp instanceof Container) {
              traverse((Container) comp, applicator);
          }
      }
  }
}
