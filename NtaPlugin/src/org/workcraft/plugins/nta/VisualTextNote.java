package org.workcraft.plugins.nta;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_N)
@DisplayName("Text Note")
@SVGIcon("images/node-comment.svg")
public class VisualTextNote extends VisualComment implements Collapsible {

    public static final String PROPERTY_IS_COLLAPSED = "Is collapsed";

    private boolean isCurrentLevelInside = false;
    private boolean isCollapsed = false;
    private boolean isExcited = false;

    private RenderedText typeRenderedText = new RenderedText("", getNameFont(), getNamePositioning(), getNameOffset());

    public VisualTextNote(TextNote refNode) {
        super(refNode);
        setTextAlignment(Alignment.LEFT);
        removePropertyDeclarationByName(PROPERTY_TEXT_ALIGNMENT);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualTextNote, Boolean>(
                this, PROPERTY_IS_COLLAPSED, Boolean.class, true, true) {
            @Override
            public void setter(VisualTextNote object, Boolean value) {
                object.setIsCollapsed(value);
            }
            @Override
            public Boolean getter(VisualTextNote object) {
                return object.getIsCollapsed();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualTextNote, TextNote.Type>(
                this, TextNote.PROPERTY_TYPE, TextNote.Type.class, true, true) {
            @Override
            public void setter(VisualTextNote object, TextNote.Type value) {
                object.getReferencedTextNote().setType(value);
            }
            @Override
            public TextNote.Type getter(VisualTextNote object) {
                return object.getReferencedTextNote().getType();
            }
        });
    }

    public void setIsExcited(boolean value) {
        if (isExcited != value) {
            sendNotification(new TransformChangingEvent(this));
            isExcited = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public void setIsCollapsed(boolean value) {
        if (isCollapsed != value) {
            sendNotification(new TransformChangingEvent(this));
            isCollapsed = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public boolean getIsCollapsed() {
        return isCollapsed && !isExcited;
    }

    public void setIsCurrentLevelInside(boolean value) {
        if (isCurrentLevelInside != value) {
            sendNotification(new TransformChangingEvent(this));
            this.isCurrentLevelInside = value;
            sendNotification(new TransformChangedEvent(this));
        }
    }

    public boolean isCurrentLevelInside() {
        return isCurrentLevelInside;
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        Rectangle2D bb = null;
        if (!getIsCollapsed() || isCurrentLevelInside()) {
            bb = super.getInternalBoundingBoxInLocalSpace();
            if (getLabelVisibility()) {
                bb = BoundingBoxHelper.union(bb, getLabelBoundingBox());
            }
        }
        if (bb == null) {
            bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
        }
        return BoundingBoxHelper.expand(bb, 0.2, 0.2);
    }

    @Override
    public String getLabel() {
        return getReferencedTextNote().getText();
    }

    @Override
    public void setLabel(String value) {
        getReferencedTextNote().setText(value);
    }

    @Override
    public boolean getLabelVisibility() {
        return !getIsCollapsed() || isCurrentLevelInside();
    }

    @Override
    public Point2D getNameOffset() {
        return getOffset(getNamePositioning());
    }

    @Override
    public void draw(DrawRequest r) {
        drawOutline(r);
        drawLabelInLocalSpace(r);
        drawTypeInLocalSpace(r);
    }

    protected void drawTypeInLocalSpace(DrawRequest r) {
        cacheTypeRenderedText(getReferencedTextNote().getType().toString(), getNameFont(), getNamePositioning(), getNameOffset());
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        g.setColor(Coloriser.colorise(getNameColor(), d.getColorisation()));
        typeRenderedText.draw(g);
    }

    private void cacheTypeRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if (typeRenderedText.isDifferent(text, font, positioning, offset)) {
            typeRenderedText = new RenderedText(text, font, positioning, offset);
        }
    }

    @Override
    public void drawOutline(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        cacheRenderedText(r); // needed to better estimate the bounding box
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getBackground()));
            g.fill(bb);
            g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
            float w = (float) strokeWidth;
            float[] pattern = {10.0f * w, 10.0f * w};
            g.setStroke(new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
            g.draw(bb);
        }
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (getLabelVisibility()) {
            bb = BoundingBoxHelper.union(bb, getLabelBoundingBox());
        }
        bb = BoundingBoxHelper.union(bb, getTypeBoundingBox());
        return bb;
    }

    private Rectangle2D getTypeBoundingBox() {
        if ((typeRenderedText != null) && !typeRenderedText.isEmpty()) {
            return typeRenderedText.getBoundingBox();
        } else {
            return null;
        }
    }

    public void cacheLabelRenderedText() {
        super.cacheLabelRenderedText(null);
    }

    public TextNote getReferencedTextNote() {
        return (TextNote) getReferencedComponent();
    }

}
