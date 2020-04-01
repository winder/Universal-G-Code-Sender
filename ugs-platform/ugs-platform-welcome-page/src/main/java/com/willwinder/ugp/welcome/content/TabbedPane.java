/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.WelcomePageOptions;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
public class TabbedPane extends JPanel implements Constants {// , Scrollable {
    // TabButton static variables
    private final static Color colBackground = COLOR_TAB_BACKGROUND; 
    private static final Image imgSelected = ImageUtilities.loadImage( IMAGE_TAB_SELECTED, true );
    private static final Image imgRollover = ImageUtilities.loadImage( IMAGE_TAB_ROLLOVER, true );
    private static final JLabel imgLogo = new JLabel(new ImageIcon(ImageUtilities.loadImage( IMAGE_LOGO, true )));

    // TabBorder static variables
    private static final Color COL__BORDER1 = new Color(72,93,112, 255);
    private static final Color COL__BORDER2 = new Color(72,93,112, 255);

    // TabbedPane variables
    private final List<JComponent> tabs;
    private final TabButton[] buttons;
    private final JPanel tabContent;
    private boolean[] tabAdded;
    private int selTabIndex = -1;
    
    public TabbedPane( List<JComponent> tabs ) {
        super( new BorderLayout() );

        setOpaque(false);
        
        this.tabs = tabs;
        tabAdded = new boolean[tabs.size()];
        Arrays.fill(tabAdded, false);

        for( JComponent c : tabs ) {
            c.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
            c.putClientProperty("print.name", c.getName()); // NOI18N
        }
        
        ActionListener al = (ActionEvent e) -> {
          TabButton btn = (TabButton) e.getSource();
          switchTab( btn.getTabIndex() );
          WelcomePageOptions.getDefault().setLastActiveTab( btn.getTabIndex() );
        };
        
        buttons = new TabButton[tabs.size()];
        for( int i=0; i<buttons.length; i++ ) {
            buttons[i] = new TabButton(tabs.get(i).getName(), i);
            buttons[i].addActionListener(al);
        }

        
        JComponent tabHeader = new TabHeader(buttons);
        add( tabHeader, BorderLayout.NORTH );
        
        tabContent = new TabContentPane();//JPanel( new GridBagLayout() );

        add( tabContent, BorderLayout.CENTER );
        int activeTabIndex = WelcomePageOptions.getDefault().getLastActiveTab();
        if( WelcomePageOptions.getDefault().isSecondStart() && activeTabIndex < 0 ) {
            activeTabIndex = 1;
            WelcomePageOptions.getDefault().setLastActiveTab( 1 );
        }
        activeTabIndex = Math.max(0, activeTabIndex);
        activeTabIndex = Math.min(activeTabIndex, tabs.size()-1);
        switchTab( activeTabIndex );
    }

    private void switchTab( int tabIndex ) {
        if( !tabAdded[tabIndex] ) {
            tabContent.add( tabs.get(tabIndex), new GridBagConstraints(tabIndex, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0) ); //NOI18N
            tabAdded[tabIndex] = true;
        }
        if( selTabIndex >= 0 ) {
            buttons[selTabIndex].setSelected(false);
        }
        JComponent compToShow = tabs.get(tabIndex);
        JComponent compToHide = selTabIndex >= 0 ? tabs.get(selTabIndex) : null;
        selTabIndex = tabIndex;
        buttons[selTabIndex].setSelected(true);

        if( null != compToHide )
            compToHide.setVisible( false );
        
        compToShow.setVisible( true );
        compToShow.requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if( null != getParent() && null != getParent().getParent() ) {
            Component scroll = getParent().getParent();
            if( scroll.getWidth() > 0 ) {
                if( d.width > scroll.getWidth() ) {
                    d.width = Math.max(scroll.getWidth(), START_PAGE_MIN_WIDTH+(int)(((FONT_SIZE-11)/11.0)*START_PAGE_MIN_WIDTH));
                } else if( d.width < scroll.getWidth() ) {
                    d.width = scroll.getWidth();
                }
            }
        }
        d.width = Math.min( d.width, 1000 );
        return d;
    }

