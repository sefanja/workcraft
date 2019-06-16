package org.workcraft.plugins.nta.utils;

import org.workcraft.plugins.nta.Location;
import org.workcraft.plugins.nta.Nta;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class LocationComposer {

    public static Location composeLocation(Collection<Location> componentLocations) {
        Location compositeLocation = new Location();
        compositeLocation.setInitial(componentLocations.stream().allMatch(Location::isInitial));
        compositeLocation.setInvariant(composeInvariant(componentLocations));
        return compositeLocation;
    }

    public static String composeName(Collection<Location> componentLocations, Nta nta) {
        return componentLocations.stream().sorted(Comparator.comparing(l -> getTemplateName(l, nta)))
                .map(l -> getTemplateName(l, nta) + "_" + nta.getName(l)).collect(Collectors.joining("__"));
    }

    private static String composeInvariant(Collection<Location> locations) {
        return locations.stream().map(Location::getInvariant)
                .filter(i -> i != null && !i.isEmpty())
                .collect(Collectors.joining(" && "));
    }

    private static String getTemplateName(Location location, Nta nta) {
        return nta.getName(location.getTemplate());
    }

}
