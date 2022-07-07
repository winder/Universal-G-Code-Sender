package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import org.openide.util.ImageUtilities;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.willwinder.ugp.welcome.Constants.IMAGE_TAB_ROLLOVER;
import static com.willwinder.ugp.welcome.Constants.IMAGE_TAB_SELECTED;
import static com.willwinder.ugp.welcome.Constants.TAB_FONT;

public class TabButton extends JPanel {
    private static final Image imgSelected = ImageUtilities.loadImage(IMAGE_TAB_SELECTED, true);
    private static final Image imgRollover = ImageUtilities.loadImage(IMAGE_TAB_ROLLOVER, true);
    private final int tabIndex;
    private final JLabel lblTitle = new JLabel();
    private boolean isSelected = false;
    private ActionListener actionListener;
    private boolean isMouseOver = false;
    private Component pressedComponent;

    public TabButton(String title, int tabIndex) {
        super(new BorderLayout());
        lblTitle.setText(title);
        add(lblTitle, BorderLayout.CENTER);
        this.tabIndex = tabIndex;
        setOpaque(true);
        lblTitle.setFont(TAB_FONT);
        lblTitle.setForeground(Color.white);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        setFocusable(true);
        setBackground(Constants.COLOR_HEADER_BACKGROUND);

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
                        && (null != actionListener)) {
                    actionListener.actionPerformed(new ActionEvent(TabButton.this, 0, "clicked"));
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (null != actionListener) {
                    actionListener.actionPerformed(new ActionEvent(TabButton.this, 0, "clicked"));
                }
            }

            @Override
            public void mousePressed(MouseEvent exc) {
                pressedComponent = exc.getComponent();
            }

            @Override
            public void mouseReleased(MouseEvent exc) {
                if (pressedComponent != null && pressedComponent.contains(exc.getPoint())) {
                    if (null != actionListener) {
                        actionListener.actionPerformed(new ActionEvent(TabButton.this, 0, "clicked"));
                    }
                }

                pressedComponent = null;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                isMouseOver = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                isMouseOver = false;
                repaint();
            }
        });

        addFocusListener(new FocusListener() {
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
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (isSelected) {
            g2d.drawImage(imgSelected, 0, 0, getWidth(), getHeight(), this);
        } else if (isMouseOver || isFocusOwner() || lblTitle.isFocusOwner()) {
            g2d.drawImage(imgRollover, 0, 0, getWidth(), getHeight(), this);
        } else {
            super.paintComponent(g);
        }
    }

    public void addActionListener(ActionListener l) {
        assert null == actionListener;
        this.actionListener = l;
    }

    public void setSelected(boolean sel) {
        this.isSelected = sel;

        setFocusable(!sel);
        repaint();
    }

    public int getTabIndex() {
        return tabIndex;
    }
}
