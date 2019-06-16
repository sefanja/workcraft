package org.workcraft.plugins.nta.converters;

import com.google.common.collect.Sets;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.utils.LocationComposer;
import org.workcraft.plugins.nta.utils.TransitionComposer;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ParallelCompositionConverter {

    private final Nta srcModel;
    private final Nta dstModel;

    private final TransitionComposer transitionComposer;
    private final Set<Transition> dstTransitions;
    private final Map<Location, Set<Location>> dstLocations;

    private final Set<Location> todo;
    private final Set<Location> done;

    public ParallelCompositionConverter(VisualNta srcModel, Nta dstModel) {

        // initialise before composing
        InstantiationConverter instantiationConverter = new InstantiationConverter(srcModel, new VisualNta(new Nta()));
        this.srcModel = instantiationConverter.getDstModel().getReferencedNta();
        this.dstModel = dstModel;

        Nta instantiatedNta = (Nta) instantiationConverter.getDstModel().getMathModel();
        transitionComposer = new TransitionComposer(instantiatedNta);

        dstTransitions = new HashSet<>();
        dstLocations = new HashMap<>();

        todo = new HashSet<>();
        done = new HashSet<>();

        addInitialDstLocation(instantiatedNta);

        while (!todo.isEmpty()) {
            Location nextDstLocation = todo.stream().findAny().get();
            todo.remove(nextDstLocation);
            done.add(nextDstLocation);

            Set<Transition> nonSynchronisedTransitions = getOutgoingNonSynchronisedTransitions(nextDstLocation);
            processDstTransitions(nonSynchronisedTransitions);

            Set<Transition> synchronisedTransitions = getOutgoingSynchronisedTransitions(nextDstLocation);
            processDstTransitions(synchronisedTransitions);
        }

        populateDstModel();
    }

    private void addInitialDstLocation(Nta instantiatedNta) {
        Set<Location> initialSrcLocations = instantiatedNta.getAllLocations().stream().filter(Location::isInitial).collect(toSet());
        Location initialDstLocation = LocationComposer.composeLocation(initialSrcLocations);
        todo.add(initialDstLocation);
        dstLocations.put(initialDstLocation, initialSrcLocations);
    }

    private void populateDstModel() {
        Template template = new Template();
        dstModel.add(template);
        dstModel.setName(template, "ParallelComposition");

        template.add(dstTransitions);

        Set<Location> locations = dstLocations.keySet();
        template.add(locations);
        for (Location location : locations) {
            String name = generateDstLocationName(location);
            dstModel.setName(location, name);
        }
    }

    private void processDstTransitions(Set<Transition> dstTransitions) {
        this.dstTransitions.addAll(dstTransitions);
        for (Transition dstTransition : dstTransitions) {
            Location dstSecondLocation = dstTransition.getSecond();
            if (!done.contains(dstSecondLocation)) {
                todo.add(dstSecondLocation);
            }
        }
    }

    private Set<Transition> getOutgoingNonSynchronisedTransitions(Location dstFirstLocation) {
        Set<Transition> dstTransitions = new HashSet<>();
        Set<Location> srcFirstLocations = getSrcLocations(dstFirstLocation);

        Set<Transition> nonSynchronisedSrcTransitions = Sets.difference(
                new HashSet<>(srcModel.getAllTransitions()),
                transitionComposer.getSynchronisedTransitions());
        Set<Transition> outgoingSrcTransitions = nonSynchronisedSrcTransitions.stream()
                .filter(t -> srcFirstLocations.contains(t.getFirst())).collect(toSet());

        for (Transition srcTransition : outgoingSrcTransitions) {
            Set<Location> srcSecondLocations = getSecondLocations(srcFirstLocations, srcTransition);
            Location dstSecondLocation = findOrCreateDstSecondLocation(srcSecondLocations);
            Transition dstTransition = new Transition(srcTransition, dstFirstLocation, dstSecondLocation);
            dstTransitions.add(dstTransition);
        }

        return dstTransitions;
    }

    private Set<Transition> getOutgoingSynchronisedTransitions(Location dstFirstLocation) {
        Set<Transition> dstTransitions = new HashSet<>();
        Set<Location> srcFirstLocations = getSrcLocations(dstFirstLocation);

        Collection<Transition> srcSendersHavingReceivers = transitionComposer.getSendersHavingReceivers();
        Set<Transition> srcOutgoingSenders = srcSendersHavingReceivers.stream()
                .filter(t -> srcFirstLocations.contains(t.getFirst())).collect(toSet());

        for (Transition srcSender : srcOutgoingSenders) {
            Collection<Transition> srcReceiversHavingSenders = transitionComposer.getReceiversHavingSenders(srcSender);
            Set<Transition> srcOutgoingReceivers = srcReceiversHavingSenders.stream()
                    .filter(t -> srcFirstLocations.contains(t.getFirst())).collect(toSet());
            for (Transition srcReceiver : srcOutgoingReceivers) {
                Set<Location> srcSecondLocations = getSecondLocations(srcFirstLocations, srcSender, srcReceiver);
                Location dstSecondLocation = findOrCreateDstSecondLocation(srcSecondLocations);
                Transition dstTransition = TransitionComposer.composeTransition(dstFirstLocation, dstSecondLocation, srcSender, srcReceiver);
                dstTransitions.add(dstTransition);
            }
        }

        return dstTransitions;
    }

    /**
     * Gets the set of locations that we arrive at when starting at the `firstLocations` and taking the `transitions`.
     */
    private Set<Location> getSecondLocations(Set<Location> firstLocations, Transition...transitions) {
        Set<Location> secondLocations = new HashSet<>(firstLocations);
        for (Transition transition : transitions) {
            secondLocations.remove(transition.getFirst());
            secondLocations.add(transition.getSecond());
        }
        return  secondLocations;
    }

    private Location findOrCreateDstSecondLocation(Set<Location> srcSecondLocations) {
        Location dstSecondLocation;
        if (dstLocations.containsValue(srcSecondLocations)) {
            dstSecondLocation = getKeyByValue(dstLocations, srcSecondLocations);
        } else {
            dstSecondLocation = LocationComposer.composeLocation(srcSecondLocations);
            dstLocations.put(dstSecondLocation, srcSecondLocations);
        }
        return dstSecondLocation;
    }

    private String generateDstLocationName(Location dstLocation) {
        return LocationComposer.composeName(getSrcLocations(dstLocation), srcModel);
    }

    private Set<Location> getSrcLocations(Location dstLocation) {
        return dstLocations.get(dstLocation);
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Nta getDstModel() {
        return dstModel;
    }

}
