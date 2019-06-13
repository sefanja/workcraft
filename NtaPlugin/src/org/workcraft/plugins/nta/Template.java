package org.workcraft.plugins.nta;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.nta.utils.ChangeDetector;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

@IdentifierPrefix("T")
@VisualClass(VisualTemplate.class)
public class Template extends PageNode {

    public static final String PROPERTY_INSTANCE_COUNT = "Instance count";
    public static final String PROPERTY_PARAMETERS = "Parameters";

    private int instanceCount = 1;
    private String parameters;

    public Template() {
    }

    // for making copies
    public Template(Template template) {
        instanceCount = template.instanceCount;
        parameters = template.parameters;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int value) {
        if (value < 0) {
            throw new ArgumentException(PROPERTY_INSTANCE_COUNT + " must be greater than 0.");
        }
        if (instanceCount != value) {
            instanceCount = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INSTANCE_COUNT));
        }
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String value) {
        if (ChangeDetector.hasChanged(parameters, value)) {
            parameters = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_PARAMETERS));
        }
    }

    public Collection<Location> getLocations() {
        return Hierarchy.getChildrenOfType(this, Location.class);
    }

    public Collection<Transition> getTransitions() {
        return Hierarchy.getChildrenOfType(this, Transition.class);
    }

}
