package org.workcraft.plugins.graph.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class ReachabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Graph.class);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Graph graph = WorkspaceUtils.getAs(we, Graph.class);
        HashSet<Vertex> unreachable = checkReachability(graph);
        if (unreachable.isEmpty()) {
            DialogUtils.showInfo("The graph does not have unreachable vertices.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(graph, unreachable, SizeHelper.getWrapLength());
            String msg = "The graph has unreachable vertices:\n" + refStr + "\n\nSelect unreachable vertices?";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualModel visualGraph = we.getModelEntry().getVisualModel();
                SelectionHelper.selectByReferencedComponents(visualGraph, unreachable);
            }
        }
        return unreachable.isEmpty();
    }

    private HashSet<Vertex> checkReachability(final Graph graph) {
        Queue<Vertex> queue = new LinkedList<>();
        for (Vertex vertex: graph.getVertices()) {
            if (graph.getPreset(vertex).isEmpty()) {
                queue.add(vertex);
            }
        }

        HashSet<Vertex> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Vertex vertex = queue.remove();
            if (visited.contains(vertex)) continue;
            visited.add(vertex);
            for (MathNode node: graph.getPostset(vertex)) {
                if ((node instanceof Vertex) && visited.containsAll(graph.getPreset(node))) {
                    queue.add((Vertex) node);
                }
            }
        }

        HashSet<Vertex> unreachable = new HashSet<>(graph.getVertices());
        unreachable.removeAll(visited);
        return unreachable;
    }

}
