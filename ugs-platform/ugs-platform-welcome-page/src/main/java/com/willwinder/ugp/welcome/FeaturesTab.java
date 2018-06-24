/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugp.welcome;

import com.willwinder.ugp.welcome.content.AbstractTab;
import com.willwinder.ugp.welcome.content.Constants;
import com.willwinder.ugp.welcome.content.ContentSection;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class FeaturesTab extends AbstractTab implements Constants {
  private static Logger logger = Logger.getLogger(FeaturesTab.class.getName());

  private final Collection<Feature> features;

  public static class Feature {
    public final Date version;
    public final String name;
    public final String description;
    public final String image;

    public Feature(Date version, String name, String description, String image) {
      this.version = version;
      this.name = name;
      this.description = description;
      this.image = image;
    }
  }

  public FeaturesTab(String title, Collection<Feature> features) {
    super(title);
    this.features = features;
  }

  @Override
  protected JComponent buildContent() {
    JPanel contentPanel = new JPanel(new MigLayout("fill, wrap 1"));
    contentPanel.setBackground(COLOR_TAB_CONTENT_BACKGROUND);

    // Add features.
    boolean left = true;
    for (Feature f : features) {
      try {
        JPanel featurePanel = getFeaturePanel(f, left);
        if (featurePanel != null) {
          contentPanel.add(featurePanel);
          left = !left;
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Issue loading feature: " + f.name, e);
      }
    }

    return new ContentSection( contentPanel, false);
  }

  private JPanel getFeaturePanel(Feature f, Boolean imageOnLeft) {
    JPanel panel = new JPanel(new MigLayout());
    JComponent image = getImage(f.image);
    JLabel label = getLabel(FEATURE_FONT, f.description);
    
    panel.add(getLabel(FEATURE_HEADER_FONT, f.name), "al center, span 2, wrap");
    panel.add(imageOnLeft ? image : label);
    panel.add(imageOnLeft ? label : image);
    panel.setBackground(imageOnLeft ? COLOR_TAB_CONTENT_BACKGROUND : COLOR_TAB_CONTENT_BACKGROUND2);

    return panel;
  }

  private JComponent getImage(String uri) {
    // TODO: Load images remotely, this is needed when the "what's new" tab is implemented.
    if (uri.startsWith("http")) {
      throw new IllegalArgumentException("Not supported yet.");
    }

    Image image = Toolkit.getDefaultToolkit().getImage(FeaturesTab.class.getClassLoader().getResource(uri));
    return new JLabel(new ImageIcon(image));
  }
}
