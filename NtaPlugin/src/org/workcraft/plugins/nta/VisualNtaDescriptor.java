package org.workcraft.plugins.nta;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualNtaDescriptor implements VisualModelDescriptor {
    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, Nta.class, VisualNta.class.getSimpleName());
        return new VisualNta((Nta) mathModel);
    }

}
