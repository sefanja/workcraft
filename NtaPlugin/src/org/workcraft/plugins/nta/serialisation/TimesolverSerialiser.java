package org.workcraft.plugins.nta.serialisation;

import com.google.common.collect.Sets;
import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.converters.InstantiationConverter;
import org.workcraft.plugins.nta.interop.TimesolverFormat;
import org.workcraft.plugins.nta.utils.TransitionComposer;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.workcraft.plugins.nta.utils.TimeSolverExportUtils.*;
import static org.workcraft.plugins.nta.utils.TransitionComposer.*;

public class TimesolverSerialiser implements ModelSerialiser {

    private static final String INDENTATION = "  ";

    class ReferenceResolver implements ReferenceProducer {
        final HashMap<Object, String> refMap = new HashMap<>();

        ReferenceResolver(Nta nta) {
            for (Template template : nta.getTemplates()) {
                refMap.put(template, nta.getName(template));

                for (Location location : template.getLocations()) {
                    refMap.put(location, Integer.toString(location.getNumber()));
                }
            }
        }

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) {
        if (!this.isApplicableTo(model)) {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }

        InstantiationConverter converter = new InstantiationConverter((VisualNta) model, new VisualNta(new Nta()));
        Nta dstNta = converter.getDstModel().getReferencedNta();
        ReferenceResolver resolver = new ReferenceResolver(dstNta);
        PrintWriter writer = new PrintWriter(out);
        write(writer, dstNta, resolver);
        writer.close();
        return resolver;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof VisualNta;
    }

    @Override
    public UUID getFormatUUID() {
        return TimesolverFormat.getInstance().getUuid();
    }

    private void write(PrintWriter out, Nta nta, ReferenceProducer refs) {
        out.write(Info.getGeneratedByText("// TimeSolver file ", "\n\n"));
        writeClocksSection(out, nta);
        writeControlSection(out, nta, refs);
        writePredicateStartEquationsSection(out, nta);
        writeInvariantSection(out, nta, refs);
        writeTransitionsSection(out, nta, refs);
    }

    private void writeClocksSection(PrintWriter out, Nta nta) {
        out.write("CLOCKS: {" + String.join(",", getClocks(nta)) + "}\n");
    }

    private void writeControlSection(PrintWriter out, Nta nta, ReferenceProducer refs) {
        String processes = nta.getTemplates().stream().map(refs::getReference).collect(Collectors.joining(","));
        out.write("CONTROL: {" + processes + "}\n");
    }

    private void writePredicateStartEquationsSection(PrintWriter out, Nta nta) {
        String text = TextNote.findText(nta.getRoot(), TextNote.Type.TIMESOLVER);
        if (text == null) {
            text = "// TimeSolver text note missing from model";
        }
        // surrounding newlines should help the user to recognise his own direct input
        out.write("\n" + replaceNewlines(text) + "\n\n");
    }

    private void writeInvariantSection(PrintWriter out, Nta nta, ReferenceProducer refs) {
        boolean isFirstLine = true;
        for (Template template : nta.getTemplates()) {
            for (Location location : template.getLocations()) {
                String invariant = removeNewlines(location.getInvariant());
                if (invariant != null && !invariant.isEmpty()) {
                    if (isFirstLine) {
                        out.write("INVARIANT:\n");
                        isFirstLine = false;
                    }
                    for (String term : invariant.split("&&")) {
                        out.write(INDENTATION +
                                refs.getReference(template) + " == " + refs.getReference(location)
                                + " -> " + term.trim() + "\n");
                    }
                }
            }
        }
    }

    private void writeTransitionsSection(PrintWriter out, Nta nta, ReferenceProducer refs) {
        out.write("TRANSITIONS:\n");
        TransitionComposer transitionComposer = new TransitionComposer(nta);
        writeNonSynchronisedTransitions(out, nta, transitionComposer, refs);
        writeSynchronisedTransitions(out, transitionComposer, refs);
    }

    private void writeSynchronisedTransitions(PrintWriter out, TransitionComposer transitionComposer, ReferenceProducer refs) {
        for (Transition sender : transitionComposer.getSendersHavingReceivers()) {
            for (Transition receiver : transitionComposer.getReceiversHavingSenders(sender)) {
                String senderTemplateName = refs.getReference(sender.getTemplate());
                String receiverTemplateName = refs.getReference(receiver.getTemplate());
                String guard = composeGuard(sender, receiver);
                String resets = getResets(composeAssignments(sender, receiver));

                out.write(INDENTATION +
                        "(" + senderTemplateName + "==" + refs.getReference(sender.getFirst()) +
                        " && " + receiverTemplateName + "==" + refs.getReference(receiver.getFirst()) +
                        (guard != null && !guard.isEmpty() ? ", " + guard : "") +
                        ")->(" + senderTemplateName + "=" + refs.getReference(sender.getSecond()) +
                        ", " + receiverTemplateName + "=" + refs.getReference(receiver.getSecond()) + ")" +
                        resets +
                        ";\n");
            }
        }
    }

    private void writeNonSynchronisedTransitions(PrintWriter out, Nta nta, TransitionComposer transitionComposer, ReferenceProducer refs) {
        Set<Transition> nonSynchronisedTransitions = Sets.difference(
                new HashSet<>(nta.getTransitions()),
                transitionComposer.getSynchronisedTransitions());
        for (Transition transition : nonSynchronisedTransitions) {
            String templateName = refs.getReference(transition.getTemplate());
            String guard = transition.getGuard();
            String resets = getResets(transition.getAssignments());

            out.write(INDENTATION +
                    "(" + templateName + "==" + refs.getReference(transition.getFirst()) +
                    (guard != null && !guard.isEmpty() ? ", " + guard : "") +
                    ")->(" + templateName + "=" + refs.getReference(transition.getSecond()) + ")" +
                    resets +
                    ";\n");
        }
    }

}
