package org.workcraft.plugins.nta;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;

@Hotkey(KeyEvent.VK_L)
@DisplayName("Location")
@SVGIcon("images/ta-node-vertex.svg")
public class VisualLocation extends VisualComponent {

    private static final double size = 1.0;
    private static final float strokeWidth = 0.1f;

    public VisualLocation(Location location) {
        super(location);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualLocation, String>(
                this, Location.PROPERTY_COMMENTS, String.class, true, true) {
            public void setter(VisualLocation object, String value) {
                object.getReferencedLocation().setComments(value);
            }

            public String getter(VisualLocation object) {
                return object.getReferencedLocation().getComments();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualLocation, Boolean>(
                this, Location.PROPERTY_IS_COMMITTED, Boolean.class, false, false) {
            public void setter(VisualLocation object, Boolean value) {
                object.getReferencedLocation().setCommitted(value);
            }
            public Boolean getter(VisualLocation object) {
                return object.getReferencedLocation().isCommitted();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualLocation, Boolean>(
                this, Location.PROPERTY_IS_INITIAL, Boolean.class, false, false) {
            public void setter(VisualLocation object, Boolean value) {
                object.getReferencedLocation().setInitial(value);
            }
            public Boolean getter(VisualLocation object) {
                return object.getReferencedLocation().isInitial();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualLocation, Boolean>(
                this, Location.PROPERTY_IS_URGENT, Boolean.class, false, false) {
            public void setter(VisualLocation object, Boolean value) {
                object.getReferencedLocation().setUrgent(value);
            }
            public Boolean getter(VisualLocation object) {
                return object.getReferencedLocation().isUrgent();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualLocation, Integer>(
                this, Location.PROPERTY_NUMBER, Integer.class, false, false) {
            public void setter(VisualLocation object, Integer value) {
                object.getReferencedLocation().setNumber(value);
            }
            public Integer getter(VisualLocation object) {
                return object.getReferencedLocation().getNumber();
            }
        });

        renamePropertyDeclarationByName(PROPERTY_LABEL, Location.PROPERTY_INVARIANT);
    }

    @Override
    public void setLabel(String value) {
        getReferencedLocation().setInvariant(value);
    }

    @Override
    public String getLabel() {
        return getReferencedLocation().getInvariant();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        double s = size - strokeWidth;
        Shape shape = new Ellipse2D.Double(-s / 2, -s / 2, s, s);
        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        if (getReferencedLocation().isCommitted()) {
            g.draw(getCommittedMarkerShape());
        }
        if (getReferencedLocation().isInitial()) {
            g.draw(getInitialMarkerShape());
        }
        if (getReferencedLocation().isUrgent()) {
            g.draw(getUrgentMarkerShape());
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    private Shape getCommittedMarkerShape() {
        double s = size * .6 * .5;
        Arc2D arc = new Arc2D.Double();
        arc.setArc(-s / 2, -s / 2, s, s, 90, 180, Arc2D.OPEN);
        return arc;
    }

    private Shape getInitialMarkerShape() {
        double s = size * .6;
        return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
    }

    private Shape getUrgentMarkerShape() {
        double s = size * .6 * .5;
        Arc2D arc = new Arc2D.Double();
        arc.setArc(-s / 2, -s / 2, s, s, 180, 180, Arc2D.OPEN);
        return arc;
    }

    public Location getReferencedLocation() {
        return (Location) getReferencedComponent();
    }

    @Override
    protected boolean cacheNameRenderedText(DrawRequest r) {
        MathModel mathModel = r.getModel().getMathModel();
        Location location = getReferencedLocation();
        String name = location.getNumber() + ": " + mathModel.getName(location);
        return cacheNameRenderedText(name, getNameFont(), getNamePositioning(), getNameOffset());
    }

}
