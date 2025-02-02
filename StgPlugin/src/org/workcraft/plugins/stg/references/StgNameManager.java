package org.workcraft.plugins.stg.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.DefaultNameManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.types.ListMap;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;

public class StgNameManager extends DefaultNameManager {

    private final InstanceManager instancedNameManager = new InstanceManager();
    private final ListMap<String, SignalTransition> signalTransitions = new ListMap<>();
    private final ListMap<String, DummyTransition> dummyTransitions = new ListMap<>();

    @Override
    public String getPrefix(Node node) {
        if (node instanceof SignalTransition) {
            switch (((SignalTransition) node).getSignalType()) {
            case INPUT: return "in";
            case OUTPUT: return "out";
            case INTERNAL: return "sig";
            }
        }
        return super.getPrefix(node);
    }

    public int getInstanceNumber(Node node) {
        return instancedNameManager.getInstance(node).getSecond();
    }

    public void setInstanceNumber(Node node, int number) {
        instancedNameManager.assign(node, number);
    }

    private void renameSignalTransition(SignalTransition t, String signalName) {
        signalTransitions.remove(t.getSignalName(), t);
        t.setSignalName(signalName);
        signalTransitions.put(t.getSignalName(), t);
    }

    private void renameDummyTransition(DummyTransition t, String name) {
        dummyTransitions.remove(t.getName(), t);
        t.setName(name);
        dummyTransitions.put(t.getName(), t);
    }

    private void setSignalTransitionName(SignalTransition st, String name, boolean forceInstance) {
        String signalName = st.getSignalName();
        SignalTransition.Direction direction = st.getDirection();
        Integer instance = 0;
        if (Identifier.isName(name)) {
            signalName = name;
        } else {
            final Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(name);
            if (r == null) {
                throw new ArgumentException("Name '" + name + "' is not a valid signal transition label.");
            }
            signalName = r.getFirst();
            direction = r.getSecond();
            instance = r.getThird();
        }
        if (!signalTransitions.get(signalName).isEmpty() || isUnusedName(signalName) || renameOccupantIfDifferent(st, signalName)) {
            instancedNameManager.assign(st, Pair.of(signalName + direction, instance), forceInstance);
            st.setDirection(direction);
            renameSignalTransition(st, signalName);
        }
    }

    private void setDummyTransitionName(DummyTransition dt, String name, boolean forceInstance) {
        final Pair<String, Integer> r = LabelParser.parseDummyTransition(name);
        if (r == null) {
            throw new ArgumentException("Name '" + name + "' is not a valid dummy label.");
        }
        String dummyName = r.getFirst();
        if (!dummyTransitions.get(dummyName).isEmpty() || isUnusedName(dummyName) || renameOccupantIfDifferent(dt, dummyName)) {
            instancedNameManager.assign(dt, r, forceInstance);
            renameDummyTransition(dt, dummyName);
        }
    }

    private void setPlaceName(StgPlace p, String name) {
        if (!p.isImplicit()) {
            if (isUnusedName(name) || renameOccupantIfDifferent(p, name)) {
                super.setName(p, name);
            }
        }
    }

    private boolean renameOccupantIfDifferent(Node node, String name) {
        Node occupant = getNode(name);
        if (occupant != node) {
            if (!(occupant instanceof StgPlace)) {
                throw new ArgumentException("Name '" + name + "' is unavailable.");
            }
            String derivedName = getDerivedName(occupant, name);
            String msg = "The name '" + name + "' is already taken by a place.\n" +
                    "Rename that place to '" + derivedName + "' and continue?";
            if (!DialogUtils.showConfirmWarning(msg)) {
                return false;
            }
            setName(occupant, derivedName);
        }
        return true;
    }

    public void setName(Node node, String name, boolean forceInstance) {
        if (node instanceof StgPlace) {
            setPlaceName((StgPlace) node, name);
        } else if (node instanceof SignalTransition) {
            setSignalTransitionName((SignalTransition) node, name, forceInstance);
        } else if (node instanceof DummyTransition) {
            setDummyTransitionName((DummyTransition) node, name, forceInstance);
        } else {
            super.setName(node, name);
        }
    }

    @Override
    public void setName(Node node, String s) {
        setName(node, s, false);
    }

