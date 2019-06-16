package org.workcraft.plugins.nta.converters;

import com.google.common.collect.Sets;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.utils.TransitionComposer;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class ParallelCompositionIncrementalConverter {

    private final Nta srcModel;
    private final Nta dstModel;
    private final Template dstTemplate;

    private final Set<Transition> transitions;
    private final Map<Location, Set<Location>> processedLocations;
    private final Map<Location, Set<Location>> tempLocations;
    private final TransitionComposer transitionComposer;

    public ParallelCompositionIncrementalConverter(VisualNta srcModel, Nta dstModel) {

        // initialise before composing
        InstantiationConverter instantiationConverter = new InstantiationConverter(srcModel, new VisualNta(new Nta()));
        this.srcModel = instantiationConverter.getDstModel().getReferencedNta();
        this.dstModel = dstModel;
        dstTemplate = convertTemplates(); // single template in dstModel

        Nta srcNta = (Nta) instantiationConverter.getDstModel().getMathModel();
        transitionComposer = new TransitionComposer(srcNta);

        transitions = new HashSet<>();
        processedLocations = new HashMap<>();
        tempLocations = new HashMap<>();
        Map<Location, Set<Location>> todo = new HashMap<>();

        setInitialLocation(srcNta, todo);

        while (!todo.isEmpty()) {
            Location compositeLocation = todo.keySet().stream().findAny().get();
            Set<Location> componentLocations = todo.get(compositeLocation);
            todo.remove(compositeLocation);

            processedLocations.put(compositeLocation, componentLocations);

            Set<Transition> sendTransitions = findSendTransitions(compositeLocation, componentLocations);
            processTransitions(todo, sendTransitions);

            Set<Transition> sendReceiveTransitions = findSendReceiveTransitions(compositeLocation, componentLocations);
            processTransitions(todo, sendReceiveTransitions);
        }

        fillDestinationModel();
    }

    private void setInitialLocation(Nta srcNta, Map<Location, Set<Location>> todo) {
        Set<Location> initialComponentLocations = srcNta.getAllLocations().stream().filter(Location::isInitial).collect(toSet());
        Location initialCompositeLocation = composeLocation(initialComponentLocations);
        todo.put(initialCompositeLocation, initialComponentLocations);
        tempLocations.put(initialCompositeLocation, initialComponentLocations);
    }

    private void fillDestinationModel() {
        dstTemplate.add(transitions);
        Set<Location> locations = processedLocations.keySet();
        dstTemplate.add(locations);
        for (Location location : locations) {
            String name = getName(location);
            this.dstModel.setName(location, name);
        }
    }

    private void processTransitions(Map<Location, Set<Location>> todo, Set<Transition> sendTransitions) {
        transitions.addAll(sendTransitions);
        for (Transition sendTransition : sendTransitions) {
            if (!processedLocations.containsKey(sendTransition.getSecond())) {
                todo.put(sendTransition.getSecond(), tempLocations.get(sendTransition.getSecond()));
            }
        }
    }

    private Set<Transition> findSendTransitions(Location compositeLocation, Set<Location> componentLocations) {
        Set<Transition> transitions = new HashSet<>();
        Set<Transition> nonSynchronisedTransitions = Sets.difference(
                new HashSet<>(srcModel.getAllTransitions()),
                transitionComposer.getSynchronisedTransitions());
        Set<Transition> srcTransitions = nonSynchronisedTransitions.stream().filter(s -> componentLocations.contains(s.getFirst())).collect(toSet());

        for (Transition srcTransition : srcTransitions) {
            Set<Location> temp = new HashSet<>(componentLocations);
            temp.remove(srcTransition.getFirst());
            temp.add(srcTransition.getSecond());
            Location second = findOrCreateSecondLocation(temp);
            Transition newTransition = new Transition(srcTransition, compositeLocation, second);
            transitions.add(newTransition);
        }
        return transitions;
    }

    private Set<Transition> findSendReceiveTransitions(Location compositeLocation, Set<Location> componentLocations) {
        Set<Transition> transitions = new HashSet<>();
        Collection<Transition> sendersHavingReceivers = transitionComposer.getSendersHavingReceivers();
        Set<Transition> srcSenderTransitions = sendersHavingReceivers.stream().filter(s -> componentLocations.contains(s.getFirst())).collect(toSet());

        for (Transition srcSender : srcSenderTransitions) {
            Collection<Transition> receiversHavingSenders = transitionComposer.getReceiversHavingSenders(srcSender);
            Set<Transition> srcReceiverTransitions = receiversHavingSenders.stream().filter(s -> componentLocations.contains(s.getFirst())).collect(toSet());
            for (Transition srcReceiver : srcReceiverTransitions) {
                Set<Location> temp = new HashSet<>(componentLocations);
                temp.remove(srcSender.getFirst());
                temp.add(srcSender.getSecond());
                temp.remove(srcReceiver.getFirst());
                temp.add(srcReceiver.getSecond());
                Location second = findOrCreateSecondLocation(temp);
                Transition dstTransition = TransitionComposer.composeTransition(compositeLocation, second, srcSender, srcReceiver);
                transitions.add(dstTransition);
            }
        }
        return transitions;
    }

    private Location findOrCreateSecondLocation(Set<Location> temp) {
        Location second;
        if (tempLocations.containsValue(temp)) {
            second = getKeyByValue(tempLocations, temp);
        } else {
            second = composeLocation(temp);
            tempLocations.put(second, temp);
        }
        return second;
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

    private Template convertTemplates() {
        Template dstTemplate = new Template();
        dstModel.add(dstTemplate);
        dstModel.setName(dstTemplate, "ParallelComposition");
        return dstTemplate;
    }

    public Nta getDstModel() {
        return dstModel;
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getName(Location compositeLocation) {
        return composeName(getComponentLocations(compositeLocation));
    }

    private String composeName(Collection<Location> componentLocations) {
        return componentLocations.stream().sorted(Comparator.comparing(this::getTemplateName))
                .map(l -> getTemplateName(l) + "_" + srcModel.getName(l)).collect(Collectors.joining("__"));
    }

    private String getTemplateName(Location location) {
        return srcModel.getName(location.getTemplate());
    }

    private Set<Location> getComponentLocations(Location compositeLocation) {
        return processedLocations.get(compositeLocation);
    }


}
