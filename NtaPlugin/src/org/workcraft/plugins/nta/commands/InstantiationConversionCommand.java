package org.workcraft.plugins.nta.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.nta.Nta;
import org.workcraft.plugins.nta.NtaDescriptor;
import org.workcraft.plugins.nta.VisualNta;
import org.workcraft.plugins.nta.converters.InstantiationConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class InstantiationConversionCommand extends AbstractConversionCommand {

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualNta nta = me.getAs(VisualNta.class);
        InstantiationConverter converter = new InstantiationConverter(nta, new VisualNta(new Nta()));
        return new ModelEntry(new NtaDescriptor(), converter.getDstModel());
    }

    @Override
    public String getDisplayName() {
        return "Instantiation";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualNta.class);
    }
}
