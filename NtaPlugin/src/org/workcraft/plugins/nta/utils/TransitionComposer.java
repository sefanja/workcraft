package org.workcraft.plugins.nta.utils;

import com.google.common.collect.Sets;
import org.workcraft.plugins.nta.Location;
import org.workcraft.plugins.nta.Nta;
import org.workcraft.plugins.nta.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class TransitionComposer {

    private final Map<Transition, Collection<Transition>> senderToReceiversMap;

    public TransitionComposer(Nta nta) {
        senderToReceiversMap = new HashMap<>();

        Collection<Transition> senders = new ArrayList<>();
        for (Transition transition : nta.getTransitions()) {
            if (transition.isSender()) {
                senders.add(transition);
            }
        }

        Collection<Transition> receivers = new ArrayList<>();
        for (Transition transition : nta.getTransitions()) {
            if (transition.isReceiver()) {
                receivers.add(transition);
            }
        }

        for (Transition sender : senders) {
            ArrayList<Transition> matchingReceivers = new ArrayList<>();
            for (Transition receiver : receivers) {
                if (sender.getSynchronisationName().equals(receiver.getSynchronisationName())
                        && sender.getTemplate() != receiver.getTemplate()) {
                    matchingReceivers.add(receiver);
                }
            }
            if (!matchingReceivers.isEmpty()) {
                senderToReceiversMap.put(sender, matchingReceivers);
            }
        }
    }

    public Collection<Transition> getSendersHavingReceivers() {
        return senderToReceiversMap.keySet();
    }

    public Collection<Transition> getReceiversHavingSenders(Transition sender) {
        return senderToReceiversMap.get(sender);
    }

    public Set<Transition> getSynchronisedTransitions() {
        Set<Transition> senders = senderToReceiversMap.keySet();
        Set<Transition> receivers = new HashSet<>();
        for (Transition sender : senders) {
            receivers.addAll(senderToReceiversMap.get(sender));
        }
        return Sets.union(senders, receivers);
    }

    public static Transition composeTransition(
            Location first, Location second, Transition sender, Transition receiver) {
        Transition compositeTransition = new Transition(first, second);
        compositeTransition.setAssignments(composeAssignments(sender, receiver));
        compositeTransition.setGuard(composeGuard(sender, receiver));
        compositeTransition.setSelects(composeSelects(sender, receiver));
        compositeTransition.setSynchronisation(composeSynchronisation(sender, receiver));
        return compositeTransition;
    }

    public static String composeAssignments(Transition...transitions) {
        return Arrays.stream(transitions).map(Transition::getAssignments)
                .filter(a -> a != null && !a.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public static String composeGuard(Transition...transitions) {
        return Arrays.stream(transitions).map(Transition::getGuard)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.joining(" && "));
    }

    public static String composeSelects(Transition...transitions) {
        return Arrays.stream(transitions).map(Transition::getSelects)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public static String composeSynchronisation(Transition...transitions) {
        return transitions[0].getSynchronisationName(); // by definition, all names should be equal
    }

}
