package org.workcraft.plugins.nta.commands;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.nta.Nta;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.HashMap;

public class ParallelCompositionConversionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testBouyerConversion() throws DeserialisationException {
        String srcWorkName = PackageUtils.getPackagePath(getClass(), "bouyer.src.work");
        String expectedWorkName = PackageUtils.getPackagePath(getClass(), "bouyer.expected-composition.work");
        testConversion(srcWorkName, expectedWorkName);
    }

    private void testConversion(String srcWorkName, String expectedWorkName) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // load srcModel
        URL srcUrl = classLoader.getResource(srcWorkName);
        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Nta srcModel = WorkspaceUtils.getAs(srcWe, Nta.class);

        // load expectedModel
        URL expectedUrl = classLoader.getResource(expectedWorkName);
        WorkspaceEntry expectedWe = framework.loadWork(expectedUrl.getFile());
        Nta expectedModel = WorkspaceUtils.getAs(expectedWe, Nta.class);

        // convert srcModel to actualModel
        ParallelCompositionConversionCommand command = new ParallelCompositionConversionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Nta actualModel = WorkspaceUtils.getAs(dstWe, Nta.class);

        // compare actualModel to expectedModel
        Assert.assertEquals(countNodesByClass(expectedModel), countNodesByClass(actualModel));
    }

    private HashMap<Class, Integer> countNodesByClass(Nta nta) {
        final HashMap<Class, Integer> counter = new HashMap<>();
        for (Node node : Hierarchy.getDescendants(nta.getRoot())) {
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
