/*
    Copyright 2016-2019 Will Winder

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

import com.willwinder.universalgcodesender.model.BackendAPI;

/**
 * A factory for accessing the backend in injected resources
 */
public class BackendAPIFactory {
    private static BackendAPIFactory instance;
    private BackendAPI backendAPI;

    public static BackendAPIFactory getInstance() {
        if (instance == null) {
            instance = new BackendAPIFactory();
        }
        return instance;
    }

    public void register(BackendAPI backendAPI) {
        this.backendAPI = backendAPI;
    }

    public BackendAPI getBackendAPI() {
        return backendAPI;
    }
}