    private static class TabButton extends JPanel {
        private boolean isSelected = false;
        private ActionListener actionListener;
        private final int tabIndex;
        private final JLabel lblTitle = new JLabel();
        private boolean isMouseOver = false;
        
        public TabButton( String title, int tabIndex ) {
            super( new BorderLayout() );
            lblTitle.setText( title );
            add( lblTitle, BorderLayout.CENTER );
            this.tabIndex = tabIndex;
            setOpaque( true );
            lblTitle.setFont( TAB_FONT );
            lblTitle.setForeground( Color.white );
            lblTitle.setHorizontalAlignment( JLabel.CENTER );
            setFocusable(true);
            setBackground( colBackground );
            
            addKeyListener( new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if( (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
                            && (null != actionListener)) {
                        actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                    }
                }
            });

            addMouseListener( new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
//                    setSelected( !isSelected );
                    if( null != actionListener ) {
                        actionListener.actionPerformed( new ActionEvent( TabButton.this, 0, "clicked") );
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if( !isSelected ) {
                        setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                    } else {
                        setCursor( Cursor.getDefaultCursor() );
                    }
                    isMouseOver = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor( Cursor.getDefaultCursor() );
                    isMouseOver = false;
                    repaint();
                }
            });
            
            addFocusListener( new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    isMouseOver = true;
                    repaint();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    isMouseOver = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent( Graphics g ) {
            Graphics2D g2d = ( Graphics2D ) g;
            if( isSelected ) {
                g2d.drawImage( imgSelected, 0, 0, getWidth(), getHeight(), this );
            } else if( isMouseOver || isFocusOwner() || lblTitle.isFocusOwner() ) {
                g2d.drawImage( imgRollover, 0, 0, getWidth(), getHeight(), this );
            } else {
                super.paintComponent( g );
            }
        }
        
        public void addActionListener( ActionListener l ) {
            assert null == actionListener;
            this.actionListener = l;
        }
        
        public void setSelected( boolean sel ) {
            this.isSelected = sel;
            
            setFocusable(!sel);
            repaint();
        }

        public int getTabIndex() {
            return tabIndex;
        }
    }

    private class TabHeader extends JPanel {

        private final ShowNextTime showNextTime = new ShowNextTime();
        private final JComponent appLogo = new JPanel(new BorderLayout());


        public TabHeader( TabButton ... buttons ) {
            super( new GridBagLayout() );

            appLogo.add(imgLogo);

            setOpaque(false);
            JPanel panelButtons = new JPanel( new GridLayout( 1, 0 ) );
            panelButtons.setOpaque( false );
            for( int i=0; i<buttons.length; i++ ) {
                TabButton btn = buttons[i];
                btn.setBorder(new TabBorder( i == buttons.length-1 ));
                panelButtons.add( btn );
            }

            add( appLogo, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,12,5,12), 0, 0) );
            add( new JLabel(), new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0) );
            add( panelButtons, new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0, 0) );
            add( new JLabel(), new GridBagConstraints( 3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0) );
            add( showNextTime, new GridBagConstraints( 4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15,12,15,12), 0, 0) );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g2d.setPaint( colBackground );
            g2d.fillRoundRect( 0, 0, getWidth(), getHeight(), 8, 8 );
        }

        @Override
        public void addNotify() {
            super.addNotify();
            SwingUtilities.invokeLater(showNextTime::requestFocusInWindow);
        }
    }

    private static final class TabBorder implements Border {

        private final boolean isLastButton;

        public TabBorder( boolean isLastButton ) {
            this.isLastButton = isLastButton;
        }

        @Override
        public void paintBorder( Component c, Graphics g, int x, int y, int width, int height ) {
            g.setColor( COL__BORDER2 );
            g.drawRect( x, y, width-1, height-1 );
            g.setColor( COL__BORDER1 );
            g.drawLine( x, y, x, height );
            if( isLastButton ) {
                g.drawLine( width-1, y, width-1, height );
            }
        }

        @Override
        public Insets getBorderInsets( Component c ) {
            return new Insets( 16, 16, 12, 16 );
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

    }
}