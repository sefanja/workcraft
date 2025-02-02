package org.workcraft.plugins.stg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.commands.MergePlaceTransformationCommand;
import org.workcraft.plugins.stg.commands.*;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

public class TransformationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCelementMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    @Test
    public void testBuckMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    @Test
    public void testVmeMirrorSignalTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testMirrorSignalTransformationCommand(workName);
    }

    private void testMirrorSignalTransformationCommand(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> srcInputs = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcInternals = stg.getSignalReferences(Signal.Type.INTERNAL);

        MirrorSignalTransformationCommand command = new MirrorSignalTransformationCommand();
        command.execute(we);
        Set<String> dstInputs = stg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstOutputs = stg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstInternals = stg.getSignalReferences(Signal.Type.INTERNAL);

        framework.closeWork(we);
        Assert.assertEquals(srcInputs, dstOutputs);
        Assert.assertEquals(srcOutputs, dstInputs);
        Assert.assertEquals(srcInternals, dstInternals);
    }

    @Test
    public void testCelementMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    @Test
    public void testBuckMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    @Test
    public void testVmeMirrorTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testMirrorTransitionTransformationCommand(workName);
    }

    private void testMirrorTransitionTransformationCommand(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        int srcMinusCount = 0;
        int srcPlusCount = 0;
        int srcToggleCount = 0;
        for (SignalTransition srcTransition : stg.getSignalTransitions()) {
            switch (srcTransition.getDirection()) {
            case MINUS:
                srcMinusCount++;
                break;
            case PLUS:
                srcPlusCount++;
                break;
            case TOGGLE:
                srcToggleCount++;
                break;
            }
        }

        MirrorTransitionTransformationCommand command = new MirrorTransitionTransformationCommand();
        command.execute(we);
        int dstMinusCount = 0;
        int dstPlusCount = 0;
        int dstToggleCount = 0;
        for (SignalTransition dstTransition : stg.getSignalTransitions()) {
            switch (dstTransition.getDirection()) {
            case MINUS:
                dstMinusCount++;
                break;
            case PLUS:
                dstPlusCount++;
                break;
            case TOGGLE:
                dstToggleCount++;
                break;
            }
        }

        framework.closeWork(we);
        Assert.assertEquals(srcMinusCount, dstPlusCount);
        Assert.assertEquals(srcPlusCount, dstMinusCount);
        Assert.assertEquals(srcToggleCount, dstToggleCount);
    }

    @Test
    public void testCelementPlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testPlaceTransformationCommands(workName);
    }

    @Test
    public void testBuckPlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testPlaceTransformationCommands(workName);
    }

    @Test
    public void testVmePlaceTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testPlaceTransformationCommands(workName);
    }

    private void testPlaceTransformationCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        int srcPlaces = stg.getVisualPlaces().size();
        int srcImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitions = stg.getVisualSignalTransitions().size();
        int srcDummyTransitions = stg.getVisualDummyTransitions().size();

        ExplicitPlaceTransformationCommand command1 = new ExplicitPlaceTransformationCommand();
        command1.execute(we);
        int expPlaces = stg.getVisualPlaces().size();
        int expImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int expSignalTransitions = stg.getVisualSignalTransitions().size();
        int expDummyTransitions = stg.getVisualDummyTransitions().size();

        Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, expPlaces + expImplicitPlaceArcs);
        Assert.assertEquals(srcSignalTransitions, expSignalTransitions);
        Assert.assertEquals(srcDummyTransitions, expDummyTransitions);

        ImplicitPlaceTransformationCommand command2 = new ImplicitPlaceTransformationCommand();
        command2.execute(we);
        int impPlaces = stg.getVisualPlaces().size();
        int impImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int impSignalTransitions = stg.getVisualSignalTransitions().size();
        int impDummyTransitions = stg.getVisualDummyTransitions().size();

        framework.closeWork(we);
        Assert.assertEquals(srcPlaces + srcImplicitPlaceArcs, impPlaces + impImplicitPlaceArcs);
        Assert.assertEquals(srcSignalTransitions, impSignalTransitions);
        Assert.assertEquals(srcDummyTransitions, impDummyTransitions);
    }

    @Test
    public void testHandshakes2ExpandHandshakeTransformationCommand() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "handshakes-2.stg.work");
        testExpandHandshakeTransformationCommand(workName);
    }

    @Test
    public void testHandshakes3ExpandHandshakeTransformationCommand() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "handshakes-3.stg.work");
        testExpandHandshakeTransformationCommand(workName);
    }

    private void testExpandHandshakeTransformationCommand(String workName) throws IOException, DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        int srcPlaces = stg.getVisualPlaces().size();
        int srcImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int srcSignalTransitions = stg.getVisualSignalTransitions().size();
        int srcDummyTransitions = stg.getVisualDummyTransitions().size();
        int srcConnections = stg.getVisualConnections().size();

        stg.selectAll();
        ExpandHandshakeReqAckTransformationCommand command = new ExpandHandshakeReqAckTransformationCommand();
        command.execute(we);
        int dstPlaces = stg.getVisualPlaces().size();
        int dstImplicitPlaceArcs = stg.getVisualImplicitPlaceArcs().size();
        int dstSignalTransitions = stg.getVisualSignalTransitions().size();
        int dstDummyTransitions = stg.getVisualDummyTransitions().size();
        int dstConnections = stg.getVisualConnections().size();

        framework.closeWork(we);
        Assert.assertEquals(srcPlaces, dstPlaces);
        Assert.assertEquals(srcSignalTransitions * 2, dstSignalTransitions);
        Assert.assertEquals(srcDummyTransitions, dstDummyTransitions);
        Assert.assertEquals(srcImplicitPlaceArcs + srcSignalTransitions, dstImplicitPlaceArcs);
        Assert.assertEquals(srcConnections + srcSignalTransitions, dstConnections);
    }

    @Test
    public void testVmeSelectAllSignalTransitionsTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testSelectAllSignalTransitionsTransformationCommand(workName, new String[]{"dsr+", "dtack+/1"}, 5);
    }

    private void testSelectAllSignalTransitionsTransformationCommand(String workName, String[] refs, int expCount) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
        selectVisualComponentsByMathRefs(stg, refs);

        SelectAllSignalTransitionsTransformationCommand command = new SelectAllSignalTransitionsTransformationCommand();
        command.execute(we);
        int count = stg.getSelection().size();

        framework.closeWork(we);
        Assert.assertEquals(expCount, count);
    }

    @Test
    public void testVmeSignalToDummyTransitionTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testSignalToDummyTransitionTransformationCommand(workName, new String[]{"dsw+", "dtack+/1"});
    }

    private void testSignalToDummyTransitionTransformationCommand(String workName, String[] refs) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);

        int srcSignalTransitionCount = stg.getVisualSignalTransitions().size();
        int srcDummyTransitionCount = stg.getVisualDummyTransitions().size();

        selectVisualComponentsByMathRefs(stg, refs);

        int selectionCount = stg.getSelection().size();

        SignalToDummyTransitionTransformationCommand command = new SignalToDummyTransitionTransformationCommand();
        command.execute(we);

        int dstSignalTransitionCount = stg.getVisualSignalTransitions().size();
        int dstDummyTransitionCount = stg.getVisualDummyTransitions().size();

        framework.closeWork(we);
        Assert.assertEquals(srcDummyTransitionCount + selectionCount, dstDummyTransitionCount);
        Assert.assertEquals(srcSignalTransitionCount - selectionCount, dstSignalTransitionCount);
    }

    private void selectVisualComponentsByMathRefs(VisualStg stg, String[] refs) {
        stg.selectNone();
        for (String ref : refs) {
            VisualComponent t = stg.getVisualComponentByMathReference(ref, VisualComponent.class);
            if (t != null) {
                stg.addToSelection(t);
            }
        }
    }

    @Test
    public void testTransitionTransformationCommand() throws DeserialisationException, InvalidConnectionException {
        String workName = PackageUtils.getPackagePath(getClass(), "inv.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);

        Assert.assertEquals(1, stg.getVisualPlaces().size());
        Assert.assertEquals(4, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(0, stg.getVisualDummyTransitions().size());

        VisualSignalTransition outPlus = stg.getVisualComponentByMathReference("out+", VisualSignalTransition.class);
        Assert.assertNotNull(outPlus);

        VisualStgPlace p1 = stg.createVisualPlace("p1");
        p1.getReferencedComponent().setTokens(1);

        VisualSignalTransition intToggle = stg.createVisualSignalTransition("int", Signal.Type.INTERNAL, SignalTransition.Direction.TOGGLE);

        VisualSignalTransition inPlus = stg.getVisualComponentByMathReference("in+", VisualSignalTransition.class);
        Assert.assertNotNull(inPlus);

        VisualStgPlace p0 = stg.getVisualComponentByMathReference("p0", VisualStgPlace.class);
        Assert.assertNotNull(p0);

        VisualConnection connection = stg.getConnection(p0, inPlus);
        Assert.assertNotNull(connection);

        stg.connect(outPlus, p1);
        stg.connect(p1, intToggle);
        stg.connect(intToggle, inPlus);

        InsertDummyTransformationCommand insertDummyCommand = new InsertDummyTransformationCommand();
        stg.select(connection);
        insertDummyCommand.execute(we);

        Assert.assertEquals(2, stg.getVisualPlaces().size());
        Assert.assertEquals(5, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(1, stg.getVisualDummyTransitions().size());

        DummyToSignalTransitionTransformationCommand dummyToSignalTransitionCommand = new DummyToSignalTransitionTransformationCommand();
        VisualDummyTransition dummy = stg.getVisualComponentByMathReference("dum0", VisualDummyTransition.class);
        Assert.assertNotNull(dummy);
        stg.select(dummy);
        dummyToSignalTransitionCommand.execute(we);

        Assert.assertEquals(2, stg.getVisualPlaces().size());
        Assert.assertEquals(6, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(0, stg.getVisualDummyTransitions().size());

        MergePlaceTransformationCommand mergePlaceCommand = new MergePlaceTransformationCommand();
        stg.select(Arrays.asList(p0, p1));
        mergePlaceCommand.execute(we);

        Assert.assertEquals(1, stg.getVisualPlaces().size());
        Assert.assertEquals(6, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(0, stg.getVisualDummyTransitions().size());

        MergeTransitionTransformationCommand mergeTransitionCommand = new MergeTransitionTransformationCommand();
        VisualSignalTransition sigToggle = stg.getVisualComponentByMathReference("sig~", VisualSignalTransition.class);
        Assert.assertNotNull(sigToggle);
        stg.select(Arrays.asList(intToggle, sigToggle));
        mergeTransitionCommand.execute(we);

        Assert.assertEquals(1, stg.getVisualPlaces().size());
        Assert.assertEquals(5, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(0, stg.getVisualDummyTransitions().size());

        ContractNamedTransitionTransformationCommand contractTransitionCommand = new ContractNamedTransitionTransformationCommand();
        VisualSignalTransition intsigToggle = stg.getVisualComponentByMathReference("int_sig~", VisualSignalTransition.class);
        if (intsigToggle == null) {
            intsigToggle = stg.getVisualComponentByMathReference("sig_int~", VisualSignalTransition.class);
        }
        Assert.assertNotNull(intsigToggle);
        stg.select(intsigToggle);
        contractTransitionCommand.execute(we);

        Assert.assertEquals(1, stg.getVisualPlaces().size());
        Assert.assertEquals(4, stg.getVisualSignalTransitions().size());
        Assert.assertEquals(0, stg.getVisualDummyTransitions().size());
    }

}
