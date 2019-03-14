package com.willwinder.universalgcodesender.pendantui;

import com.google.gson.Gson;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class will launch a local webserver which will provide a simple pendant interface
 * @author bobj
 *
 */
public class PendantUI implements ControllerListener {
    private static final Logger logger = Logger.getLogger(PendantUI.class.getName());
    private BackendAPI mainWindow;
    private Server server = null;
    private int port = 8080;
    private SystemStateBean systemState = new SystemStateBean();
    
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
     * @return the url for the pendant interface
     */
    public List<PendantURLBean> start(){
        server = new Server(port);

        ResourceHandler pendantResourceHandler = new ResourceHandler();
        pendantResourceHandler.setDirectoriesListed(true);
        pendantResourceHandler.setWelcomeFiles(new String[]{"index.html"});
        pendantResourceHandler.setBaseResource(getBaseResource("/resources/ugs-pendant"));

        ContextHandler pendantResourceHandlerContext = new ContextHandler();
        pendantResourceHandlerContext.setContextPath("/");
        pendantResourceHandlerContext.setHandler(pendantResourceHandler);


        ResourceHandler oldPendantResourceHandler = new ResourceHandler();
        oldPendantResourceHandler.setDirectoriesListed(true);
        oldPendantResourceHandler.setWelcomeFiles(new String[]{"index.html"});
        oldPendantResourceHandler.setBaseResource(getBaseResource("/pendantUI/old"));

        ContextHandler oldPendantResourceHandlerContext = new ContextHandler();
        oldPendantResourceHandlerContext.setContextPath("/old");
        oldPendantResourceHandlerContext.setHandler(oldPendantResourceHandler);


        ContextHandler sendGcodeContext = new ContextHandler();
        sendGcodeContext.setContextPath("/sendGcode");
        sendGcodeContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        sendGcodeContext.setHandler(new SendGcodeHandler());

        ContextHandler adjustManualLocationContext = new ContextHandler();
        adjustManualLocationContext.setContextPath("/adjustManualLocation");
        adjustManualLocationContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        adjustManualLocationContext.setHandler(new AdjustManualLocationHandler());

        ContextHandler getSystemStateContext = new ContextHandler();
        getSystemStateContext.setContextPath("/getSystemState");
        getSystemStateContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        getSystemStateContext.setHandler(new GetSystemStateHandler());

        ContextHandler configContext = new ContextHandler();
        configContext.setContextPath("/config");
        configContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        configContext.setHandler(new ConfigHandler());
        configContext.setInitParameter("cacheControl", "max-age=0, public");

        // Create a servlet servletContextHandler
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/api");
        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(1);
        servletHolder.setInitParameter("javax.ws.rs.Application", AppConfig.class.getCanonicalName());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {servletContextHandler, configContext, sendGcodeContext, adjustManualLocationContext, getSystemStateContext, oldPendantResourceHandlerContext, pendantResourceHandlerContext, new DefaultHandler()});
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
    public List<PendantURLBean> getUrlList(){
        List<PendantURLBean> out = new ArrayList<>();
        
        Enumeration<NetworkInterface> networkInterfaceEnum;
        try {
            networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while(networkInterfaceEnum.hasMoreElements())
        {
            NetworkInterface networkInterface = networkInterfaceEnum.nextElement();

            Enumeration<InetAddress> addressEnum = networkInterface.getInetAddresses();
            while(addressEnum.hasMoreElements())
            {
                InetAddress addr = addressEnum.nextElement();
                String hostAddress = addr.getHostAddress();
               if(!hostAddress.contains(":") && 
                       !hostAddress.equals("127.0.0.1")){
                   String url = "http://"+hostAddress+":"+port;
                   ByteArrayOutputStream bout = QRCode.from(url).to(ImageType.PNG).stream();
                   out.add(new PendantURLBean(url, bout.toByteArray()));
                   System.out.println("Listening on: "+url);
               }
            }
        }
        
        return out;
    }

    public class SendGcodeHandler extends AbstractHandler{
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            String gCode = baseRequest.getParameter("gCode");
            
            try {
                if(isManualControlEnabled()){
                    switch (gCode) {
                        case "$H":
                            mainWindow.performHomingCycle();
                            break;
                        case "$X":
                            mainWindow.killAlarmLock();
                            break;
                        case "$C":
                            mainWindow.toggleCheckMode();
                            break;
                        case "RESET_ZERO":
                            mainWindow.resetCoordinatesToZero();
                            break;
                        case "RETURN_TO_ZERO":
                            mainWindow.returnToZero();
                            break;
                        case "SEND_FILE":
                            mainWindow.send();
                            break;
                        case "PAUSE_RESUME_FILE":
                        case "CANCEL_FILE":
                            break;
                        default:
                            try {
                                MacroHelper.executeCustomGcode(gCode, mainWindow);
                            } catch (Exception ex) {
                        	    System.err.println("pendant failed executing gCode [" + gCode + "]");
                        	    ex.printStackTrace();
                            }
                            break;
                    }
                } else {
                switch (gCode) {
                    case "PAUSE_RESUME_FILE":
                        mainWindow.pauseResume();
                        break;
                    case "CANCEL_FILE":
                        mainWindow.cancel();
                        break;
                    default:
                        break;
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception in pendant.", e);
                logger.warning(Localization.getString("SendGcodeHandler"));
            }

            response.getWriter().print(getSystemStateJson());
        }
    }
    
    public class GetSystemStateHandler extends AbstractHandler{
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            updateSystemState(systemState);
            response.getWriter().print(getSystemStateJson());
        }
    }

    public void updateSystemState(SystemStateBean systemStateBean) {
        logger.log(Level.FINE, "Getting system state 'updateSystemState'");
        File gcodeFile = getMainWindow().getGcodeFile();
        if (gcodeFile != null) {
            systemStateBean.setFileName(gcodeFile.getAbsolutePath());
        }
        // TODO how do we get the last comment
        //systemStateBean.setLatestComment(lastComment);
        systemStateBean.setControlState(mainWindow.getControlState());

        Position machineCoord = mainWindow.getMachinePosition();
        if (machineCoord != null) {
            systemStateBean.setMachineX(Utils.formatter.format(machineCoord.x));
            systemStateBean.setMachineY(Utils.formatter.format(machineCoord.y));
            systemStateBean.setMachineZ(Utils.formatter.format(machineCoord.z));
        }

        IController controller = mainWindow.getController();
        if (controller != null) {
            systemStateBean.setActiveState(mainWindow.getController().getControllerStatus().getStateString());
            systemStateBean.setRemainingRows(String.valueOf(mainWindow.getNumRemainingRows()));
            systemStateBean.setRowsInFile(String.valueOf(mainWindow.getNumRows()));
            systemStateBean.setSentRows(String.valueOf(mainWindow.getNumSentRows()));
            systemStateBean.setDuration(String.valueOf(mainWindow.getSendDuration()));
            systemStateBean.setEstimatedTimeRemaining(String.valueOf(mainWindow.getSendRemainingDuration()));
        }

        Position workCoord = mainWindow.getWorkPosition();
        if (workCoord != null) {
            systemStateBean.setWorkX(Utils.formatter.format(workCoord.x));
            systemStateBean.setWorkY(Utils.formatter.format(workCoord.y));
            systemStateBean.setWorkZ(Utils.formatter.format(workCoord.z));
        }
        systemStateBean.setSendButtonEnabled(mainWindow.canSend());
        systemStateBean.setPauseResumeButtonEnabled(mainWindow.canPause());
        systemStateBean.setCancelButtonEnabled(mainWindow.canCancel());
    }

    public String getSystemStateJson(){
        return new Gson().toJson(systemState);
    }
    
    
    public class ConfigHandler extends AbstractHandler{
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            response.setContentType("application/json");
            // TODO handle configuration
            response.getWriter().print(new Gson().toJson(new PendantConfigBean()));
        }
    }

