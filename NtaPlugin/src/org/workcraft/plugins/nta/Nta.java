package org.workcraft.plugins.nta;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.serialisation.References;
import org.workcraft.types.Func;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

@VisualClass(VisualNta.class)
public class Nta extends AbstractMathModel {

    public Nta() {
        this(null, null);
    }

    public Nta(Container root, References refs) {
        super(root, refs);
    }

    public Transition connect(Location first, Location second) throws InvalidConnectionException {
        validateConnection(first, second);
        Transition transition = new Transition(first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(transition);
        return transition;
    }

    public Collection<Transition> getAllTransitions() {
        return Hierarchy.getDescendantsOfType(this.getRoot(), Transition.class);
    }

    public Collection<Template> getTemplates() {
        return Hierarchy.getChildrenOfType(this.getRoot(), Template.class);
    }

    public Collection<Location> getAllLocations() {
        return Hierarchy.getDescendantsOfType(this.getRoot(), Location.class);
    }

    public Collection<Location> getAllLocations(Func<Location, Boolean> filter) {
        return Hierarchy.getDescendantsOfType(this.getRoot(), Location.class, filter);
    }

    public Collection<Transition> getAllTransitions(Func<Transition, Boolean> filter) {
        return Hierarchy.getDescendantsOfType(this.getRoot(), Transition.class, filter);
    }

}
