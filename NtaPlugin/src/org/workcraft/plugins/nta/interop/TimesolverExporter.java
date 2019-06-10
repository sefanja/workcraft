package org.workcraft.plugins.nta.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.nta.serialisation.TimesolverSerialiser;

public class TimesolverExporter extends AbstractSerialiseExporter {

    private final TimesolverSerialiser serialiser = new TimesolverSerialiser();

    @Override
    public Format getFormat() {
        return TimesolverFormat.getInstance();
    }

    @Override
    public TimesolverSerialiser getSerialiser() {
        return serialiser;
    }

}