    public class AdjustManualLocationHandler extends AbstractHandler{
        @Override
        public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            
            if(isManualControlEnabled()){
                int dirX = parseInt(baseRequest.getParameter("dirX"));
                int dirY = parseInt(baseRequest.getParameter("dirY"));
                int dirZ = parseInt(baseRequest.getParameter("dirZ"));
                double stepSize = parseDouble(baseRequest.getParameter("stepSize"));

                try {
                    mainWindow.adjustManualLocation(dirX, dirY, dirZ, stepSize, 1, Units.UNKNOWN);
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
            }

            response.getWriter().print(systemState.getControlState().name());
        }
    }
    
    public int parseInt(String string){
        int out = 0;
        
        try {
            out = Integer.parseInt(string);
        } catch (Exception e) {
            // nothing to do
        }
        return out;
    }
    
    public double parseDouble(String string){
        double out = 0.0;
        try {
            out = Double.parseDouble(string);
        } catch (Exception e) {
            // nothing to do
        }
        return out;
    }
    
    

    public void stop(){
        try {
            if(server!=null){
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

    public boolean isManualControlEnabled() {
        switch (systemState.getControlState()) {
        case COMM_DISCONNECTED:
            return false;
        case COMM_IDLE:
            return true;
        case COMM_SENDING:
            return false;
        default:
            return true;
        }
    }

    public BackendAPI getMainWindow() {
        return mainWindow;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setMainWindow(BackendAPI mainWindow) {
        this.mainWindow = mainWindow;
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
    }

    @Override
    public void commandComment(String comment) {
    }

    @Override
    public void probeCoordinates(Position p) {
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        // TODO Auto-generated method stub
        
    }

    public SystemStateBean getSystemState() {
        return systemState;
    }

    public void setSystemState(SystemStateBean systemState) {
        this.systemState = systemState;
    }
}
