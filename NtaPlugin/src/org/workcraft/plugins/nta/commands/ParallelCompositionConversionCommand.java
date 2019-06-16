package org.workcraft.plugins.nta.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.nta.Nta;
import org.workcraft.plugins.nta.NtaDescriptor;
import org.workcraft.plugins.nta.VisualNta;
import org.workcraft.plugins.nta.converters.ParallelCompositionConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ParallelCompositionConversionCommand extends AbstractConversionCommand {

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualNta visualNta = me.getAs(VisualNta.class);
        ParallelCompositionConverter converter = new ParallelCompositionConverter(visualNta, new Nta());
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