    @Override
    public String getName(Node node) {
        String result = null;
        if ((node instanceof StgPlace) && ((StgPlace) node).isImplicit()) {
            // Skip implicit places.
        } else if (node instanceof NamedTransition) {
            Pair<String, Integer> instance = instancedNameManager.getInstance(node);
            if (instance != null) {
                if (instance.getSecond().equals(0)) {
                    result = instance.getFirst();
                } else {
                    result = instance.getFirst() + "/" + instance.getSecond();
                }
            }
        } else {
            result = super.getName(node);
        }
        return result;
    }

    @Override
    public boolean isNamed(Node node) {
        return super.isNamed(node)
                || (instancedNameManager.getInstance(node) != null);
    }

    @Override
    public boolean isUnusedName(String name) {
        return super.isUnusedName(name)
            && !instancedNameManager.containsGenerator(name + "-")
            && !instancedNameManager.containsGenerator(name + "+")
            && !instancedNameManager.containsGenerator(name + "~")
            && !instancedNameManager.containsGenerator(name);
    }

    @Override
    public Node getNode(String name) {
        Node result = null;
        Pair<String, Integer> instancedName = LabelParser.parseInstancedTransition(name);
        if (instancedName != null) {
            if (instancedName.getSecond() == null) {
                instancedName = Pair.of(instancedName.getFirst(), 0);
            }
            result = instancedNameManager.getObject(instancedName);
        }
        if (result == null) {
            result = super.getNode(name);
        }
        return result;
    }

    @Override
    public void remove(Node node) {
        super.remove(node);
        if (instancedNameManager.getInstance(node) != null) {
            instancedNameManager.remove(node);
        }
    }

    private Signal.Type getSignalType(String signalName) {
        for (SignalTransition st: signalTransitions.get(signalName)) {
            return st.getSignalType();
        }
        return null;
    }

    private boolean isSignalName(String name) {
        return !signalTransitions.get(name).isEmpty();
    }

    private boolean isDummyName(String name) {
        return !dummyTransitions.get(name).isEmpty();
    }

    private boolean isGoodSignalName(String name, Signal.Type type) {
        boolean result = true;
        if (super.getNode(name) != null) {
            result = false;
        } else if (isDummyName(name)) {
            result = false;
        } else if (isSignalName(name)) {
            Signal.Type expectedType = getSignalType(name);
            if ((expectedType != null) && !expectedType.equals(type)) {
                result = false;
            }
        }
        return result;
    }

    private boolean isGoodDummyName(String name) {
        boolean result = true;
        if (super.getNode(name) != null) {
            result = false;
        } else if (isSignalName(name)) {
            result = false;
        } else if (isDummyName(name)) {
            result = false;
        }
        return result;
    }

    private void setDeaultSignalTransitionNameIfUnnamed(final SignalTransition st) {
        if (!instancedNameManager.contains(st)) {
            String prefix = getPrefix(st);
            Integer count = getPrefixCount(prefix);
            String name = prefix;
            if (count > 0) {
                name = prefix + count;
            }
            while (!isGoodSignalName(name, st.getSignalType())) {
                name = prefix + (++count);
            }
            setPrefixCount(prefix, count);
            st.setSignalName(name);
            signalTransitions.put(name, st);
            if (instancedNameManager.getInstance(st) == null) {
                instancedNameManager.assign(st);
            }
        }
    }

    private void setDefaultDummyTransitionNameIfUnnamed(final DummyTransition dt) {
        if (!instancedNameManager.contains(dt)) {
            String prefix = getPrefix(dt);
            Integer count = getPrefixCount(prefix);
            String name;
            do {
                name = prefix + (count++);
            } while (!isGoodDummyName(name));
            dt.setName(name);
            dummyTransitions.put(name, dt);
            if (instancedNameManager.getInstance(dt) == null) {
                instancedNameManager.assign(dt);
            }
        }
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if ((node instanceof StgPlace) && ((StgPlace) node).isImplicit()) {
            // Skip implicit places.
        } else if (node instanceof SignalTransition) {
            setDeaultSignalTransitionNameIfUnnamed((SignalTransition) node);
        } else if (node instanceof DummyTransition) {
            setDefaultDummyTransitionNameIfUnnamed((DummyTransition) node);
        } else {
            super.setDefaultNameIfUnnamed(node);
        }
    }

    @Override
    public String getDerivedName(Node node, String candidate) {
        if (node instanceof SignalTransition) {
            return candidate;
        }
        if (node instanceof DummyTransition) {
            Pair<String, Integer> r = LabelParser.parseDummyTransition(candidate);
            candidate = r.getFirst();
        }
        return super.getDerivedName(node, candidate);
    }

}
