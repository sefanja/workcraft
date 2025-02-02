package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public abstract class AbstractTagCommand implements ScriptableCommand<Void> {

    @Override
    public final String getSection() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public boolean isVisibleInMenu() {
        return false;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Collection<Contact> contacts = getFunction().apply(circuit);
        if (contacts.isEmpty()) {
            we.uncaptureMemento();
        } else {
            we.saveMemento();
            ArrayList<String> refs = ReferenceHelper.getReferenceList(circuit, contacts);
            LogUtils.logInfo(LogUtils.getTextWithRefs(getMessage(), refs));
        }

        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            final Toolbox toolbox = framework.getMainWindow().getEditor(we).getToolBox();
            Class<GraphEditorTool> toolClass = getToolClass();
            toolbox.selectTool(toolbox.getToolInstance(toolClass));
        }
        return null;
    }

    abstract Function<Circuit, Collection<Contact>> getFunction();

    abstract String getMessage();

    abstract <T extends GraphEditorTool> Class<T> getToolClass();

}
