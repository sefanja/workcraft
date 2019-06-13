package org.workcraft.plugins.nta.interop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.nta.TestUtils;
import org.workcraft.plugins.nta.VisualNta;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class UppaalExportImportTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void test2doorsExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "2doors.work");
        testExportImport(workName);
    }

    @Test
    public void testBridgeExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bridge.work");
        testExportImport(workName);
    }

    @Test
    public void testFischerExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "fischer.work");
        testExportImport(workName);
    }

    @Test
    public void testFischerSymmetryExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "fischer_symmetry.work");
        testExportImport(workName);
    }

    @Test
    public void testInterruptExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "interrupt.work");
        testExportImport(workName);
    }

    @Test
    public void testTrainGateExportImport()
            throws FileNotFoundException, DeserialisationException, SerialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "train-gate.work");
        testExportImport(workName);
    }

    /**
     * Performs a chained export and import, and compares the resulting model to the original.
     */
    private void testExportImport(String workName)
            throws FileNotFoundException, DeserialisationException, SerialisationException {

        // load original model
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        ModelEntry me = we.getModelEntry();
        VisualNta original = WorkspaceUtils.getAs(we, VisualNta.class);

        // export to UPPAAL file
        File directory = FileUtils.createTempDirectory("workcraft-" + workName);
        File uppaalFile = new File(directory, "export.xml");
        framework.exportModel(me, uppaalFile, UppaalFormat.getInstance());

        // import exported UPPAAL file
        final InputStream uppaalStream = new FileInputStream(uppaalFile);
        VisualNta importedExport = new UppaalImporter().importUppaalSystem(uppaalStream);

        // compare chained export and import to original model
        TestUtils.assertNtaEquals(original.getReferencedNta(), importedExport.getReferencedNta());

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(directory);
    }

}
