package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ConnectionUtils {

    public static void moveInternalContacts(VisualConnection connection) {
        if ((connection != null) && (connection.getGraphic() instanceof Polyline)) {
            Polyline polyline = (Polyline) connection.getGraphic();
            VisualNode first = connection.getFirst();
            VisualNode second = connection.getSecond();
            if (polyline.getControlPointCount() > 0) {
                moveInternalContactsToControlPoints(first, second, polyline);
            } else {
                moveInternalContactrsByGradient(first, second);
            }
        }
    }
    private static void moveInternalContactsToControlPoints(VisualNode first, VisualNode second, Polyline polyline) {
        ControlPoint firstControlPoint = polyline.getFirstControlPoint();
        Point2D firstPos = firstControlPoint.getRootSpacePosition();
        ControlPoint lastControlPoint = polyline.getLastControlPoint();
        Point2D lastPos = lastControlPoint.getRootSpacePosition();
        if (first instanceof VisualContact) {
            VisualContact firstContact = (VisualContact) first;
            if (moveContactIfInsideComponent(firstContact, firstPos)) {
                polyline.remove(firstControlPoint);
            }
        }
        if (second instanceof VisualContact) {
            VisualContact secondContact = (VisualContact) second;
            if (moveContactIfInsideComponent(secondContact, lastPos)) {
                polyline.remove(lastControlPoint);
            }
        }
    }

    private static boolean moveContactIfInsideComponent(VisualContact contact, Point2D pos) {
        Node parent = contact.getParent();
        if (parent instanceof VisualCircuitComponent) {
            VisualCircuitComponent component = (VisualCircuitComponent) parent;
            Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
            if (bb.contains(contact.getPosition())) {
                contact.setRootSpacePosition(pos);
                double dx = getOffset(contact.getX(), bb.getMinX(), bb.getMaxX());
                double dy = getOffset(contact.getY(), bb.getMinY(), bb.getMaxY());
                if (Math.abs(dy) > Math.abs(dx)) {
                    contact.setDirection(dy > 0 ? VisualContact.Direction.SOUTH : VisualContact.Direction.NORTH);
                } else {
                    contact.setDirection(dx > 0 ? VisualContact.Direction.EAST : VisualContact.Direction.WEST);
                }
                return true;
            }
        }
        return false;
    }

    private static double getOffset(double value, double min, double max) {
        if (value > max) {
            return value - max;
        }
        if (value < min) {
            return value - min;
        }
        return 0;
    }

    private static void moveInternalContactrsByGradient(VisualNode first, VisualNode second) {
        Point2D gradient = getGradient(first, second);
        if (gradient != null) {
            if (first instanceof VisualContact) {
                VisualContact firstContact = (VisualContact) first;
                moveContactOutsideComponent(firstContact, gradient.getX(), gradient.getY());
            }
            if (second instanceof VisualContact) {
                VisualContact secondContact = (VisualContact) second;
                moveContactOutsideComponent(secondContact, -gradient.getX(), -gradient.getY());
            }
        }
    }

    private static Point2D getGradient(VisualNode first, VisualNode second) {
        if (!(first instanceof VisualComponent) || !(second instanceof VisualComponent)) {
            return null;
        }
        Point2D firstPos = ((VisualComponent) first).getRootSpacePosition();
        Point2D secondPos = ((VisualComponent) second).getRootSpacePosition();
        double dx = secondPos.getX() - firstPos.getX();
        double dy = secondPos.getY() - firstPos.getY();
        return new Point2D.Double(dx, dy);
    }

    private static void moveContactOutsideComponent(VisualContact contact, double dx, double dy) {
        Node parent = contact.getParent();
        if (parent instanceof VisualCircuitComponent) {
            VisualCircuitComponent component = (VisualCircuitComponent) parent;
            Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
            if (bb.contains(contact.getPosition())) {
                if (Math.abs(dy) > Math.abs(dx)) {
                    VisualContact.Direction direction = (dy > 0) ? VisualContact.Direction.SOUTH : VisualContact.Direction.NORTH;
                    boolean reverseProgression = dx < 0;
                    component.setPositionByDirection(contact, direction, reverseProgression);
                } else {
                    VisualContact.Direction direction = (dx > 0) ? VisualContact.Direction.EAST : VisualContact.Direction.WEST;
                    boolean reverseProgression = dy < 0;
                    component.setPositionByDirection(contact, direction, reverseProgression);
                }
            }
        }
    }

}
