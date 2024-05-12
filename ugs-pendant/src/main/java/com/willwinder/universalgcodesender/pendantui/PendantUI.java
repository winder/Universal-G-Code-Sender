/*
    Copyright 2016-2023 Will Winder

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

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.pendantui.html.StaticConfig;
import com.willwinder.universalgcodesender.pendantui.v1.AppV1Config;
import com.willwinder.universalgcodesender.pendantui.v1.ws.EventsSocket;
import com.willwinder.universalgcodesender.services.JogService;
import jakarta.ws.rs.core.UriBuilder;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class will launch a local webserver which will provide a simple pendant interface
 *
 * @author bobj
 */
public class PendantUI implements UGSEventListener {
    public static final String WEBSOCKET_CONTEXT_PATH = "/ws/v1";
    public static final String API_CONTEXT_PATH = "/api/v1";
    private static final Logger LOG = Logger.getLogger(PendantUI.class.getSimpleName());
    private final JogService jogService;
    private final BackendAPI backendAPI;
    private int port = 8080;
    private Server server;

    public PendantUI(BackendAPI backendAPI) {
        this.backendAPI = backendAPI;
        backendAPI.addUGSEventListener(this);
        jogService = new JogService(backendAPI);
        BackendProvider.register(backendAPI);
    }

    /**
     * Launches the local web server.
     *
     * @return the url for the pendant interface
     */
    public List<PendantURLBean> start() {
        port = backendAPI.getSettings().getPendantPort();
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();

        server = JettyHttpContainerFactory.createServer(baseUri, false);
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        server.setHandler(contextHandlerCollection);

        contextHandlerCollection.addHandler(createResourceConfigHandler(new StaticConfig(), ""));
        contextHandlerCollection.addHandler(createResourceConfigHandler(new AppV1Config(backendAPI, jogService), API_CONTEXT_PATH));
        contextHandlerCollection.addHandler(createResourceConfigHandler(new StaticConfig(), "/*"));
        contextHandlerCollection.addHandler(createWebSocketHandler(WEBSOCKET_CONTEXT_PATH));

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getUrlList();
    }

    private ServletContextHandler createResourceConfigHandler(ResourceConfig config, String path) {
        ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder servletHolder = new ServletHolder(servletContainer);
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addServlet(servletHolder, "/*");
        return servletContextHandler;
    }

    private ServletContextHandler createWebSocketHandler(String contextPath) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(contextPath);
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
            wsContainer.setDefaultMaxTextMessageBufferSize(65535);
            wsContainer.addEndpoint(EventsSocket.class);
        });
        return context;
    }

    /**
     * Unfortunately, this is not as simple as it seems... since you can have multiple addresses and some of those may not be available via wireless
     *
     * @return
     */
    public List<PendantURLBean> getUrlList() {
        List<PendantURLBean> out = new ArrayList<>();

        Enumeration<NetworkInterface> networkInterfaceEnum;
        try {
            networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (networkInterfaceEnum.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnum.nextElement();

            Enumeration<InetAddress> addressEnum = networkInterface.getInetAddresses();
            while (addressEnum.hasMoreElements()) {
                InetAddress addr = addressEnum.nextElement();
                String hostAddress = addr.getHostAddress();
                if (!hostAddress.contains(":") && !hostAddress.equals("127.0.0.1")) {
                    String url = "http://" + hostAddress + ":" + port;
                    ByteArrayOutputStream bout = QRCode.from(url).to(ImageType.PNG).stream();
                    out.add(new PendantURLBean(url, bout.toByteArray()));
                    LOG.info(() -> "Listening on: " + url);
                }
            }
        }

        return out;
    }

    public void stop() {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStarted() {
        return server != null && server.isStarted();
    }

    public BackendAPI getBackendAPI() {
        return backendAPI;
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent && (backendAPI.getSettings().getPendantPort() != port && isStarted())) {
            stop();
            start();
        }
    }
}
