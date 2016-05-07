package com.willwinder.universalgcodesender;

import org.apache.commons.lang3.StringUtils;
import com.willwinder.universalgcodesender.types.Simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 2/17/14
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class GrblSimulator extends Simulator {

    public GrblSimulator() {
    }

    public GrblSimulator(Collection<String> configurationCommands) {
        super(configurationCommands);
    }

    protected Process getSimulatorProcess() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sim.exe", "1");
        return pb.start();
    }

    protected void sendCommands(Collection<String> commands, BufferedWriter simInputBuffer) throws IOException{
        for (String command : commands) {
            simInputBuffer.write(command);
            simInputBuffer.newLine();
            simInputBuffer.flush();
        }
    }

    protected List<String> runSimulation(Collection<String> commands) throws IOException {
        BufferedWriter simInputBuffer = null;
        ProcessBuffer simOutputBuffer = null;
        Thread parserThread = null;

        try {
            Process simulation = getSimulatorProcess();
            simInputBuffer = new BufferedWriter(new OutputStreamWriter(simulation.getOutputStream()));
            simOutputBuffer = new ProcessBuffer(simulation);
            parserThread = new Thread(simOutputBuffer);
            parserThread.start();

            while (!simOutputBuffer.isReady()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ioe) {
                    parserThread.interrupt();
                    return null;
                }
            }
            sendCommands(configStrings, simInputBuffer);
            sendCommands(commands, simInputBuffer);
            simInputBuffer.close();

            try {
                simulation.waitFor();
                parserThread.join();
            } catch (InterruptedException ie) {
                return null;
            }
        } finally {
            if (simInputBuffer != null)
                simInputBuffer.close();
            if (parserThread.isAlive())
                parserThread.interrupt();
        }

        return simOutputBuffer.getErrLines();
    }

    protected long parseResults(List<String> simOutput) {
        long result = 0;

        for (int i = simOutput.size() - 1; i >= 0; i--) {
            String line = simOutput.get(i).trim();
            if (line.startsWith("#"))
                continue;
            int ci = line.indexOf(',');
            if (ci > 0) {
                String secondsString = line.substring(0, ci);
                Double seconds;
                try {
                    seconds = Double.parseDouble(secondsString);
                } catch (Exception e) {
                    continue;
                }
                result = StrictMath.round(seconds * 1000);
                break;
            }
        }

        return result;
    }

    @Override
    public long estimateRunLength(Collection<String> commands) {
        long result = 0;
        List<String> buffer = null;
        try {
             buffer = runSimulation(commands);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (buffer != null) {
            result = parseResults(buffer);
        }

        return result;
    }

    public class ProcessBuffer implements Runnable {
        protected Process process;
        protected List<String> stderrList;
        protected List<String> stdoutList;
        protected List<String> outList;
        protected boolean ready;

        public boolean isReady() {
            return ready;
        }

        public ProcessBuffer(Process process) {
            this.process = process;
        }

        public List<String> getErrLines() {
            return getLines(stderrList);
        }

        public List<String> getStdLines() {
            return getLines(stdoutList);
        }

        public List<String> getAllLines() {
            return getLines(outList);
        }

        private List<String> getLines(List<String> list) {
            if (list == null)
                return null;

            int size = list.size();

            List<String> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(list.get(i));
            }

            return result;
        }

        @Override
        public void run() {
            outList = new ArrayList<>();
            List<String> syncList = Collections.synchronizedList(outList);
            stderrList = new ArrayList<>();
            stdoutList = new ArrayList<>();
            Thread errReader = new Thread(new Buffer(process.getErrorStream(), stderrList, syncList));
            Thread stdReader = new Thread(new Buffer(process.getInputStream(), stdoutList, syncList));

            stdReader.start();
            errReader.start();

            while (stdoutList.size() < 2  || ! StringUtils.join(getStdLines(), "").contains(" for help]")) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ioe) {
                    process.destroy();
                    return;
                }
            }

            ready = true;

            try {
                stdReader.join();
                errReader.join();
            } catch (InterruptedException ie) {
                process.destroy();
                return;
            }
        }
    }

    public class Buffer implements Runnable {

        private final BufferedReader reader;
        List<String> buffer;
        List<String> globalBuffer;

        public Buffer(InputStream in, List<String> buffer, List<String> globalBuffer) {
            reader = new BufferedReader(new InputStreamReader(in));
            this.buffer = buffer;
            this.globalBuffer = globalBuffer;
        }

        @Override
        public void run() {
            buffer.clear();
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    buffer.add(line);
                    globalBuffer.add(line);
                }
            } catch (IOException ioe) {
                return;
            }
        }
    }
}
