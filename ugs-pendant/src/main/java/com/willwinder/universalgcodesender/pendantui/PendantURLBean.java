/*
    Copyright 2016-2025 Will Winder

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
package com.willwinder.universalgcodesender.pendantui;

public class PendantURLBean {
    private final String displayName;
    private final String urlString;
    private final byte[] qrCodeJpg;

    public PendantURLBean(String urlString, String displayName, byte[] qrCodeJpg) {
        this.urlString = urlString;
        this.displayName = displayName;
        this.qrCodeJpg = qrCodeJpg;
    }

    public String getUrlString() {
        return urlString;
    }

    public byte[] getQrCodeJpg() {
        return qrCodeJpg;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return urlString;
    }
}
