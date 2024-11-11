package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import mslinks.ShellLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@ActionID(
        category = LocalizingService.CATEGORY_TOOLS,
        id = "com.willwinder.ugs.nbp.core.actions.CreateShortcutAction")
@ActionRegistration(
        displayName = "resources/MessagesBundle#platform.action.createShortcut",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_TOOLS,
                position = 2110)
})
public class CreateShortcutAction extends AbstractAction {

    public static final String LINUX_DESKTOP_FILE = System.getProperty("user.home") + "/.local/share/applications/ugs.desktop";

    public CreateShortcutAction() {
        setEnabled(isEnabled());
        putValue(NAME, Localization.getString("platform.action.createShortcut"));
    }

    private static void createLinuxShortcut() {
        Path currentRelativePath = Paths.get("");
        String ugsDirectory = currentRelativePath.toAbsolutePath().toString();
        String desktopFile = "[Desktop Entry]\n" +
                "Type=Application\n" +
                "Version=1.0\n" +
                "Name=Universal Gcode Sender\n" +
                "Comment=Control your CNC machine\n" +
                "Path=" + ugsDirectory + "\n" +
                "Exec=" + ugsDirectory + File.separator + "ugsplatform %F\n" +
                "Icon=" + ugsDirectory + File.separator + "icon.svg\n" +
                "MimeType=text/x-gcode\n" +
                "Terminal=false\n" +
                "StartupWMClass=Universal Gcode Sender\n" +
                "Categories=Engineering\n" +
                "Keywords=ugs\n";
        try {
            FileUtils.write(new File(LINUX_DESKTOP_FILE), desktopFile, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (SystemUtils.IS_OS_LINUX) {
            createLinuxShortcut();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            createWindowsShortcut();
        }
    }

    private static void createWindowsShortcut() {
        try {
            String shortcutPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Universal Gcode Sender.lnk";
            Path currentRelativePath = Paths.get("");

            String basePath = currentRelativePath.toAbsolutePath() + File.separator + "bin" + File.separator;
            String executablePath = basePath + "ugsplatform64.exe";
            if (!new File(executablePath).exists()) {
                executablePath = basePath + "ugsplatform.exe";
            }

            ShellLink.createLink(executablePath)
                    .setWorkingDir(currentRelativePath.toAbsolutePath().toString())
                    .setIconLocation(basePath + "icon.ico")
                    .saveTo(shortcutPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEnabled() {
        return SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS;
    }
}
