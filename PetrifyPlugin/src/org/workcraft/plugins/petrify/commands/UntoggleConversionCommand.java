package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;

public class UntoggleConversionCommand extends AbstractPetrifyConversionCommand {

    @Override
    public String getDisplayName() {
        return "Untoggle signal transitions [Petrify]";
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        Collection<Mutex> mutexes = getMutexes(we);
        TransformationTask task = new TransformationTask(
                we, "Signal transition untoggle", new String[] {"-untog"}, mutexes);

        boolean hasSignals = hasSignals(we);
        TransformationResultHandler monitor = new TransformationResultHandler(we, !hasSignals, mutexes);

        TaskManager taskManager = Framework.getInstance().getTaskManager();
        taskManager.execute(task, "Petrify signal transition untoggle", monitor);
        return monitor.waitForHandledResult();
    }

}
