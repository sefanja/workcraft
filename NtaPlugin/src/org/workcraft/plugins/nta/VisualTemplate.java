package org.workcraft.plugins.nta;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.utils.Hierarchy;

import java.awt.event.KeyEvent;
import java.util.Collection;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Template")
@SVGIcon("images/selection-page.svg")
public class VisualTemplate extends VisualPage {

    public VisualTemplate(Template refNode) {
        super(refNode);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        renamePropertyDeclarationByName(PROPERTY_LABEL, Template.PROPERTY_PARAMETERS);

        addPropertyDeclaration(new PropertyDeclaration<VisualTemplate, Integer>(
                this, Template.PROPERTY_INSTANCE_COUNT, Integer.class, true, true) {
            public void setter(VisualTemplate object, Integer value) {
                object.getReferencedTemplate().setInstanceCount(value);
            }

            public Integer getter(VisualTemplate object) {
                return object.getReferencedTemplate().getInstanceCount();
            }
        });
    }

    @Override
    public void setLabel(String value) {
        getReferencedTemplate().setParameters(value);
    }

    @Override
    public String getLabel() {
        return getReferencedTemplate().getParameters();
    }

    public Template getReferencedTemplate() {
        return (Template) getReferencedComponent();
    }

    public Collection<VisualLocation> getVisualLocations() {
        return Hierarchy.getDescendantsOfType(this, VisualLocation.class);
    }

    public Collection<VisualTextNote> getVisualTextNotes() {
        return Hierarchy.getDescendantsOfType(this, VisualTextNote.class);
    }

    public Collection<VisualTransition> getVisualTransitions() {
        return Hierarchy.getDescendantsOfType(this, VisualTransition.class);
    }

    @Override
    protected boolean cacheNameRenderedText(DrawRequest r) {
        MathModel mathModel = r.getModel().getMathModel();
        Template template = getReferencedTemplate();
        String name = mathModel.getName(template) + "(" + template.getInstanceCount() + ")";
        return cacheNameRenderedText(name, getNameFont(), getNamePositioning(), getNameOffset());
    }

}
