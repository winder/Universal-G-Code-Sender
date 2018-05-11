/*
    Copyright 2015-2018 Will Winder

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
package com.willwinder.universalgcodesender.connection;

import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for creating a serial connection object using the settings
 *
 * @author wwinder
 */
public class ConnectionFactory {

    private static final Logger logger = Logger.getLogger(ConnectionFactory.class.getSimpleName());

    static public Connection getConnection() {
        Settings settings = SettingsFactory.loadSettings();
        String connectionClass = settings.getConnectionClass();
        return createInstance(connectionClass)
                .orElse(new JSSCConnection());
    }

    /**
     * Tries to create a connection instance with the given class.
     *
     * @param connectionClass the full class name of the connection class
     * @return a created instance of the given class or an empty optional
     */
    private static Optional<Connection> createInstance(String connectionClass) {
        try {
            Class<?> loadedClass = Class.forName(connectionClass);
            Class<? extends Connection> loadedConnectionClass = loadedClass.asSubclass(Connection.class);
            return Optional.of(loadedConnectionClass.newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.log(Level.WARNING, "Couldn't load connection using class " + connectionClass, e);
        }

        return Optional.empty();
    }
}
