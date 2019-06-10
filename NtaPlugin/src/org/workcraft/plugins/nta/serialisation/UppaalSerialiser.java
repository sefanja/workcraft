package org.workcraft.plugins.nta.serialisation;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.interop.UppaalFormat;
import org.workcraft.plugins.nta.uppaal.*;
import org.workcraft.plugins.nta.uppaal.Location;
import org.workcraft.plugins.nta.uppaal.Nta;
import org.workcraft.plugins.nta.uppaal.Template;
import org.workcraft.plugins.nta.uppaal.Transition;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

import javax.xml.bind.JAXB;
import java.io.OutputStream;
import java.util.*;

public class UppaalSerialiser implements ModelSerialiser {
    private static final double SCALE = 20.0;

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof VisualNta;
    }

    @Override
    public UUID getFormatUUID() {
        return UppaalFormat.getInstance().getUuid();
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (!this.isApplicableTo(model)) {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }

        VisualNta vNta = (VisualNta) model;
        Nta uNta = generateNta(vNta);
        JAXB.marshal(uNta, out);

        return refs;
    }

    private Nta generateNta(VisualNta vNta) {
        Nta uNta = new Nta();

        org.workcraft.dom.Container container = vNta.getReferencedNta().getRoot();
        uNta.setDeclaration(replaceNewlines(TextNote.findText(container, TextNote.Type.DECLARATION)));
        uNta.setInstantiation(replaceNewlines(TextNote.findText(container, TextNote.Type.INSTANTIATION)));
        uNta.setSystem(replaceNewlines(TextNote.findText(container, TextNote.Type.SYSTEM)));

        uNta.getTemplate().addAll(generateTemplates(vNta));

        return uNta;
    }

    private List<Template> generateTemplates(VisualNta vNta) {
        List<Template> templates = new ArrayList<>();
        for (VisualTemplate vTemplate : vNta.getVisualTemplates()) {
            org.workcraft.plugins.nta.Template mTemplate = vTemplate.getReferencedTemplate();
            Template uTemplate = new Template();

            uTemplate.setName(generateName(vNta, vTemplate));

            String parameters = mTemplate.getParameters();
            if (parameters != null) {
                Parameter uParameter = new Parameter();
                uParameter.setValue(parameters);
                uTemplate.setParameter(uParameter);
            }

            uTemplate.setDeclaration(replaceNewlines(TextNote.findText(mTemplate, TextNote.Type.DECLARATION)));

            Map<org.workcraft.plugins.nta.Location, Location> locationMap = generateLocations(vNta, vTemplate);

            uTemplate.getLocation().addAll(locationMap.values());

            uTemplate.setInit(generateInit(locationMap, vTemplate));

            uTemplate.getTransition().addAll(generateTransitions(locationMap, vTemplate));

            templates.add(uTemplate);
        }
        return templates;
    }

    private Map<org.workcraft.plugins.nta.Location, Location> generateLocations(
            VisualNta vNta, VisualTemplate vTemplate) {
        Map<org.workcraft.plugins.nta.Location, Location> locationMap = new HashMap<>();

        int id = 0;
        for (VisualLocation vLocation : vTemplate.getVisualLocations()) {
            org.workcraft.plugins.nta.Location mLocation = vLocation.getReferencedLocation();
            Location uLocation = new Location();

            uLocation.setId("id" + id++);
            uLocation.setName(generateName(vNta, mLocation));
            if (mLocation.isCommitted()) {
                uLocation.setCommitted(new Committed());
            }
            if (mLocation.isUrgent()) {
                uLocation.setUrgent(new Urgent());
            }
            uLocation.setX((int) (vLocation.getPosition().getX() * SCALE));
            uLocation.setY((int) (vLocation.getPosition().getY() * SCALE));

            String invariant = mLocation.getInvariant();
            if (invariant != null) {
                uLocation.getLabel().add(generateLabel("invariant", invariant));
            }

            locationMap.put(mLocation, uLocation);
        }
        return locationMap;
    }

    private Init generateInit(Map<org.workcraft.plugins.nta.Location, Location> locationMap, VisualTemplate vTemplate) {
        for (VisualLocation vLocation : vTemplate.getVisualLocations()) {
            org.workcraft.plugins.nta.Location mLocation = vLocation.getReferencedLocation();
            if (mLocation.isInitial()) {
                Init uInit = new Init();
                uInit.setRef(locationMap.get(mLocation));
                return uInit;
            }
        }
        return null;
    }

    private List<Transition> generateTransitions(
            Map<org.workcraft.plugins.nta.Location, Location> locationMap, VisualTemplate vTemplate) {
        List<Transition> uTransitions = new ArrayList<>();
        for (VisualTransition vTransition : vTemplate.getVisualTransitions()) {
            org.workcraft.plugins.nta.Transition mTransition =
                    (org.workcraft.plugins.nta.Transition) vTransition.getReferencedConnection();
            Transition uTransition = new Transition();

            Source uSource = new Source();
            uSource.setRef(locationMap.get(mTransition.getFirst()));
            uTransition.setSource(uSource);

            Target uTarget = new Target();
            uTarget.setRef(locationMap.get(mTransition.getSecond()));
            uTransition.setTarget(uTarget);

            String assignments = mTransition.getAssignments();
            if (assignments != null) {
                uTransition.getLabel().add(generateLabel("assignment", assignments));
            }

            String comments = mTransition.getComments();
            if (comments != null) {
                uTransition.getLabel().add(generateLabel("comments", comments));
            }

            String guard = mTransition.getGuard();
            if (guard != null) {
                uTransition.getLabel().add(generateLabel("guard", guard));
            }

            String selects = mTransition.getSelects();
            if (selects != null) {
                uTransition.getLabel().add(generateLabel("select", selects));
            }

            String synchronisation = mTransition.getSynchronisation();
            if (synchronisation != null) {
                uTransition.getLabel().add(generateLabel("synchronisation", synchronisation));
            }

            uTransition.getNail().addAll(generateNails(vTransition));

            uTransitions.add(uTransition);
        }
        return uTransitions;
    }

    private List<Nail> generateNails(VisualTransition vTransition) {
        List<Nail> uNails = new ArrayList<>();
        ConnectionGraphic graphic = vTransition.getGraphic();

        for (ControlPoint point : graphic.getControlPoints()) {
            Nail uNail = new Nail();
            uNail.setX((int) (point.getPosition().getX() * SCALE));
            uNail.setY((int) (point.getPosition().getY() * SCALE));
            uNails.add(uNail);
        }

        return uNails;
    }

    private Name generateName(VisualNta vNta, Node node) {
        Name uName = new Name();
        uName.setValue(vNta.getMathName(node));
        return uName;
    }

    private Label generateLabel(String kind, String value) {
        Label uLabel = new Label();
        uLabel.setKind(kind);
        uLabel.setValue(value);
        return uLabel;
    }

    private String replaceNewlines(String text) {
        if (text == null) {
            return null;
        }
        return text.replace(
                '|', // Workcraft's newline character
                '\n' // UPPAAL's newline character
        );
    }

}
