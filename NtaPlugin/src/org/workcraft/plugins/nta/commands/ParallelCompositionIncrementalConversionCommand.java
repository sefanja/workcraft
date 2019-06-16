package org.workcraft.plugins.nta.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.nta.Nta;
import org.workcraft.plugins.nta.NtaDescriptor;
import org.workcraft.plugins.nta.VisualNta;
import org.workcraft.plugins.nta.converters.ParallelCompositionIncrementalConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ParallelCompositionIncrementalConversionCommand extends AbstractConversionCommand {

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualNta visualNta = me.getAs(VisualNta.class);
        ParallelCompositionIncrementalConverter converter = new ParallelCompositionIncrementalConverter(visualNta, new Nta());
        return new ModelEntry(new NtaDescriptor(), converter.getDstModel());
    }

    @Override
    public String getDisplayName() {
        return "Parallel Composition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualNta.class);
    }
}
