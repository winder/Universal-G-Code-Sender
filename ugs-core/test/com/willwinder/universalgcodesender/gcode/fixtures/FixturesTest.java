package com.willwinder.universalgcodesender.gcode.fixtures;

import com.google.common.base.Joiner;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.*;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;

/**
 * A simple framework where you can specify one or more gcode files in the /gcode/fixtures/ test-resources folder
 * which gets run through various GcodeParsers and its output is recorded in a fixture and gets monitored for changes
 *
 * The output is not supposed to stay static, but it should help you to point out maybe unwanted behaviour changes
 * of the parser. And also lets you easily generate and validate parsed and processed gcode against other viewers.
 *
 *  - Add more of the test{...}Fixtures runners with different configured parsers and test folders
 *  - Run the test once to generate out{...}/ files and commit them to the repo
 */
public class FixturesTest {
    @Test
    public void testArcFixtures() throws Exception {
        runAllFixturesInPath("./gcode/fixtures/arc/", "Arc", () -> {
            GcodeParser gcp = new GcodeParser();
            gcp.addCommandProcessor(new CommentProcessor());
            gcp.addCommandProcessor(new ArcExpander(true, 0.1));
            gcp.addCommandProcessor(new LineSplitter(1));
            return gcp;
        });
    }

    @Test
    public void testArcFixturesCoarse() throws Exception {
        runAllFixturesInPath("./gcode/fixtures/arc/", "ArcCoarse", () -> {
            GcodeParser gcp = new GcodeParser();
            gcp.addCommandProcessor(new CommentProcessor());
            gcp.addCommandProcessor(new ArcExpander(true, 1));
            return gcp;
        });
    }

    @Test
    public void testArcWithMeshLevelerFixtures() throws Exception {
        runAllFixturesInPath("./gcode/fixtures/arc/", "ArcMesh", ()->{
            GcodeParser gcp = new GcodeParser();

            gcp.addCommandProcessor(new CommentProcessor());
            gcp.addCommandProcessor(new ArcExpander(true, 0.1));
            gcp.addCommandProcessor(new LineSplitter(1));
            Position grid[][] = {
                    { new Position(-5,-5,0, MM), new Position(-5,35,0, MM) },
                    { new Position(35,-5,0, MM), new Position(35,35,0, MM) }
            };
            gcp.addCommandProcessor(new MeshLeveler(0, grid, UnitUtils.Units.MM));
            return gcp;
        });

    }

    @Test
    public void testRunFromFixtures() throws Exception {
        runAllFixturesInPath("./gcode/fixtures/run-from/", "RunFrom", () -> {
            GcodeParser gcp = new GcodeParser();
            gcp.addCommandProcessor(new RunFromProcessor(17));
            return gcp;
        });
    }

    private void runAllFixturesInPath(String basePath, String parserName, Callable<GcodeParser> initGcp) throws Exception {
        List<String> resourceFiles = getResourceFiles(basePath);
        System.out.printf("Running all fixtures in %s for %s:%n", basePath, parserName);
        for (String file : resourceFiles) {
            if (file.endsWith(".input.nc")){
                String baseName = file.replace(".input.nc", "");
                GcodeParser gcp = initGcp.call();
                runFixture(basePath, baseName, parserName, gcp);
            }
        }
    }



