/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.AbstractController;

import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread utils
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class ThreadHelper {
    private static final Logger logger = Logger.getLogger(AbstractController.class.getName());

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static public void invokeLater(Runnable r) {
        executor.submit(r);
    }

    /**
     * This method will wait for the given supplier to become true.
     * It will wait for the maximum given timeout and units and then throw a
     * timeout exception
     *
     * <p>There is no requirement that a new or distinct result be returned each
     * time the supplier is invoked.
     *
     * <p>Examples of usage:
     * <pre>{@code
     * // Will wait ten seconds and then throw a TimeoutException as true will never be equal to false
     * ThreadHelper.waitUntil(() -> { return true == false}, 10, TimeUnit.SECONDS);
     *
     * // Will return almost immediatly
     * ThreadHelper.waitUntil(() -> { return true == true}, 10, TimeUnit.SECONDS);
     * }</pre>
     *
     * @param waitUntilSupplier a supplier that will return true when we shouldn't wait anymore
     * @param timeout           the timeout value for the maximum time to wait until we abort
     * @param units             the unit of the timeout value
     * @throws TimeoutException if a timeout has occured
     */
    static public void waitUntil(final BooleanSupplier waitUntilSupplier, int timeout, TimeUnit units) throws TimeoutException {
        try {
            executor.submit(() -> {
                while (!waitUntilSupplier.getAsBoolean()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            }).get(timeout, units);
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "An error occured while waiting for the thread to finnish", e);
        }
    }
}
