package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.AlgebraExportDialog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class AlgebraExpressionFromGraphsCommand implements Command {

    private static final String DIALOG_SAVE_FILE = "Save file";
    private static final String DIALOG_EXPRESSION_EXPORT_ERROR = "Expression export error";

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getSection() {
        return "! Algebra";
    }

    @Override
    public String getDisplayName() {
        return "Get expression from graphs (selected or all)";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final GraphEditorPanel editor = mainWindow.getCurrentEditor();
        final Toolbox toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

        VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        String exp = CpogParsingTool.getExpressionFromGraph(visualCpog);
        if (exp == "") {
            return;
        }
        AlgebraExportDialog dialog = new AlgebraExportDialog();
        if (dialog.reveal()) {
            if (dialog.getPaste()) {
                tool.setExpressionText(exp);
                return;
            }
            if (dialog.getExport()) {
                String filePath = dialog.getFilePath();
                if (filePath.compareTo(" ") == 0 || filePath == "") {
                    DialogUtils.showError("No export file has been given", DIALOG_EXPRESSION_EXPORT_ERROR);
                    return;
                }
                File file = new File(filePath);
                if (file.exists()) {
                    String msg = "The file '" + file.getName() + "' already exists.\n" + "Overwrite it?";
                    if (DialogUtils.showConfirmWarning(msg, DIALOG_SAVE_FILE, false)) {
                        return;
                    }
                }
                PrintStream expressions;
                try {
                    expressions = new PrintStream(file);
                    expressions.print(exp);
                    expressions.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                DialogUtils.showError("No export selection was made", DIALOG_EXPRESSION_EXPORT_ERROR);
            }
        }
    }

}
