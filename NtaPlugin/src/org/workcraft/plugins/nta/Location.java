package org.workcraft.plugins.nta;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.nta.utils.ChangeDetector;
import org.workcraft.utils.Hierarchy;

import java.util.Comparator;

@IdentifierPrefix("L")
@VisualClass(VisualLocation.class)
public class Location extends MathNode {

    public static final String PROPERTY_COMMENTS = "Comments";
    public static final String PROPERTY_INVARIANT = "Invariant";
    public static final String PROPERTY_IS_COMMITTED = "Is committed";
    public static final String PROPERTY_IS_INITIAL = "Is initial";
    public static final String PROPERTY_IS_URGENT = "Is urgent";
    public static final String PROPERTY_NUMBER = "Number";

    private String comments;
    private String invariant;
    private boolean isCommitted = false;
    private boolean isInitial = false;
    private boolean isUrgent = false;
    private int number = 0;

    public Location() {
    }

    // for making copies
    public Location(Location location) {
        comments = location.comments;
        isCommitted = location.isCommitted;
        isInitial = location.isInitial;
        invariant = location.invariant;
        isUrgent = location.isUrgent;
        number = location.number;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String value) {
        if (ChangeDetector.hasChanged(comments, value)) {
            comments = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_COMMENTS));
        }
    }

    public String getInvariant() {
        return invariant;
    }

    public void setInvariant(String value) {
        if (ChangeDetector.hasChanged(invariant, value)) {
            invariant = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INVARIANT));
        }
    }

    public boolean isCommitted() {
        return isCommitted;
    }

    public void setCommitted(boolean value) {
        if (ChangeDetector.hasChanged(isCommitted, value)) {
            isCommitted = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_COMMITTED));
        }
    }

    public boolean isInitial() {
        return isInitial;
    }

    public void setInitial(boolean value) {
        if (ChangeDetector.hasChanged(isInitial, value)) {
            if (value) {
                Template template = getTemplate();
                if (template != null) {
                    template.getLocations().stream().filter(Location::isInitial).forEach(l -> l.setInitial(false));
                }
            }
            isInitial = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_INITIAL));
            setDefaultNumber();
        }
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean value) {
        if (ChangeDetector.hasChanged(isUrgent, value)) {
            isUrgent = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_URGENT));
        }
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int value) {
        if (ChangeDetector.hasChanged(number, value)) {
            number = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NUMBER));
        }
    }

    @Override
    public void setParent(Node parent) {
        super.setParent(parent);
        setDefaultNumber();
    }

    public void setDefaultNumber() {
        if (isInitial) {
            setNumber(0); // required by TimeSolver
        } else {
            Template template = getTemplate();
            if (template != null) {
                int max =
                        template.getLocations().stream()
                                .max(Comparator.comparing(Location::getNumber)).get().getNumber();
                if (number < max || number < 1) {
                    setNumber(max + 1);
                }
            }
        }
    }

    public Template getTemplate() {
        return Hierarchy.getNearestAncestor(this, Template.class);
    }

}
