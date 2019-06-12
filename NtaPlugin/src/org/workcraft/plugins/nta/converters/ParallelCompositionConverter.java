package org.workcraft.plugins.nta.converters;

import com.google.common.collect.Sets;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.utils.LocationComposer;
import org.workcraft.plugins.nta.utils.TransitionComposer;

import java.util.*;

public class ParallelCompositionConverter {

    private final Nta srcModel;
    private final Nta dstModel;

    private final Template dstTemplate;
    private final LocationComposer locationComposer;

    public ParallelCompositionConverter(VisualNta srcModel, Nta dstModel) {
        // initialise before composing
        InstantiationConverter instantiationConverter = new InstantiationConverter(srcModel, new VisualNta(new Nta()));
        this.srcModel = instantiationConverter.getDstModel().getReferencedNta();
        this.dstModel = dstModel;
        locationComposer = new LocationComposer(this.srcModel);
        dstTemplate = convertTemplates(); // single template in dstModel
        convertLocations();
        convertTransitions();
        removeUnreachableTransitions();
        removeUnreachableLocations();
    }

    private Template convertTemplates() {
        Template dstTemplate = new Template();
        dstModel.add(dstTemplate);
        dstModel.setName(dstTemplate, "ParallelComposition");
        return dstTemplate;
    }

    private void convertLocations() {
        Collection<Location> dstLocations = locationComposer.getCompositeLocations();
        dstTemplate.add(dstLocations);
        for (Location dstLocation : dstLocations) {
            dstModel.setName(dstLocation, locationComposer.getName(dstLocation));
        }
    }

    private void convertTransitions() {
        TransitionComposer transitionComposer = new TransitionComposer(srcModel);
        convertNonSynchronisedTransitions(transitionComposer);
        convertSynchronisedTransitions(transitionComposer);
    }

    private void convertNonSynchronisedTransitions(TransitionComposer transitionComposer) {
        Set<Transition> nonSynchronisedTransitions = Sets.difference(
                new HashSet<>(srcModel.getTransitions()),
                transitionComposer.getSynchronisedTransitions());
        for (Transition srcTransition : nonSynchronisedTransitions) {
            Location srcFirstLocation = srcTransition.getFirst();
            Location srcSecondLocation = srcTransition.getSecond();

            Collection<Location> dstFirstLocations =
                    locationComposer.getCompositeByComponentLocations(srcFirstLocation);

            for (Location dstFirstLocation : dstFirstLocations) {
                Location dstSecondLocation = locationComposer.findCompositeSecondLocation(
                        dstFirstLocation, srcFirstLocation, srcSecondLocation);

                Transition dstTransition = new Transition(srcTransition, dstFirstLocation, dstSecondLocation);
                dstTemplate.add(dstTransition);
            }
        }
    }

    private void convertSynchronisedTransitions(TransitionComposer transitionComposer) {
        for (Transition srcSender : transitionComposer.getSendersHavingReceivers()) {
            for (Transition srcReceiver : transitionComposer.getReceiversHavingSenders(srcSender)) {
                Set<Location> srcFirstLocations = new HashSet<>();
                srcFirstLocations.add(srcSender.getFirst());
                srcFirstLocations.add(srcReceiver.getFirst());

                Set<Location> srcSecondLocations = new HashSet<>();
                srcSecondLocations.add(srcSender.getSecond());
                srcSecondLocations.add(srcReceiver.getSecond());

                Collection<Location> dstFirstLocations =
                        locationComposer.getCompositeByComponentLocations(srcFirstLocations);

                for (Location dstFirstLocation : dstFirstLocations) {
                    Location dstSecondLocation = locationComposer.findCompositeSecondLocation(
                            dstFirstLocation, srcFirstLocations, srcSecondLocations);

                    Transition dstTransition = TransitionComposer.composeTransition(
                            dstFirstLocation, dstSecondLocation, srcSender, srcReceiver);
                    dstTemplate.add(dstTransition);
                }
            }
        }
    }

    private void removeUnreachableTransitions() {
        Set<Transition> allTransitions = new HashSet<>(dstModel.getTransitions());

        Set<Transition> reachableTransitions = new HashSet<>();
        Location initialLocation = dstModel.getLocations(Location::isInitial).iterator().next();
        findReachableTransitions(initialLocation, reachableTransitions);

        Set<Transition> unreachableTransitions = Sets.difference(allTransitions, reachableTransitions);
        dstModel.remove(unreachableTransitions);
    }

    private void findReachableTransitions(Location location, Set<Transition> reachableTransitions) {
        Collection<Transition> outTransitions = dstModel.getTransitions(t -> t.getFirst().equals(location));
        if (reachableTransitions.containsAll(outTransitions)) {
            return;
        }
        reachableTransitions.addAll(outTransitions);
        outTransitions.forEach(t -> findReachableTransitions(t.getSecond(), reachableTransitions));
    }

    private void removeUnreachableLocations() {
        // unreachable transitions are assumed to have been removed

        Set<Location> allLocations = new HashSet<>(dstModel.getLocations());

        Set<Location> reachableLocations = new HashSet<>();
        for (Transition transition : dstModel.getTransitions()) {
            reachableLocations.add(transition.getFirst());
            reachableLocations.add(transition.getSecond());
        }

        Collection<Location> unreachableLocations = Sets.difference(allLocations, reachableLocations);
        dstModel.remove(unreachableLocations);
    }

    public Nta getDstModel() {
        return dstModel;
    }

}
