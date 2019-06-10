package org.workcraft.plugins.nta.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class TimesolverFormat implements Format {

    private static TimesolverFormat instance = null;

    private TimesolverFormat() {
    }

    public static TimesolverFormat getInstance() {
        if (instance == null) {
            instance = new TimesolverFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("6a761d17-9b08-488e-b9f3-dd5aaaabfe47");
    }

    @Override
    public String getName() {
        return "TimeSolver";
    }

    @Override
    public String getExtension() {
        return ".in";
    }

    @Override
    public String getDescription() {
        return "TimeSolver";
    }

    @Override
    public String getKeyword() {
        return ".in";
    }

}
