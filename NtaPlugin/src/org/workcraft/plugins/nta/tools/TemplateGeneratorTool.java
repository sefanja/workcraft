package org.workcraft.plugins.nta.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.nta.Template;
import org.workcraft.plugins.nta.VisualTemplate;
import org.workcraft.utils.GuiUtils;

import java.awt.*;

public class TemplateGeneratorTool extends NodeGeneratorTool {

    public TemplateGeneratorTool() {
        super(new DefaultNodeGenerator(Template.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        VisualModel model = e.getModel();
        if (!(model.getCurrentLevel() instanceof VisualTemplate)) {
            super.mousePressed(e);
        }
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        VisualModel model = editor.getModel();
        if (!(model.getCurrentLevel() instanceof VisualTemplate)) {
            super.drawInScreenSpace(editor, g);
        } else {
            GuiUtils.drawEditorMessage(editor, g, Color.RED, "Templates cannot be created inside other templates.");
        }
    }

}
