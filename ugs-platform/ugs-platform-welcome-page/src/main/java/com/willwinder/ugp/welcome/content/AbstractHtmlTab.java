package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHtmlTab extends AbstractTab {
    private static final Logger LOGGER = Logger.getLogger(AbstractHtmlTab.class.getSimpleName());
    private String content = "";

    public AbstractHtmlTab(String title, InputStream resource) {
        super(title);
        try {
            content = IOUtils.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read from stream", e);
        }
    }

    @Override
    protected JComponent buildContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 14, 32, 14));

        JEditorPane editorPane = new JEditorPane("text/html", replaceFileLinks(content));
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setFont(Constants.GET_STARTED_FONT);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.addHyperlinkListener(this::onHyperLink);

        if (isDarkLaF()) {
            editorPane.setForeground(Constants.COLOR_TEXT_DARK_LAF);
        } else {
            editorPane.setForeground(Constants.COLOR_TEXT);
        }

        contentPanel.add(editorPane, BorderLayout.CENTER);
        return contentPanel;
    }

    private void onHyperLink(HyperlinkEvent hyperlinkEvent) {
        // Ignore other events
        if (!HyperlinkEvent.EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
            return;
        }

        try {
            if (hyperlinkEvent.getDescription().startsWith("http")) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(hyperlinkEvent.getDescription()));
                }
            } else {
                openLink(hyperlinkEvent.getDescription());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not open link " + hyperlinkEvent, ex);
        }
    }

    public abstract void openLink(String link);

    private String replaceFileLinks(String content) {
        String[] uris = StringUtils.substringsBetween(content, "\"file://", "\"");
        if (uris == null) {
            return content;
        }

        for (String uri : uris) {
            URL resource = AbstractHtmlTab.class.getResource(uri);
            if (resource != null) {
                content = content.replace("file://" + uri, resource.toString());
            }
        }

        return content;
    }
}
