package org.workcraft.plugins.builtin.workspace;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.utils.ImportUtils;
import org.workcraft.workspace.FileHandler;

import javax.swing.*;
import java.io.File;

public class WorkcraftOpen implements FileHandler {

    public boolean accept(File f) {
        final Framework framework = Framework.getInstance();
        if (ImportUtils.chooseBestImporter(framework.getPluginManager(), f) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void execute(File file) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        try {
            framework.loadWork(file);
        } catch (DeserialisationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Import error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public String getDisplayName() {
        return "Open in Workcraft";
    }
}
