package org.workcraft.plugins.nta.interop;

import org.workcraft.interop.AbstractSerialiseExporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.nta.serialisation.UppaalSerialiser;

public class UppaalExporter extends AbstractSerialiseExporter {

    private final UppaalSerialiser serialiser = new UppaalSerialiser();

    @Override
    public Format getFormat() {
        return UppaalFormat.getInstance();
    }

    @Override
    public UppaalSerialiser getSerialiser() {
        return serialiser;
    }

}
