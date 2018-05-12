/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.willwinder.ugp.content;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author wwinder
 */
class TestTab extends AbstractTab {
  final String contentString;
  public TestTab(String title, String content) {
    super(title);
    this.contentString = content;
  }

  @Override
  protected JComponent buildContent() {
    JPanel panel = new JPanel(new GridLayout(1,0));
    panel.setOpaque(false);
    panel.add(new ContentSection( new JLabel(contentString), false ));
    panel.add(new ContentSection( new JLabel(contentString), false ));
    panel.add(new ContentSection( new JLabel(contentString), false ));

    //panel.setBackground(Color.RED);
    //panel.add(new JLabel("A Test Tab."));
    return panel;
  }
  
}
