package org.workcraft.plugins.nta;

import org.junit.Assert;
import org.workcraft.dom.Node;
import org.workcraft.utils.Hierarchy;

import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {

    public static void assertNtaEquals(Nta expectedModel, Nta actualModel) {
        assertNodeCountByTypeEquals(expectedModel, actualModel);

        for (Template expectedTemplate : expectedModel.getTemplates()) {
            String nodeReference = expectedModel.getNodeReference(expectedTemplate);
            Template actualTemplate =
                    (Template) actualModel.getNodeByReference(nodeReference);
            assertPropertiesEqual(expectedTemplate, actualTemplate);
        }

        for (Location expectedLocation : expectedModel.getAllLocations()) {
            String nodeReference = expectedModel.getNodeReference(expectedLocation);
            Location actualLocation =
                    (Location) actualModel.getNodeByReference(nodeReference);
            assertPropertiesEqual(expectedLocation, actualLocation);
        }

        for (Transition expectedTransition : expectedModel.getAllTransitions()) {

            // find transition in actual model that corresponds to transition in expected model
            Collection<Transition> actualTansitions = actualModel.getAllTransitions();
            actualTansitions.removeIf(actualTransition -> !Objects.equals(
                    expectedModel.getNodeReference(expectedTransition.getFirst()),
                    actualModel.getNodeReference(actualTransition.getFirst()))
                    || !Objects.equals(
                    expectedModel.getNodeReference(expectedTransition.getSecond()),
                    actualModel.getNodeReference(actualTransition.getSecond())));

            // if multiple transitions with the same direction between two locations exist, narrow down the search
            if (actualTansitions.size() > 1) {
                actualTansitions.removeIf(actualTansition -> !Objects.equals(
                        Optional.ofNullable(expectedTransition.getGuard()).orElse(""),
                        Optional.ofNullable(actualTansition.getGuard()).orElse(""))
                        || !Objects.equals(
                        Optional.ofNullable(expectedTransition.getSynchronisation()).orElse(""),
                        Optional.ofNullable(actualTansition.getSynchronisation()).orElse("")));
            }

            Transition actualTransition = actualTansitions.iterator().next();

            assertPropertiesEqual(expectedTransition, actualTransition);
        }
    }

    private static void assertNodeCountByTypeEquals(Nta expectedModel, Nta actualModel) {
        Assert.assertEquals(countNodesByType(expectedModel), countNodesByType(actualModel));
    }

    private static HashMap<Class, Integer> countNodesByType(Nta nta) {
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

    private static void assertPropertiesEqual(Template expectedTemplate, Template actualTemplate) {
        Assert.assertEquals(expectedTemplate.getInstanceCount(), actualTemplate.getInstanceCount());
        assertStringEquals(expectedTemplate.getParameters(), actualTemplate.getParameters());
    }

    private static void assertPropertiesEqual(Location expectedLocation, Location actualLocation) {
        Assert.assertNotNull(actualLocation);
        assertStringEquals(expectedLocation.getComments(), actualLocation.getComments());
        assertInvariantsEquals(expectedLocation.getInvariant(), actualLocation.getInvariant());
        Assert.assertEquals(expectedLocation.isCommitted(), actualLocation.isCommitted());
        Assert.assertEquals(expectedLocation.isInitial(), actualLocation.isInitial());
        Assert.assertEquals(expectedLocation.isUrgent(), actualLocation.isUrgent());
    }

    private static void assertInvariantsEquals(String expectedInvariant, String actualInvariant) {
        if (expectedInvariant.contains("&&")) {
            String[] expectedSplit = expectedInvariant.split("&&");
            Set<String> expectedSet = Arrays.stream(expectedSplit).map(String::trim).collect(Collectors.toSet());

            String[] actualSplit = actualInvariant.split("&&");
            Set<String> actualSet = Arrays.stream(actualSplit).map(String::trim).collect(Collectors.toSet());

            Assert.assertEquals(expectedSet, actualSet);
        } else {
            assertStringEquals(expectedInvariant, actualInvariant);
        }
    }

    private static void assertPropertiesEqual(Transition expectedTransition, Transition actualTransition) {
        Assert.assertNotNull(actualTransition);
        assertStringEquals(expectedTransition.getAssignments(), actualTransition.getAssignments());
        assertStringEquals(expectedTransition.getComments(), actualTransition.getComments());
        assertStringEquals(expectedTransition.getGuard(), actualTransition.getGuard());
        assertStringEquals(expectedTransition.getSelects(), actualTransition.getSelects());
        assertStringEquals(expectedTransition.getSynchronisation(), actualTransition.getSynchronisation());
    }

    private static void assertStringEquals(String expectedString, String actualString) {
        Assert.assertEquals(
                Optional.ofNullable(expectedString).orElse(""),
                Optional.ofNullable(actualString).orElse("")
        );
    }

}