    public void runFixture(String basePath, String fixtureName, String parserName, GcodeParser gcp) throws Exception {
        String inputFixture = basePath + fixtureName + ".input.nc";
        String streamOutputFixture = basePath + "out" + parserName + "/" + fixtureName + ".stream_output.nc";
        String outputxFixture = basePath + "out" + parserName + "/" + fixtureName  + ".parsed_output.nc";
        System.out.printf("Running fixture %s...%n", inputFixture);

        // write parsed output to temp file
        Path output = Files.createTempFile(fixtureName, "output");

        // gcp needs its input from a normal file; copy from resource to a temp file
        URL file = this.getClass().getClassLoader().getResource(inputFixture);
        File tempFile = File.createTempFile(fixtureName, "input");
        IOUtils.copy(file.openStream(), FileUtils.openOutputStream(tempFile));

        // process the input file and write it to the output temp file
        try (IGcodeWriter gcw = new GcodeStreamWriter(output.toFile())) {
            GcodeParserUtils.processAndExport(gcp, tempFile, gcw);
        }

        // read the output back in and compare it to the fixture
        //GcodeStreamReader reader = new GcodeStreamReader(output.toFile());
        URI outputTest = output.toUri();

        // compare the generated raw stream output
        Iterator<String> testLines = Files.lines(Paths.get(outputTest)).iterator();

        // check the generated stream
        checkOrInitializeFixture(fixtureName + "-stream", streamOutputFixture, testLines);

        // compare the generated output, parsed
        IGcodeStreamReader reader = new GcodeStreamReader(output.toFile());

        // also verify the parsed representation of the GCode against a separate fixture
        List<String> gcode = new ArrayList<>();

        GcodeCommand c;
        while ((c = reader.getNextCommand()) != null) {
            gcode.add(c.getCommandString().trim());
        }
        checkOrInitializeFixture(fixtureName + "-gcode", outputxFixture, gcode.iterator());

    }

    private void checkOrInitializeFixture(String name, String fixtureResourceName, Iterator<String> testLines) throws URISyntaxException, IOException {
        // check if the fixture already exists
        URL fixtureUri = this.getClass().getClassLoader().getResource(fixtureResourceName);
        if (fixtureUri != null) {
            checkFixture(fixtureResourceName, testLines);
        } else {
            initializeFixture(name, fixtureResourceName, testLines);
        }

    }

    private void checkFixture(String fixtureResourceName, Iterator<String> testLines) throws URISyntaxException, IOException {
        URI fixtureUri = this.getClass().getClassLoader().getResource(fixtureResourceName).toURI();
        Iterator<String> fixtureLines = Files.lines(Paths.get(fixtureUri)).iterator();

        int n = 1;
        while (fixtureLines.hasNext()) {
            if (!testLines.hasNext()) {
                Assert.fail("Fixture for " + fixtureResourceName + " has more lines than generated gcode:\n  " + Joiner.on("\n  ").join(fixtureLines));
            }
            String testLine = testLines.next().trim();
            String refStreamLine = fixtureLines.next().trim();
            Assert.assertEquals("Line " + n + " of fixture " + fixtureResourceName + " differs from generated gcode \n", refStreamLine, testLine);
            n++;
        }
        if (testLines.hasNext()) {
            Assert.fail("Generated gcode for " + fixtureResourceName + " has more lines than the fixture:\n  " + Joiner.on("\n  ").join(testLines));
        }


    }

    // just a quick hack to initialize not existing fixtures
    // maybe that would be better suited as a maven target?
    private void initializeFixture(String basePath, String fixtureResourceName, Iterator<String> testLines) throws IOException {
        String dstFile = Paths.get(".").toAbsolutePath().normalize().toString() + "/test/resources/" + fixtureResourceName;
        Files.createDirectories(Paths.get(dstFile).getParent());

        System.out.println("-------------------------------------------------");
        System.out.println("| WARNING:  " + fixtureResourceName + " does not exist, it will be created");
        System.out.println("|  This should only happen if you made a new fixture or intentionally ");
        System.out.println("|  the old one to update it.");
        System.out.println("|  It will be written to:");
        System.out.println("|    " + dstFile);
        System.out.println("|    (if this is not correct, move it into the resource folder)");
        System.out.println("-------------------------------------------------");

        PrintStream resourceFile = new PrintStream(dstFile);
        testLines.forEachRemaining(resourceFile::println);
        resourceFile.close();
    }


    // some helper functions to iterate over resource files
    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}

