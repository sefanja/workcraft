package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.util.*;

public class ContractTransitionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private final HashSet<VisualConnection> convertedReplicaConnections = new HashSet<>();

    @Override
    public String getDisplayName() {
        return "Contract a selected transition";
    }

    @Override
    public String getPopupName() {
        return "Contract transition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetri.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        Collection<VisualNode> nodes = collect(visualModel);
        if (nodes.size() > 1) {
            DialogUtils.showError("One transition can be contracted at a time.");
        } else if (!nodes.isEmpty()) {
            we.saveMemento();
            transform(visualModel, nodes);
            visualModel.selectNone();
        }
        return null;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> transitions = new HashSet<>();
        transitions.addAll(ConversionUtils.getVisualTransitions(model));
        transitions.retainAll(model.getSelection());
        return transitions;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualTransition) {
            PetriModel mathModel = (PetriModel) model.getMathModel();
            VisualTransition transition = (VisualTransition) node;
            Transition mathTransition = transition.getReferencedTransition();
            if (hasSelfLoop(mathModel, mathTransition)) {
                DialogUtils.showError("A transition with a self-loop/read-arc cannot be contracted.");
            } else if (needsWaitedArcs(mathModel, mathTransition)) {
                DialogUtils.showError("This transformation requires weighted arcs that are currently not supported.");
            } else if (isLanguageChanging(mathModel, mathTransition)) {
                contractTransition(model, transition);
                DialogUtils.showWarning("This transformation may change the language.");
            } else if (isSafenessViolationg(mathModel, mathTransition)) {
                contractTransition(model, transition);
                DialogUtils.showWarning("This transformation may be not safeness-preserving.");
            } else {
                contractTransition(model, transition);
            }
        }
    }

    private boolean needsWaitedArcs(PetriModel model, Transition transition) {
        HashSet<MathNode> predPredNodes = new HashSet<>();
        HashSet<MathNode> predSuccNodes = new HashSet<>();
        for (MathNode predNode: model.getPreset(transition)) {
            predPredNodes.addAll(model.getPreset(predNode));
            predSuccNodes.addAll(model.getPostset(predNode));
        }
        HashSet<MathNode> succPredNodes = new HashSet<>();
        HashSet<MathNode> succSuccNodes = new HashSet<>();
        for (MathNode succNode: model.getPostset(transition)) {
            succPredNodes.addAll(model.getPreset(succNode));
            succSuccNodes.addAll(model.getPostset(succNode));
        }
        predPredNodes.retainAll(succPredNodes);
        predSuccNodes.retainAll(succSuccNodes);
        return !(predPredNodes.isEmpty() && predSuccNodes.isEmpty());
    }

    private boolean hasSelfLoop(PetriModel model, Transition transition) {
        HashSet<MathNode> connectedNodes = new HashSet<>(model.getPreset(transition));
        connectedNodes.retainAll(model.getPostset(transition));
        return !connectedNodes.isEmpty();
    }

    private boolean isLanguageChanging(PetriModel model, Transition transition) {
        return !isType1Secure(model, transition) && !isType2Secure(model, transition);
    }

    // There are no choice places in the preset (preset can be empty).
    private boolean isType1Secure(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        for (MathNode predNode: predNodes) {
            HashSet<MathNode> predSuccNodes = new HashSet<>(model.getPostset(predNode));
            predSuccNodes.remove(transition);
            if (!predSuccNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // There is at least one unmarked place in the postset AND there are no merge places in the postset (the postset cannot be empty).
    private boolean isType2Secure(PetriModel model, Transition transition) {
        Set<MathNode> succNodes = model.getPostset(transition);
        int markedPlaceCount = 0;
        for (MathNode succNode: succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                markedPlaceCount++;
            }
        }
        if (markedPlaceCount >= succNodes.size()) {
            return false;
        }
        for (MathNode succNode: succNodes) {
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSafenessViolationg(PetriModel model, Transition transition) {
        return !isType1Safe(model, transition) && !isType2Safe(model, transition) && !isType3Safe(model, transition);
    }

    // The only place in the postset is unmarked AND it is not a merge.
    private boolean isType1Safe(PetriModel model, Transition transition) {
        Set<MathNode> succNodes = model.getPostset(transition);
        if (succNodes.size() != 1) {
            return false;
        }
        for (MathNode succNode: succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // There is a single place in the preset AND all the postset places are unmarked and not merge places (the postset cannot be empty).
    private boolean isType2Safe(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        if (predNodes.size() != 1) {
            return false;
        }
        Set<MathNode> succNodes = model.getPostset(transition);
        if (succNodes.isEmpty()) {
            return false;
        }
        for (MathNode succNode: succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // The only preset place is not a choice.
    private boolean isType3Safe(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        if (predNodes.size() != 1) {
            return false;
        }
        for (MathNode predNode: predNodes) {
            HashSet<MathNode> predSuccNodes = new HashSet<>(model.getPostset(predNode));
            predSuccNodes.remove(transition);
            if (!predSuccNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void contractTransition(VisualModel visualModel, VisualTransition visualTransition) {
        beforeContraction(visualModel, visualTransition);
        LinkedList<VisualNode> predNodes = new LinkedList<>(visualModel.getPreset(visualTransition));
        LinkedList<VisualNode> succNodes = new LinkedList<>(visualModel.getPostset(visualTransition));
        boolean isTrivial = (predNodes.size() == 1) && (succNodes.size() == 1);
        HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap = new HashMap<>();
        for (VisualNode predNode: predNodes) {
            VisualPlace predPlace = (VisualPlace) predNode;
            for (VisualNode succNode: succNodes) {
                VisualPlace succPlace = (VisualPlace) succNode;
                VisualPlace productPlace = createProductPlace(visualModel, predPlace, succPlace);
                initialiseProductPlace(visualModel, predPlace, succPlace, productPlace);
                HashSet<VisualConnection> connections = new HashSet<>();
                for (Connection connection: visualModel.getConnections(predPlace)) {
                    connections.add((VisualConnection) connection);
                }
                for (Connection connection: visualModel.getConnections(succPlace)) {
                    connections.add((VisualConnection) connection);
                }
                HashMap<VisualConnection, VisualConnection> productConnectionMap = connectProductPlace(visualModel, connections, productPlace);
                productPlaceMap.put(productPlace, new Pair<>(predPlace, succPlace));
                if (isTrivial) {
                    productPlace.copyPosition(visualTransition);
                    Connection predConnection = visualModel.getConnection(predPlace, visualTransition);
                    LinkedList<Point2D> predLocations = ConnectionHelper.getMergedControlPoints(predPlace, null, (VisualConnection) predConnection);
                    Connection succConnection = visualModel.getConnection(visualTransition, succPlace);
                    LinkedList<Point2D> succLocations = ConnectionHelper.getMergedControlPoints(succPlace, (VisualConnection) succConnection, null);
                    if (visualModel.getPostset(succPlace).size() < 2) {
                        for (VisualConnection newConnection: productConnectionMap.keySet()) {
                            if (newConnection.getFirst() == productPlace) {
                                ConnectionHelper.prependControlPoints(newConnection, succLocations);
                            }
                            filterControlPoints(newConnection);
                        }
                    }
                    if (visualModel.getPreset(predPlace).size() < 2) {
                        for (VisualConnection newConnection: productConnectionMap.keySet()) {
                            if (newConnection.getSecond() == productPlace) {
                                ConnectionHelper.addControlPoints(newConnection, predLocations);
                            }
                            filterControlPoints(newConnection);
                        }
                    }
                }
            }
        }
        visualModel.remove(visualTransition);
        afterContraction(visualModel, visualTransition, productPlaceMap);
        for (VisualNode predNode: predNodes) {
            visualModel.remove(predNode);
        }
        for (VisualNode succNode: succNodes) {
            visualModel.remove(succNode);
        }
    }

    public VisualPlace createProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace) {
        Container visualContainer = (Container) Hierarchy.getCommonParent(predPlace, succPlace);
        Container mathContainer = NamespaceHelper.getMathContainer(visualModel, visualContainer);
        MathModel mathModel = visualModel.getMathModel();
        HierarchyReferenceManager refManager = (HierarchyReferenceManager) mathModel.getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager((NamespaceProvider) mathContainer);
        String predName = visualModel.getMathName(predPlace);
        String succName = visualModel.getMathName(succPlace);
        String productName = nameManagerer.getDerivedName(null, predName + succName);
        Place mathPlace = mathModel.createNode(productName, mathContainer, Place.class);
        return visualModel.createVisualComponent(mathPlace, VisualPlace.class, visualContainer);
    }

    public void initialiseProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace, VisualPlace productPlace) {
        Point2D pos = Geometry.middle(predPlace.getRootSpacePosition(), succPlace.getRootSpacePosition());
        productPlace.setRootSpacePosition(pos);
        productPlace.mixStyle(predPlace, succPlace);
        // Correct the token count and capacity of the new place
        Place mathPredPlace = predPlace.getReferencedPlace();
        Place mathSuccPlace = succPlace.getReferencedPlace();
        Place mathProductPlace = productPlace.getReferencedPlace();
        int tokens = mathPredPlace.getTokens() + mathSuccPlace.getTokens();
        mathProductPlace.setTokens(tokens);
        int capacity = tokens;
        if (capacity < mathPredPlace.getCapacity()) {
            capacity = mathPredPlace.getCapacity();
        }
        if (capacity < mathSuccPlace.getCapacity()) {
            capacity = mathSuccPlace.getCapacity();
        }
        mathProductPlace.setCapacity(capacity);
    }

    public HashMap<VisualConnection, VisualConnection> connectProductPlace(VisualModel visualModel,
            Set<VisualConnection> originalConnections, VisualPlace productPlace) {

        HashMap<VisualConnection, VisualConnection> productConnectionMap = new HashMap<>();
        for (VisualConnection originalConnection: originalConnections) {
            VisualNode first = originalConnection.getFirst();
            VisualNode second = originalConnection.getSecond();
            VisualConnection newConnection = null;
            try {
                if (originalConnection instanceof VisualReadArc) {
                    if (first instanceof VisualTransition) {
                        newConnection = visualModel.connectUndirected(first, productPlace);
                    }
                    if (second instanceof VisualTransition) {
                        newConnection = visualModel.connectUndirected(productPlace, second);
                    }
                } else {
                    if (first instanceof VisualTransition) {
                        newConnection = visualModel.connect(first, productPlace);
                    }
                    if (second instanceof VisualTransition) {
                        newConnection = visualModel.connect(productPlace, second);
                    }
                }
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
            if (newConnection instanceof VisualConnection) {
                productConnectionMap.put(newConnection, originalConnection);
                newConnection.copyStyle(originalConnection);
                newConnection.copyShape(originalConnection);
                filterControlPoints(newConnection);
            }
        }
        return productConnectionMap;
    }

    public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
        convertedReplicaConnections.clear();
        HashSet<VisualReplicaPlace> replicaPlaces = new HashSet<>();
        for (Connection connection: visualModel.getConnections(visualTransition)) {
            VisualReplicaPlace replicaPlace = null;
            if (connection.getFirst() instanceof VisualReplicaPlace) {
                replicaPlace = (VisualReplicaPlace) connection.getFirst();
            }
            if (connection.getSecond() instanceof VisualReplicaPlace) {
                replicaPlace = (VisualReplicaPlace) connection.getSecond();
            }
            if (replicaPlace != null) {
                replicaPlaces.add(replicaPlace);
            }

            VisualPlace place = null;
            if (connection.getFirst() instanceof VisualPlace) {
                place = (VisualPlace) connection.getFirst();
            }
            if (connection.getSecond() instanceof VisualPlace) {
                place = (VisualPlace) connection.getSecond();
            }
            if (place != null) {
                for (Replica replica: place.getReplicas()) {
                    if (replica instanceof VisualReplicaPlace) {
                        replicaPlaces.add((VisualReplicaPlace) replica);
                    }
                }
            }
        }
        for (VisualReplicaPlace replica: replicaPlaces) {
            VisualConnection newConnection = ConversionUtils.collapseReplicaPlace(visualModel, replica);
            convertedReplicaConnections.add(newConnection);
        }
    }

    public void afterContraction(VisualModel visualModel, VisualTransition visualTransition,
            HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap) {
        HashSet<VisualConnection> replicaPlaceConnections = new HashSet<>();
        for (VisualPlace productPlace: productPlaceMap.keySet()) {
            for (Connection productConnection: visualModel.getConnections(productPlace)) {
                Pair<VisualPlace, VisualPlace> originalPlaces = productPlaceMap.get(productPlace);
                VisualPlace predPlace = originalPlaces.getFirst();
                VisualPlace succPlace = originalPlaces.getSecond();

                Connection predPlaceConnection = null;
                Connection succPlaceConnection = null;
                if (productConnection.getFirst() instanceof VisualTransition) {
                    VisualTransition transition = (VisualTransition) productConnection.getFirst();
                    predPlaceConnection = visualModel.getConnection(transition, predPlace);
                    succPlaceConnection = visualModel.getConnection(transition, succPlace);
                }
                if (productConnection.getSecond() instanceof VisualTransition) {
                    VisualTransition transition = (VisualTransition) productConnection.getSecond();
                    predPlaceConnection = visualModel.getConnection(predPlace, transition);
                    succPlaceConnection = visualModel.getConnection(succPlace, transition);
                }
                if (((predPlaceConnection == null) || convertedReplicaConnections.contains(predPlaceConnection))
                        && ((succPlaceConnection == null) || convertedReplicaConnections.contains(succPlaceConnection))) {
                    replicaPlaceConnections.add((VisualConnection) productConnection);
                }
            }
        }
        for (VisualConnection replicaPlaceConnection: replicaPlaceConnections) {
            ConversionUtils.replicateConnectedPlace(visualModel, replicaPlaceConnection);
        }
    }

    public void filterControlPoints(VisualConnection connection) {
        ConnectionGraphic grapic = connection.getGraphic();
        if (grapic instanceof Polyline) {
            ConnectionHelper.filterControlPoints((Polyline) grapic);
        }
    }

}
