package org.workcraft.plugins.mpsat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.plugins.mpsat.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.PackageUtils;

public class SynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PunfSettings.setCommand("dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("dist-template/linux/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("dist-template/linux/libraries/workcraft.lib");
            break;
        case MACOS:
            PunfSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("dist-template/osx/Contents/Resources/libraries/workcraft.lib");
            break;
        case WINDOWS:
            PunfSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSynthesisSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            CircuitSettings.setGateLibrary("dist-template\\windows\\libraries\\workcraft.lib");
            break;
        default:
        }
    }

    @Test
    public void bufferComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void constComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testComplexGateSynthesisCommand(workName, 3);
    }

    @Test
    public void edcComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testComplexGateSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 4);
    }

    private void testComplexGateSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.testSynthesisCommand(ComplexGateSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void edcGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 4);
    }

    private void testGeneralisedCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.testSynthesisCommand(GeneralisedCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void edcStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void duplicatorCscHierStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 11);
    }

    private void testStandardCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.testSynthesisCommand(StandardCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bufferTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    public void celementTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    public void constTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 5);
    }

    @Test
    public void edcTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 11);
    }

    private void testTechnologyMappingSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.testSynthesisCommand(TechnologyMappingSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
