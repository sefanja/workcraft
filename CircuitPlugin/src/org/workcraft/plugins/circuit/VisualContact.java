package org.workcraft.plugins.circuit;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.tools.StateDecoration;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class VisualContact extends VisualComponent implements StateObserver, CustomTouchable {

    public static final String PROPERTY_DIRECTION = "Direction";

    public enum Direction {
        WEST("West"),
        NORTH("North"),
        EAST("East"),
        SOUTH("South");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public AffineTransform getTransform() {
            AffineTransform result = new AffineTransform();
            switch (this) {
            case WEST:
                result.quadrantRotate(2);
                break;
            case NORTH:
                result.quadrantRotate(3);
                break;
            case EAST:
                result.setToIdentity();
                break;
            case SOUTH:
                result.quadrantRotate(1);
                break;
            }
            return result;
        }

        public Direction rotateClockwise() {
            switch (this) {
            case WEST: return NORTH;
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            default: return this;
            }
        }

        public Direction rotateCounterclockwise() {
            switch (this) {
            case WEST: return SOUTH;
            case NORTH: return WEST;
            case EAST: return NORTH;
            case SOUTH: return EAST;
            default: return this;
            }
        }

        public Direction flipHorizontal() {
            switch (this) {
            case WEST: return EAST;
            case EAST: return WEST;
            default: return this;
            }
        }

        public Direction flipVertical() {
            switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            default: return this;
            }
        }

        public Direction flip() {
            switch (this) {
            case WEST: return EAST;
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            default: return this;
            }
        }

        public boolean isHorisontal() {
            return (this == WEST) || (this == EAST);
        }

        public boolean isVertical() {
            return (this == NORTH) || (this == SOUTH);
        }

        public int getGradientX() {
            switch (this) {
            case WEST: return -1;
            case EAST: return 1;
            default: return 0;
            }
        }

        public int getGradientY() {
            switch (this) {
            case NORTH: return -1;
            case SOUTH: return 1;
            default: return 0;
            }
        }
    };

    public static final Color inputColor = Color.RED;
    public static final Color outputColor = Color.BLUE;

    private static final double size = 0.3;
    private Direction direction = Direction.WEST;

    public VisualContact(Contact contact) {
        super(contact, true, false, false);
        setDefaultDirection();
        contact.addObserver(this);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Direction>(
                this, PROPERTY_DIRECTION, Direction.class, true, true) {
            @Override
            public void setter(VisualContact object, Direction value) {
                object.setDirection(value);
            }
            @Override
            public Direction getter(VisualContact object) {
                return object.getDirection();
            }
            @Override
            public boolean isVisible() {
                return isPort();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, IOType>(
                this, Contact.PROPERTY_IO_TYPE, IOType.class, true, false) {
            @Override
            public void setter(VisualContact object, IOType value) {
                object.getReferencedContact().setIOType(value);
            }
            @Override
            public IOType getter(VisualContact object) {
                return object.getReferencedContact().getIOType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_INIT_TO_ONE, Boolean.class, true, true) {
            @Override
            public void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setInitToOne(value);
            }
            @Override
            public Boolean getter(VisualContact object) {
                return object.getReferencedContact().getInitToOne();
            }
            @Override
            public boolean isVisible() {
                return isDriver() && !isZeroDelayDriver();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_FORCED_INIT, Boolean.class, true, true) {
            @Override
            public void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setForcedInit(value);
            }
            @Override
            public Boolean getter(VisualContact object) {
                return object.getReferencedContact().getForcedInit();
            }
            @Override
            public boolean isVisible() {
                return isDriver() && !isZeroDelayPin();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualContact, Boolean>(
                this, Contact.PROPERTY_PATH_BREAKER, Boolean.class, true, true) {
            @Override
            public void setter(VisualContact object, Boolean value) {
                object.getReferencedContact().setPathBreaker(value);
            }
            @Override
            public Boolean getter(VisualContact object) {
                return object.getReferencedContact().getPathBreaker();
            }
            @Override
            public boolean isVisible() {
                return isPin() && !isZeroDelayDriver();
            }
        });
    }

    @Override
    public Contact getReferencedComponent() {
        return (Contact) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        Contact contact = getReferencedContact();
        return ((contact != null) && contact.isPort()) ? getPortShape() : getContactShape();
    }

    private Shape getForcedShape() {
        Contact contact = getReferencedContact();
        return contact.isPort() ? getPortShape() : getForcedContactShape();
    }

    private Shape getPortShape() {
        double w2 = 0.5 * (size - CircuitSettings.getWireWidth());
        double h2 = 0.5 * (size - CircuitSettings.getWireWidth());
        Path2D path = new Path2D.Double();
        path.moveTo(-w2, -h2);
        path.lineTo(0.0, -h2);
        path.lineTo(w2, 0.0);
        path.lineTo(0.0, h2);
        path.lineTo(-w2, h2);
        path.closePath();
        return path;
    }

    private Shape getContactShape() {
        double w2 = 0.5 * size - CircuitSettings.getWireWidth();
        double h2 = 0.5 * size - CircuitSettings.getWireWidth();
        Path2D path = new Path2D.Double();
        path.moveTo(-w2, -h2);
        path.lineTo(w2, -h2);
        path.lineTo(w2, h2);
        path.lineTo(-w2, h2);
        path.closePath();
        return path;
    }

    private Shape getForcedContactShape() {
        double d = 0.5 * (size - CircuitSettings.getWireWidth());
        Path2D path = new Path2D.Double();
        path.moveTo(-d, 0);
        path.lineTo(0, d);
        path.lineTo(d, 0);
        path.lineTo(0, -d);
        path.closePath();
        return path;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        Color colorisation = d.getColorisation();
        Color fillColor = d.getBackground();
        if (fillColor == null) {
            fillColor = getFillColor();
        }

        AffineTransform savedTransform = g.getTransform();
        AffineTransform rotateTransform = getDirection() != null ? getDirection().getTransform() : new AffineTransform();
        if (isInput()) {
            rotateTransform.quadrantRotate(2);
        }
        g.transform(rotateTransform);

        boolean showContact = CircuitSettings.getShowContacts() || (d instanceof StateDecoration)
                || (d.getColorisation() != null) || (d.getBackground() != null);

        if (showContact || isPort()) {
            boolean showForcedInit = (d instanceof StateDecoration) && ((StateDecoration) d).showForcedInit();
            Shape shape = showForcedInit && isForcedDriver() ? getForcedShape() : getShape();
            float width = (float) CircuitSettings.getBorderWidth();
            g.setStroke(new BasicStroke(width));
            g.setColor(fillColor);
            g.fill(shape);
            g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
            g.draw(shape);
        } else if (r.getModel().getConnections(this).size() > 1) {
            g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
            g.fill(VisualJoint.shape);
        }

        if (!(getParent() instanceof VisualCircuitComponent)) {
            g.setTransform(savedTransform);
            rotateTransform.setToIdentity();
            if (getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH) {
                rotateTransform.quadrantRotate(-1);
            }
            g.transform(rotateTransform);
            drawNameInLocalSpace(r);
        }

        g.setTransform(savedTransform);
        d.decorate(g);
    }

    @Override
    public Positioning getNamePositioning() {
        Positioning result = Positioning.CENTER;
        Direction direction = getDirection();
        if (direction != null) {
            if ((direction == Direction.NORTH) || (direction == Direction.EAST)) {
                result = Positioning.RIGHT;
            } else {
                result = Positioning.LEFT;
            }
        }
        return result;
    }

    @Override
    public Color getNameColor() {
        return isInput() ? inputColor : outputColor;
    }

    @Override
    public Rectangle2D getNameBoundingBox() {
        Rectangle2D bb = super.getNameBoundingBox();
        if (bb != null) {
            AffineTransform rotateTransform = new AffineTransform();
            if (getDirection() == Direction.NORTH || getDirection() == Direction.SOUTH) {
                rotateTransform.quadrantRotate(-1);
            }
            bb = BoundingBoxHelper.transform(bb, rotateTransform);
        }
        return bb;
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        Shape shape = getShape();
        if (shape != null) {
            Point2D p2 = new Point2D.Double();
            p2.setLocation(pointInLocalSpace);
            if (!(getParent() instanceof VisualCircuitComponent)) {
                AffineTransform rotateTransform = getDirection().getTransform();
                if (isInput()) {
                    rotateTransform.quadrantRotate(2);
                }
                rotateTransform.transform(pointInLocalSpace, p2);
            }
            return shape.contains(p2);
        }
        return false;
    }

    @Override
    public Node hitCustom(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        Rectangle2D bb = getNameBoundingBox();
        if (bb != null) {
            if (bb.contains(pointInLocalSpace)) {
                return this;
            }
        }
        return hitTestInLocalSpace(pointInLocalSpace) ? this : null;
    }

    public void setDefaultDirection() {
        setDirection(getReferencedContact().getIOType() == IOType.INPUT ? Direction.WEST : Direction.EAST);
    }

    public void setDirection(Direction value) {
        if (direction != value) {
            sendNotification(new TransformChangingEvent(this));
            direction = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_DIRECTION));
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public Direction getDirection() {
        return direction;
    }

    @NoAutoSerialisation
    public String getName() {
        return getReferencedContact().getName();
    }

    public Contact getReferencedContact() {
        return getReferencedComponent();
    }

    public boolean isInput() {
        return getReferencedContact().isInput();
    }

    public boolean isOutput() {
        return getReferencedContact().isOutput();
    }

    public boolean isPin() {
        return getReferencedContact().isPin();
    }

    public boolean isEnvironmentPin() {
        return getReferencedContact().isEnvironmentPin();
    }

    public boolean isPort() {
        return getReferencedContact().isPort();
    }

    public boolean isDriver() {
        return getReferencedContact().isDriver();
    }

    public boolean isDriven() {
        return getReferencedContact().isDriven();
    }

    public boolean isForcedDriver() {
        return getReferencedContact().isForcedDriver();
    }

    public boolean isZeroDelayPin() {
        return getReferencedContact().isZeroDelayPin();
    }

    public boolean isZeroDelayDriver() {
        return getReferencedContact().isZeroDelayDriver();
    }

    @Override
    public void notify(StateEvent e) {
    }

    @Override
    public void rotateClockwise() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(1);
                Point2D pos = rotateTransform.transform(getPosition(), null);
                setPosition(pos);
            }
        }
        setDirection(getDirection().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                AffineTransform rotateTransform = new AffineTransform();
                rotateTransform.quadrantRotate(-1);
                Point2D pos = rotateTransform.transform(getPosition(), null);
                setPosition(pos);
            }
        }
        setDirection(getDirection().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                setX(-getX());
            }
        }
        setDirection(getDirection().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        if (getParent() instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) getParent();
            if (component.getRenderType() == RenderType.BOX) {
                setY(-getY());
            }
        }
        setDirection(getDirection().flipVertical());
        super.flipVertical();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualContact) {
            VisualContact srcComponent = (VisualContact) src;
            getReferencedContact().setInitToOne(srcComponent.getReferencedContact().getInitToOne());
            getReferencedContact().setForcedInit(srcComponent.getReferencedContact().getForcedInit());
            getReferencedContact().setPathBreaker(srcComponent.getReferencedContact().getPathBreaker());
            // TODO: Note that IOType and Direction are currently NOT copied to allow input/output
            //       port generation with Shift key (and not to be copied from a template node).
            // getReferencedContact().setIOType(srcComponent.getReferencedContact().getIOType());
            // setDirection(srcComponent.getDirection());
        }
    }

}
