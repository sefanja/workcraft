package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

@VisualClass(org.workcraft.plugins.circuit.VisualCircuitComponent.class)
public class CircuitComponent extends MathGroup {

    public static final String PROPERTY_MODULE = "Module";
    public static final String PROPERTY_IS_ENVIRONMENT = "Treat as environment";

    private String name = "";
    private String module = "";
    private boolean isEnvironment = false;

    public void setName(String value) {
        if (value == null) value = "";
        if (!value.equals(name)) {
            name = value;
            sendNotification(new PropertyChangedEvent(this, AbstractVisualModel.PROPERTY_NAME));
        }
    }

    public String getName() {
        return name;
    }

    public void setModule(String value) {
        if (value == null) value = "";
        if (!value.equals(module)) {
            module = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_MODULE));
        }
    }

    public String getModule() {
        return module;
    }

    public boolean isMapped() {
        return (module != null) && !module.isEmpty();
    }

    public void setIsEnvironment(boolean value) {
        if (isEnvironment != value) {
            isEnvironment = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_ENVIRONMENT));
        }
    }

    public boolean getIsEnvironment() {
        return isEnvironment;
    }

    public Collection<Contact> getContacts() {
        return Hierarchy.filterNodesByType(getChildren(), Contact.class);
    }

    public Collection<Contact> getInputs() {
        ArrayList<Contact> result = new ArrayList<>();
        for (Contact contact: getContacts()) {
            if (contact.isInput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public Collection<Contact> getOutputs() {
        ArrayList<Contact> result = new ArrayList<>();
        for (Contact contact: getContacts()) {
            if (contact.isOutput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public Contact getFirstInput() {
        Contact result = null;
        for (Contact contact: getContacts()) {
            if (contact.isInput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    public Contact getFirstOutput() {
        Contact result = null;
        for (Contact contact: getContacts()) {
            if (contact.isOutput()) {
                result = contact;
                break;
            }
        }
        return result;
    }

    public boolean isSingleInputSingleOutput() {
        return (getContacts().size() == 2) && (getFirstInput() != null) && (getFirstOutput() != null);
    }

}
