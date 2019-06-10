package org.workcraft.plugins.nta;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.*;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.nta.tools.LocationGeneratorTool;
import org.workcraft.plugins.nta.tools.TemplateGeneratorTool;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Network of Timed Automata")
public class VisualNta extends AbstractVisualModel {

    public VisualNta(Nta model) {
        this(model, null);
    }

    public VisualNta(Nta model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(false, false, false, false));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(TextNote.class)));
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new LocationGeneratorTool());
        tools.add(new TemplateGeneratorTool());
        setGraphEditorTools(tools);
    }

    @Override
    public VisualTransition connect(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        Transition mTransition = (Transition) mConnection;

        validateConnection(first, second);
        if (mTransition == null) {
            Location mFirst = (Location) getReferencedComponent(first);
            Location mSecond = (Location) getReferencedComponent(second);
            mTransition = ((Nta) getMathModel()).connect(mFirst, mSecond);
        }
        VisualTransition vConnection = new VisualTransition(mTransition, (VisualLocation) first, (VisualLocation) second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(vConnection);
        return vConnection;
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        if (!(first instanceof VisualLocation) || !(second instanceof VisualLocation)) {
            throw new InvalidConnectionException("Invalid connection.");
        }

        if (first.getParent() != second.getParent()) {
            throw new InvalidConnectionException("Invalid connection");
        }

        getMathModel().validateConnection(
                ((VisualComponent) first).getReferencedComponent(),
                ((VisualComponent) second).getReferencedComponent());
    }

    public Nta getReferencedNta() {
        return (Nta) getMathModel();
    }

    public Collection<VisualTemplate> getVisualTemplates() {
        return Hierarchy.getDescendantsOfType(this.getRoot(), VisualTemplate.class);
    }

}
