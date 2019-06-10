package org.workcraft.plugins.nta.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class UppaalFormat implements Format {

    private static UppaalFormat instance = null;

    private UppaalFormat() {
    }

    public static UppaalFormat getInstance() {
        if (instance == null) {
            instance = new UppaalFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("9d8ee0fc-57fb-46cc-a2c5-de6c9a65bc0d");
    }

    @Override
    public String getName() {
        return "UPPAAL System";
    }

    @Override
    public String getExtension() {
        return ".xml";
    }

    @Override
    public String getDescription() {
        return "UPPAAL System";
    }
}
