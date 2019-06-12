package org.workcraft.plugins.nta;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.nta.commands.InstantiationConversionCommand;
import org.workcraft.plugins.nta.commands.ParallelCompositionConversionCommand;
import org.workcraft.plugins.nta.interop.TimesolverExporter;
import org.workcraft.plugins.nta.interop.UppaalExporter;
import org.workcraft.plugins.nta.interop.UppaalImporter;
import org.workcraft.utils.ScriptableCommandUtils;

public class NtaPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Network of Timed Automata";
    }

    @Override
    public void init() {
        initPluginManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(NtaDescriptor.class);
        pm.registerExporter(TimesolverExporter.class);
        pm.registerExporter(UppaalExporter.class);
        pm.registerImporter(UppaalImporter.class);

        ScriptableCommandUtils.register(InstantiationConversionCommand.class, "convertToInstantiation",
                "instantiate automata templates");
        ScriptableCommandUtils.register(ParallelCompositionConversionCommand.class, "convertToParallelComposition",
                "compute a parallel composition");
    }

}
