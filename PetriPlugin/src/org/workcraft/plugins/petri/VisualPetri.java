package org.workcraft.plugins.petri;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.petri.tools.*;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName ("Petri Net")
public class VisualPetri extends AbstractVisualModel {

    public VisualPetri(Petri model) {
        this (model, null);
    }

    public VisualPetri(Petri model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new PetriSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new PetriConnectionTool());
        tools.add(new ReadArcConnectionTool());
        tools.add(new PlaceGeneratorTool());
        tools.add(new TransitionGeneratorTool());
        tools.add(new PetriSimulationTool());
        setGraphEditorTools(tools);
    }

    public Petri getPetriNet() {
        return (Petri) getMathModel();
    }

    public VisualPlace createPlace(String mathName, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        Place place = getPetriNet().createPlace(mathName, mathContainer);
        VisualPlace visualPlace = new VisualPlace(place);
        container.add(visualPlace);
        return visualPlace;
    }

    public VisualTransition createTransition(String mathName, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        Transition transition = getPetriNet().createTransition(mathName, mathContainer);
        VisualTransition visualTransition = new VisualTransition(transition);
        add(visualTransition);
        return visualTransition;
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace))
                && ((second instanceof VisualPlace) || (second instanceof VisualReplicaPlace))) {
            throw new InvalidConnectionException("Arcs between places are not allowed.");
        }
        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            throw new InvalidConnectionException("Arcs between transitions are not allowed.");
        }
        if (ConversionUtils.hasReadArcConnection(this, first, second) || ConversionUtils.hasReadArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected by a read-arc.");
        }
        if (ConversionUtils.hasProducingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This producing arc already exists.");
        }
        if (ConversionUtils.hasConsumingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This consuming arc already exists.");
        }
    }

    @Override
    public void validateUndirectedConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        super.validateUndirectedConnection(first, second);
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace))
                && ((second instanceof VisualPlace) || (second instanceof VisualReplicaPlace))) {
            throw new InvalidConnectionException("Read-arcs between places are not allowed.");
        }
        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            throw new InvalidConnectionException("Read-arcs between transitions are not allowed.");
        }
        if (ConversionUtils.hasReadArcConnection(this, first, second)
                || ConversionUtils.hasReadArcConnection(this, second, first)
                || ConversionUtils.hasProducingArcConnection(this, first, second)
                || ConversionUtils.hasProducingArcConnection(this, second, first)
                || ConversionUtils.hasConsumingArcConnection(this, first, second)
                || ConversionUtils.hasConsumingArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected.");
        }
    }

    @Override
    public VisualConnection connectUndirected(VisualNode first, VisualNode second) throws InvalidConnectionException {
        validateUndirectedConnection(first, second);

        VisualNode place = null;
        VisualNode transition = null;
        if (first instanceof VisualTransition) {
            place = second;
            transition = first;
        } else if (second instanceof VisualTransition) {
            place = first;
            transition = second;
        }
        VisualConnection connection = null;
        if ((place != null) && (transition != null)) {
            connection = createReadArcConnection(place, transition);
        }
        return connection;
    }

    private VisualReadArc createReadArcConnection(VisualNode place, VisualNode transition)
             throws InvalidConnectionException {
        Petri petriNet = (Petri) getMathModel();

        Place mPlace = null;
        if (place instanceof VisualPlace) {
            mPlace = ((VisualPlace) place).getReferencedPlace();
        } else if (place instanceof VisualReplicaPlace) {
            mPlace = ((VisualReplicaPlace) place).getReferencedPlace();
        }
        Transition mTransition = null;
        if (transition instanceof VisualTransition) {
            mTransition = ((VisualTransition) transition).getReferencedTransition();
        }

        VisualReadArc connection = null;
        if ((mPlace != null) && (mTransition != null)) {
            MathConnection mConsumingConnection = petriNet.connect(mPlace, mTransition);
            MathConnection mProducingConnection = petriNet.connect(mTransition, mPlace);

            connection = new VisualReadArc(place, transition, mConsumingConnection, mProducingConnection);
            Hierarchy.getNearestContainer(place, transition).add(connection);
        }
        return connection;
    }

    public Collection<VisualPlace> getVisualPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class);
    }

    public Collection<VisualTransition> getVisualTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
    }

    public VisualTransition getVisualTransition(Transition transition) {
        for (VisualTransition vt: getVisualTransitions()) {
            if (vt.getReferencedTransition() == transition) {
                return vt;
            }
        }
        return null;
    }

}
