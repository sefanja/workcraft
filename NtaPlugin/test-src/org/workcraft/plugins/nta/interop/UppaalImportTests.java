package org.workcraft.plugins.nta.interop;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.nta.VisualNta;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class UppaalImportTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void test2doorsImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "2doors.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "2doors.xml");
        testImport(workName, uppaalName);
    }

    @Test
    public void testBridgeImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bridge.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "bridge.xml");
        testImport(workName, uppaalName);
    }

    @Test
    public void testFischerImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "fischer.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "fischer.xml");
        testImport(workName, uppaalName);
    }

    @Test
    public void testFischerSymmetryImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "fischer_symmetry.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "fischer_symmetry.xml");
        testImport(workName, uppaalName);
    }

    @Test
    public void testInterruptImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "interrupt.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "interrupt.xml");
        testImport(workName, uppaalName);
    }

    @Test
    public void testTrainGateImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "train-gate.work");
        String uppaalName = PackageUtils.getPackagePath(getClass(), "train-gate.xml");
        testImport(workName, uppaalName);
    }

    private void testImport(String workName, String uppaalName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // import UPPAAL file
        final InputStream test = classLoader.getResourceAsStream(uppaalName);
        VisualNta imported = new UppaalImporter().importUppaalSystem(test);

        // load Work file
        URL url = classLoader.getResource(workName);
        String filePath = url.getFile();
        WorkspaceEntry we = framework.loadWork(filePath);
        VisualNta loaded = WorkspaceUtils.getAs(we, VisualNta.class);
        framework.closeWork(we);

        // compare UPPAAL to Work
        Assert.assertEquals(countNodesByClass(loaded), countNodesByClass(imported));
    }

    private HashMap<Class, Integer> countNodesByClass(VisualNta vNta) {
        final HashMap<Class, Integer> counter = new HashMap<>();
        for (Node node : Hierarchy.getDescendants(vNta.getRoot())) {
            Class c = node.getClass();
            if (!counter.containsKey(c)) {
                counter.put(c, 1);
            } else {
                counter.put(c, counter.get(c) + 1);
            }
        }
        return counter;
    }

}
