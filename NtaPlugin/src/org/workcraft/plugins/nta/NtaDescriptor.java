package org.workcraft.plugins.nta;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class NtaDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Network of Timed Automata";
    }

    @Override
    public MathModel createMathModel() {
        return new Nta();
    }

    @Override
    public VisualModelDescriptor getVisualModelDescriptor() {
        return new VisualNtaDescriptor();
    }

}
