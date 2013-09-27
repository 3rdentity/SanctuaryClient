/*
 * Copyright 2008 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.m3m.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Based on a blog post from Alexander Potochkin at the following url:
 * http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html
 *
 * @author Alexander Potochkin
 * @author Stephen Chin
 * @author Keith Combs
 *
 * A fix was added by:
 * @author Thunder
 */
public class JXTrayIcon extends TrayIcon {

    private JPopupMenu menu;
    private JWindow jWindow;
    private JFrame jFrame;
    private PopupMenuListener popupListener = new PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            jWindow.setVisible(false);
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            jWindow.setVisible(false);
        }
    };

    public JXTrayIcon(Image image, JFrame frame) {
        super(image);
        jWindow = new JWindow(frame);
        jWindow.setAlwaysOnTop(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                showJPopupMenu(e);
            }
        });
    }

    private void showJPopupMenu(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && menu != null) {
            Dimension size = menu.getPreferredSize();
            int adjustedY = e.getY() - size.height;
            jWindow.setLocation(e.getX(), adjustedY < 0 ? e.getY() : adjustedY);
            jWindow.setVisible(true);
            menu.show(jWindow.getContentPane(), 0, 0);

            // popup works only for focused window:
            jWindow.toFront();
        }
    }

    public JPopupMenu getJPopupMenu() {
        return menu;
    }

    public void setJPopupMenu(JPopupMenu menu) {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(popupListener);
        }

        if (menu != null) {
            this.menu = menu;
            this.menu.addPopupMenuListener(popupListener);
            this.menu.setInvoker(jFrame);
        }
    }
}
