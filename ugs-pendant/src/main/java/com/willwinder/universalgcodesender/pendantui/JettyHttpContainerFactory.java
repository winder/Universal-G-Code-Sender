/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.willwinder.universalgcodesender.pendantui;

import jakarta.ws.rs.ProcessingException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;
import org.glassfish.jersey.process.JerseyProcessingUncaughtExceptionHandler;
import org.glassfish.jersey.server.spi.Container;

import java.net.URI;
import java.util.concurrent.ThreadFactory;

/**
 * Copied from glasfish source as the shaded variant will throw an unsupported exception by default.
 * <p/>
 * Factory for creating and starting Jetty server handlers. This returns
 * a handle to the started server as {@link Server} instances, which allows
 * the server to be stopped by invoking the {@link org.eclipse.jetty.server.Server#stop()} method.
 * <p/>
 * To start the server in HTTPS mode an {@link SslContextFactory} can be provided.
 * This will be used to decrypt and encrypt information sent over the
 * connected TCP socket channel.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 * @author Marek Potociar
 */
public final class JettyHttpContainerFactory {

    private JettyHttpContainerFactory() {
    }

    /**
     * Creates a {@link Server} instance that registers an {@link org.eclipse.jetty.server.Handler}.
     *
     * @param uri   uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *              segment will be used as context path, the rest will be ignored.
     * @param start if set to false, server will not get started, which allows to configure the underlying transport
     *              layer, see above for details.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     */
    public static Server createServer(final URI uri, final boolean start) throws ProcessingException {
        return createServer(uri, null, start);
    }

    /**
     * Create a {@link Server} that registers an {@link org.eclipse.jetty.server.Handler} that
     * in turn manages all root resource and provider classes found by searching the
     * classes referenced in the java classpath.
     *
     * @param uri               the URI to create the http server. The URI scheme must be
     *                          equal to {@code https}. The URI user information and host
     *                          are ignored. If the URI port is not present then port
     *                          {@value org.glassfish.jersey.server.spi.Container#DEFAULT_HTTPS_PORT} will be
     *                          used. The URI path, query and fragment components are ignored.
     * @param sslContextFactory this is the SSL context factory used to configure SSL connector
     * @param start             if set to false, server will not get started, this allows end users to set
     *                          additional properties on the underlying listener.
     * @return newly created {@link Server}.
     *
     * @throws ProcessingException      in case of any failure when creating a new Jetty {@code Server} instance.
     * @throws IllegalArgumentException if {@code uri} is {@code null}.
     * @see JettyHttpContainer
     */
    public static Server createServer(final URI uri,
                                      final SslContextFactory.Server sslContextFactory,
                                      final boolean start) {
        if (uri == null) {
            throw new IllegalArgumentException(LocalizationMessages.URI_CANNOT_BE_NULL());
        }
        final String scheme = uri.getScheme();
        int defaultPort = Container.DEFAULT_HTTP_PORT;

        if (sslContextFactory == null) {
            if (!"http".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(LocalizationMessages.WRONG_SCHEME_WHEN_USING_HTTP());
            }
        } else {
            if (!"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(LocalizationMessages.WRONG_SCHEME_WHEN_USING_HTTPS());
            }
            defaultPort = Container.DEFAULT_HTTPS_PORT;
        }
        final int port = (uri.getPort() == -1) ? defaultPort : uri.getPort();

        final Server server = new Server(new JettyConnectorThreadPool());
        final HttpConfiguration config = new HttpConfiguration();
        if (sslContextFactory != null) {
            config.setSecureScheme("https");
            config.setSecurePort(port);
            config.addCustomizer(new SecureRequestCustomizer());

            final ServerConnector https = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(config));
            https.setPort(port);
            server.setConnectors(new Connector[]{https});

        } else {
            final ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(config));
            http.setPort(port);
            server.setConnectors(new Connector[]{http});
        }

        if (start) {
            try {
                // Start the server.
                server.start();
            } catch (final Exception e) {
                throw new ProcessingException(LocalizationMessages.ERROR_WHEN_CREATING_SERVER(), e);
            }
        }
        return server;
    }

    // TODO: Use https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/util/thread/QueuedThreadPool.html
    //  #%3Cinit%3E(int,int,int,int,java.util.concurrent.BlockingQueue,java.lang.ThreadGroup,java.util.concurrent.ThreadFactory)
    //
    //  Keeping this for backwards compatibility for the time being
    private static final class JettyConnectorThreadPool extends QueuedThreadPool {
        private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("jetty-http-server-%d")
                .setUncaughtExceptionHandler(new JerseyProcessingUncaughtExceptionHandler())
                .build();

        @Override
        public Thread newThread(Runnable runnable) {
            return threadFactory.newThread(runnable);
        }
    }
}
