package org.workcraft.plugins.nta.converters;

import org.workcraft.plugins.nta.*;
import org.workcraft.utils.Hierarchy;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstantiationConverter {

    private final VisualNta srcModel;
    private final VisualNta dstModel;

    private static final Pattern variablePattern = Pattern.compile("([_A-Za-z][_A-Za-z0-9]*)(\\s*\\W?)");

    public InstantiationConverter(VisualNta srcModel, VisualNta dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        convertTopLevelVisualTextNotes();
        convertVisualTemplates();
    }

    private void convertTopLevelVisualTextNotes() {
        for (VisualTextNote srcVisualTextNote : Hierarchy.getChildrenOfType(srcModel.getRoot(), VisualTextNote.class)) {
            TextNote dstTextNote = new TextNote(srcVisualTextNote.getReferencedTextNote());
            VisualTextNote dstVisualTextNote = new VisualTextNote(dstTextNote);
            dstVisualTextNote.copyPosition(srcVisualTextNote);
            dstVisualTextNote.copyStyle(srcVisualTextNote);
            dstModel.getReferencedNta().add(dstTextNote);
            dstModel.add(dstVisualTextNote);
        }
    }

    private void convertVisualTemplates() {
        for (VisualTemplate srcVisualTemplate : srcModel.getVisualTemplates()) {
            int instanceCount = srcVisualTemplate.getReferencedTemplate().getInstanceCount();
            for (int instanceNumber = 1; instanceNumber <= instanceCount; instanceNumber++) {
                createDstVisualTemplate(srcVisualTemplate, instanceCount > 1, instanceNumber);
            }
        }
    }

    private void createDstVisualTemplate(VisualTemplate srcVisualTemplate, boolean rename, int instanceNumber) {
        Template dstTemplate = new Template();
        dstModel.getReferencedNta().add(dstTemplate);
        String dstTemplateName = srcModel.getReferencedNta()
                .getName(srcVisualTemplate.getReferencedTemplate()) + (rename ? instanceNumber : "");
        dstModel.getReferencedNta().setName(dstTemplate, dstTemplateName);

        VisualTemplate dstVisualTemplate = new VisualTemplate(dstTemplate);
        Map<VisualLocation, VisualLocation> locationMap = new HashMap<>();
        dstVisualTemplate.copyStyle(srcVisualTemplate);
        dstVisualTemplate.copyPosition(srcVisualTemplate);
        dstModel.add(dstVisualTemplate);

        for (VisualTextNote srcVisualTextNote : srcVisualTemplate.getVisualTextNotes()) {
            TextNote dstTextNote = new TextNote(srcVisualTextNote.getReferencedTextNote());
            dstTemplate.add(dstTextNote);

            VisualTextNote dstVisualTextNote = new VisualTextNote(dstTextNote);
            dstVisualTextNote.copyPosition(srcVisualTextNote);
            dstVisualTextNote.copyStyle(srcVisualTextNote);
            dstVisualTemplate.add(dstVisualTextNote);
        }

        for (VisualLocation srcVisualLocation : srcVisualTemplate.getVisualLocations()) {
            Location dstLocation = new Location(srcVisualLocation.getReferencedLocation());
            dstTemplate.add(dstLocation);

            VisualLocation dstVisualLocation = new VisualLocation(dstLocation);
            dstVisualLocation.copyStyle(srcVisualLocation);
            dstVisualLocation.copyPosition(srcVisualLocation);
            dstVisualTemplate.add(dstVisualLocation);
            locationMap.put(srcVisualLocation, dstVisualLocation);

            dstModel.getReferencedNta().setName(dstLocation,
                    srcModel.getReferencedNta().getName(srcVisualLocation.getReferencedLocation()));
        }

        for (VisualTransition srcVisualTransition : srcVisualTemplate.getVisualTransitions()) {
            VisualLocation srcFirst = (VisualLocation) srcVisualTransition.getFirst();
            VisualLocation srcSecond = (VisualLocation) srcVisualTransition.getSecond();

            Transition dstTransition = new Transition(
                    srcVisualTransition.getReferencedTransition(),
                    locationMap.get(srcFirst).getReferencedLocation(),
                    locationMap.get(srcSecond).getReferencedLocation());
            dstTemplate.add(dstTransition);

            VisualTransition dstVisualTransition = new VisualTransition(
                    dstTransition,
                    locationMap.get(srcFirst),
                    locationMap.get(srcSecond));
            dstVisualTransition.copyStyle(srcVisualTransition);
            dstVisualTransition.copyShape(srcVisualTransition);
            dstVisualTemplate.add(dstVisualTransition);
        }

        if (rename) {
            renameVariables(dstTemplate, instanceNumber);
        }
    }

    private static void renameVariables(Template template, int instanceNumber) {
        for (Location location : template.getLocations()) {
            location.setInvariant(renameVariables(location.getInvariant(), instanceNumber));
        }
        for (Transition transition : template.getTransitions()) {
            transition.setAssignments(renameVariables(transition.getAssignments(), instanceNumber));
            transition.setGuard(renameVariables(transition.getGuard(), instanceNumber));
            transition.setSelects(renameVariables(transition.getSelects(), instanceNumber));
        }
    }

    private static String renameVariables(String expression, int instanceNumber) {
        // Quick and dirty solution that might not work for all possible expressions, although it does work for the
        // subset of expressions supported by TimeSolver.
        if (expression == null) {
            return null;
        }
        Matcher matcher = variablePattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String dstClock = matcher.group(1) + instanceNumber;
            matcher.appendReplacement(sb, dstClock + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public VisualNta getDstModel() {
        return dstModel;
    }
}
