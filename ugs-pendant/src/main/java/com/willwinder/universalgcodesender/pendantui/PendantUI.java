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
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class will launch a local webserver which will provide a simple pendant interface
 *
 * @author bobj
 */
public class PendantUI {
    private BackendAPI mainWindow;
    private Server server = null;
    private int port = 8080;
    private static final Logger LOG = Logger.getLogger(PendantUI.class.getSimpleName());

    public PendantUI(BackendAPI mainWindow) {
        this.mainWindow = mainWindow;
        BackendAPIFactory.getInstance().register(mainWindow);
    }

    public Resource getBaseResource(String directory) {
        try {
            URL res = getClass().getResource(directory);
            return Resource.newResource(res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Launches the local web server.
     *
     * @return the url for the pendant interface
     */
    public List<PendantURLBean> start() {
        server = new Server(port);

        ResourceHandler staticResourceHandler = new ResourceHandler();
        staticResourceHandler.setDirectoriesListed(true);
        staticResourceHandler.setWelcomeFiles(new String[]{"index.html"});
        staticResourceHandler.setBaseResource(getBaseResource("/resources/ugs-pendant"));

        ContextHandler staticResourceHandlerContext = new ContextHandler();
        staticResourceHandlerContext.setContextPath("/");
        staticResourceHandlerContext.setHandler(staticResourceHandler);

        // Create a servlet servletContextHandler
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/api");
        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(1);
        servletHolder.setInitParameter("javax.ws.rs.Application", AppConfig.class.getCanonicalName());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{servletContextHandler, staticResourceHandlerContext, new DefaultHandler()});
        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getUrlList();
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
                if (!hostAddress.contains(":") &&
                        !hostAddress.equals("127.0.0.1")) {
                    String url = "http://" + hostAddress + ":" + port;
                    ByteArrayOutputStream bout = QRCode.from(url).to(ImageType.PNG).stream();
                    out.add(new PendantURLBean(url, bout.toByteArray()));
                    LOG.info("Listening on: " + url);
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public BackendAPI getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(BackendAPI mainWindow) {
        this.mainWindow = mainWindow;
    }
}
