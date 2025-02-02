package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Iterator;

public class CycleCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCycleTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testLoopbreakerCommands(workName, 0, true);
    }

    @Test
    public void testChargeTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testLoopbreakerCommands(workName, 3, true);
    }

    private void testLoopbreakerCommands(String workName, int breakCount, boolean pass)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        new PathBreakerClearAllTagCommand().execute(we);
        Assert.assertEquals(0, countPathBreaker(circuit));

        new PathBreakerAutoAppendTagCommand().execute(we);
        int count = countPathBreaker(circuit);
        Assert.assertEquals(breakCount, count);

        if (count > 0) {
            new TbufInsertionCommand().execute(we);
            Assert.assertEquals(breakCount, countPathBreaker(circuit));

            new ScanInsertionCommand().execute(we);

            Iterator<String> portNameIterator = CircuitSettings.parseScanPorts().iterator();
            Iterator<String> pinNameIterator = CircuitSettings.parseScanPins().iterator();

            while (portNameIterator.hasNext() || pinNameIterator.hasNext()) {
                String portName = portNameIterator.next();
                MathNode port = circuit.getNodeByReference(portName);
                Assert.assertTrue(port instanceof FunctionContact);

                String pinName = pinNameIterator.next();
                for (FunctionComponent component : circuit.getFunctionComponents()) {
                    if (ScanUtils.hasPathBreakerOutput(component)) {
                        MathNode pin = circuit.getNodeByReference(component, pinName);
                        Assert.assertTrue(pin instanceof FunctionContact);
                        Assert.assertEquals(port, CircuitUtils.findDriver(circuit, pin, false));
                    }
                }
            }
        }

        Assert.assertEquals(pass, new CycleFreenessVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private int countPathBreaker(Circuit circuit) {
        int result = 0;
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (ScanUtils.hasPathBreakerOutput(component)) {
                result++;
            }
            for (Contact contact : component.getInputs()) {
                if (contact.getPathBreaker()) {
                    result++;
                }
            }
        }
        return result;
    }

}
