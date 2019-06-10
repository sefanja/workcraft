package org.workcraft.plugins.nta;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.nta.utils.ChangeDetector;
import org.workcraft.utils.Hierarchy;

public class Transition extends MathConnection {

    public static final String PROPERTY_ASSIGNMENTS = "Assignments";
    public static final String PROPERTY_COMMENTS = "Comments";
    public static final String PROPERTY_GUARD = "Guard";
    public static final String PROPERTY_SELECTS = "Selects";
    public static final String PROPERTY_SYNCHRONISATION = "Synchronisation";

    private String assignments;
    private String comments;
    private String guard;
    private String selects;
    private String synchronisation;

    // necessary constructor for deserialization saved .work files
    public Transition() {
    }

    public Transition(Location first, Location second) {
        super(first, second);
    }

    // for making copies
    public Transition(Transition transition, Location firstCopy, Location secondCopy) {
        super(firstCopy, secondCopy);
        guard = transition.guard;
        assignments = transition.assignments;
        comments = transition.comments;
        synchronisation = transition.synchronisation;
        selects = transition.selects;
    }

    public String getAssignments() {
        return assignments;
    }

    public void setAssignments(String value) {
        if (ChangeDetector.hasChanged(assignments, value)) {
            assignments = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ASSIGNMENTS));
        }
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

    public String getGuard() {
        return guard;
    }

    public void setGuard(String value) {
        if (ChangeDetector.hasChanged(guard, value)) {
            guard = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_GUARD));
        }
    }

    public String getSelects() {
        return selects;
    }

    public void setSynchronisation(String value) {
        if (ChangeDetector.hasChanged(synchronisation, value)) {
            synchronisation = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SYNCHRONISATION));
        }
    }

    public String getSynchronisation() {
        return synchronisation;
    }

    public void setSelects(String value) {
        if (ChangeDetector.hasChanged(selects, value)) {
            selects = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SELECTS));
        }
    }

    public boolean isSender() {
        return synchronisation != null && synchronisation.endsWith("!");
    }

    public boolean isReceiver() {
        return synchronisation != null && synchronisation.endsWith("?");
    }

    public String getSynchronisationName() {
        if (synchronisation == null) {
            return null;
        }
        return synchronisation.replaceAll("\\W", "");
    }

    public Template getTemplate() {
        return Hierarchy.getNearestAncestor(this, Template.class);
    }

    @Override
    public Location getFirst() {
        return (Location) super.getFirst();
    }

    @Override
    public Location getSecond() {
        return (Location) super.getSecond();
    }

}
