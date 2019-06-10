package org.workcraft.plugins.nta.utils;

import com.google.common.collect.Sets;
import org.workcraft.plugins.nta.Location;
import org.workcraft.plugins.nta.Nta;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class LocationComposer {

    private final Map<Location, Set<Location>> compositeToComponentLocations;
    private final Nta nta;

    public LocationComposer(Nta nta) {
        this.nta = nta;

        List<Set<Location>> locationsByTemplate = nta.getTemplates().stream()
                .map(t -> new HashSet<>(t.getLocations())).collect(toList());

        compositeToComponentLocations = new HashMap<>();
        for (Collection<Location> componentLocations : Sets.cartesianProduct(locationsByTemplate)) {
            Location compositeLocation = composeLocation(componentLocations);
            compositeToComponentLocations.put(compositeLocation, new HashSet<>(componentLocations));
        }
    }

    private Location composeLocation(Collection<Location> componentLocations) {
        Location compositeLocation = new Location();
        compositeLocation.setInitial(componentLocations.stream().allMatch(Location::isInitial));
        compositeLocation.setInvariant(composeInvariant(componentLocations));
        return compositeLocation;
    }

    private String composeInvariant(Collection<Location> locations) {
        return locations.stream().map(Location::getInvariant)
                .filter(i -> i != null && !i.isEmpty())
                .collect(Collectors.joining(" && "));
    }

    public String getName(Location compositeLocation) {
        return composeName(getComponentLocations(compositeLocation));
    }

    private String composeName(Collection<Location> componentLocations) {
        return componentLocations.stream().sorted(Comparator.comparing(this::getTemplateName))
                .map(l -> getTemplateName(l) + "_" + nta.getName(l)).collect(Collectors.joining("__"));
    }

    private String getTemplateName(Location location) {
        return nta.getName(location.getTemplate());
    }

    public Collection<Location> getCompositeLocations() {
        return compositeToComponentLocations.keySet();
    }

    public Collection<Location> getCompositeByComponentLocations(Collection<Location> componentLocations) {
        return compositeToComponentLocations.keySet().stream()
                .filter(compositeLocation ->
                        compositeToComponentLocations.get(compositeLocation).containsAll(componentLocations))
                .collect(toList());
    }

    public Collection<Location> getCompositeByComponentLocations(Location ...componentLocations) {
        return getCompositeByComponentLocations(Arrays.asList(componentLocations));
    }

    public Location findCompositeSecondLocation(
            Location compositeFirstLocation,
            Set<Location> componentFirstLocations,
            Set<Location> componentSecondLocations) {

        Set<Location> allComponentFirstLocations = getComponentLocations(compositeFirstLocation);
        Collection<Location> compositeSecondLocations = getCompositeByComponentLocations(componentSecondLocations);

        return compositeSecondLocations.stream().filter(compositeSecondLocation -> {
            Set<Location> allComponentSecondLocations = getComponentLocations(compositeSecondLocation);
            // Only the given component locations, which are involved in the transition, may change when transitioning
            // from the first composite location to the the second.
            return Sets.difference(allComponentFirstLocations, componentFirstLocations)
                    .equals(Sets.difference(allComponentSecondLocations, componentSecondLocations));
        }).findAny().orElseThrow(() -> new IllegalMonitorStateException(
                "If this ever happens, the Cartesian product would be incomplete"));
    }

    public Location findCompositeSecondLocation(
            Location compositeFirstLocation,
            Location componentFirstLocation,
            Location componentSecondLocation) {

        Set<Location> componentFirstLocations = new HashSet<>();
        componentFirstLocations.add(componentFirstLocation);
        Set<Location> componentSecondLocations = new HashSet<>();
        componentSecondLocations.add(componentSecondLocation);

        return findCompositeSecondLocation(compositeFirstLocation, componentFirstLocations, componentSecondLocations);
    }

    private Set<Location> getComponentLocations(Location compositeLocation) {
        return compositeToComponentLocations.get(compositeLocation);
    }

}
