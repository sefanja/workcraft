package org.workcraft.plugins.nta.interop;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class TimeSolverExportTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testBouyerExport() throws IOException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bouyer.work");
        String tsName = PackageUtils.getPackagePath(getClass(), "bouyer.in");
        testExport(workName, tsName);
    }

    private void testExport(String workName, String tsName)
            throws IOException, DeserialisationException, SerialisationException {

        // open Workcraft file
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        ModelEntry me = we.getModelEntry();

        // export to TimeSolver file
        File directory = FileUtils.createTempDirectory("workcraft-" + workName);
        File tsFile = new File(directory, "export.in");
        framework.exportModel(me, tsFile, TimesolverFormat.getInstance());

        // open expected and actual exported file
        final BufferedReader actual = new BufferedReader(new FileReader(tsFile));
        final BufferedReader expected = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(tsName))));

        // count lines (can be more sophisticated of the implementation of a TimeSolver parser)
        int actualLines = 0;
        while (actual.readLine() != null) actualLines++;
        int expectedLines = 0;
        while (expected.readLine() != null) expectedLines++;

        // compare line count
        Assert.assertEquals(expectedLines, actualLines);

        actual.close();
        expected.close();
        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(directory);
    }
}
