package org.workcraft.plugins.nta;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonEditorSettings;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DisplayName("Transition")
public class VisualTransition extends VisualConnection {

    // necessary constructor for deserialization saved .work files
    public VisualTransition(Transition mathConnection) {
        this(mathConnection, null, null);
    }

    public static final String PROPERTY_LABEL_COLOR = "Label color";
    private static final String RENDERED_TEXT_LINE_SEPARATOR = "|";

    public static final Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

    private RenderedText labelRenderedText = new RenderedText("", labelFont, Positioning.CENTER, new Point2D.Double());
    private Color labelColor = CommonVisualSettings.getLabelColor();

    public VisualTransition(Transition mathConnection, VisualLocation first, VisualLocation second) {
        super(mathConnection, first, second);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, String>(
                this, Transition.PROPERTY_ASSIGNMENTS, String.class, true, true) {
            public void setter(VisualTransition object, String value) {
                object.getReferencedTransition().setAssignments(value);
            }

            public String getter(VisualTransition object) {
                return object.getReferencedTransition().getAssignments();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, String>(
                this, Transition.PROPERTY_COMMENTS, String.class, true, true) {
            public void setter(VisualTransition object, String value) {
                object.getReferencedTransition().setComments(value);
            }

            public String getter(VisualTransition object) {
                return object.getReferencedTransition().getComments();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, String>(
                this, Transition.PROPERTY_GUARD, String.class, true, true) {
            public void setter(VisualTransition object, String value) {
                object.getReferencedTransition().setGuard(value);
            }

            public String getter(VisualTransition object) {
                return object.getReferencedTransition().getGuard();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, String>(
                this, Transition.PROPERTY_SELECTS, String.class, true, true) {
            public void setter(VisualTransition object, String value) {
                object.getReferencedTransition().setSelects(value);
            }

            public String getter(VisualTransition object) {
                return object.getReferencedTransition().getSelects();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, String>(
                this, Transition.PROPERTY_SYNCHRONISATION, String.class, true, true) {
            public void setter(VisualTransition object, String value) {
                object.getReferencedTransition().setSynchronisation(value);
            }

            public String getter(VisualTransition object) {
                return object.getReferencedTransition().getSynchronisation();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, Color>(
                this, PROPERTY_LABEL_COLOR, Color.class, true, true) {
            public void setter(VisualTransition object, Color value) {
                object.setLabelColor(value);
            }
            public Color getter(VisualTransition object) {
                return object.getLabelColor();
            }
        });
    }

    public boolean getLabelVisibility() {
        String label = getLabel();
        return label != null && !label.isEmpty();
    }

    protected void cacheLabelRenderedText() {
        String label = getLabel();
        if (labelRenderedText.isDifferent(label, labelFont, Positioning.CENTER, new Point2D.Double())) {
            labelRenderedText = new RenderedText(label, labelFont, Positioning.CENTER, new Point2D.Double());
        }
    }

    public String getLabel() {
        List<String> lines = new ArrayList<>();

        Transition transition = getReferencedTransition();
        lines.add(transition.getSelects());
        lines.add(transition.getGuard());
        lines.add(transition.getSynchronisation());
        lines.add(transition.getAssignments());

        return lines.stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(RENDERED_TEXT_LINE_SEPARATOR));
    }

    private AffineTransform getLabelTransform() {
        Point2D middlePoint = getGraphic().getPointOnCurve(0.5);

        Rectangle2D bb = labelRenderedText.getBoundingBox();
        Point2D labelPosition = new Point2D.Double(bb.getCenterX(), bb.getCenterY());

        return AffineTransform.getTranslateInstance(
                middlePoint.getX() - labelPosition.getX(), middlePoint.getY() - labelPosition.getY());
    }

    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            cacheLabelRenderedText();
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();

            AffineTransform oldTransform = g.getTransform();
            AffineTransform transform = getLabelTransform();
            g.transform(transform);
            Color background = d.getBackground();
            if (background != null) {
                g.setColor(Coloriser.colorise(CommonEditorSettings.getBackgroundColor(), background));
                Rectangle2D box = BoundingBoxHelper.expand(labelRenderedText.getBoundingBox(), 0.2, 0.0);
                g.fill(box);
            }
            g.setColor(Coloriser.colorise(getLabelColor(), d.getColorisation()));
            labelRenderedText.draw(g);
            g.setTransform(oldTransform);
        }
    }

    @Override
    public void draw(DrawRequest r) {
        drawLabelInLocalSpace(r);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        Rectangle2D labelBB = getLabelBoundingBox();
        return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
    }

    private Rectangle2D getLabelBoundingBox() {
        AffineTransform transform = getLabelTransform();
        return transform.createTransformedShape(labelRenderedText.getBoundingBox()).getBounds2D();
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(Color value) {
        if (!labelColor.equals(value)) {
            labelColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL_COLOR));
        }
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedConnection();
    }
}
