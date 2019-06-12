package org.workcraft.plugins.nta;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

@VisualClass(VisualTextNote.class)
@IdentifierPrefix(value = "note", isInternal = true)
public class TextNote extends CommentNode {

    public static final String PROPERTY_TYPE = "Type";

    private static final Type defaultType = Type.COMMENT;
    private static final String defaultTimeSolverText = "// example|" +
            "PREDICATE: {X}|" +
            "START: X|" +
            "EQUATIONS: {|" +
            "  1: mu X = x >= 0|" +
            "}";

    private String text = "";
    private Type type = defaultType;

    public enum Type {
        COMMENT("Comment"),
        DECLARATION("Declaration"),
        INSTANTIATION("Instantiation"),
        SYSTEM("System"),
        TIMESOLVER("TimeSolver");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Gets the text of the first text note of `type` inside `container`.
     * Returns `null` if the text note cannot be found.
     */
    public static String findText(Container container, TextNote.Type type) {
        Collection<TextNote> textNotes =
                Hierarchy.getChildrenOfType(container, TextNote.class, n -> n.getType() == type);
        if (textNotes.size() > 0) {
            return textNotes.iterator().next().getText();
        }
        return null;
    }

    public TextNote() {
        super();
    }

    // for making copies
    public TextNote(TextNote textNote) {
        super();
        text = textNote.text;
        type = textNote.type;
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        if (value == null) text = "";
        if (!text.equals(value)) {
            text = value;
            sendNotification(new PropertyChangedEvent(this, VisualTextNote.PROPERTY_LABEL));
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        if (type != value) {
            if (value != defaultType) {
                Container container = Hierarchy.getNearestContainer(this);
                if (container != null) {
                    if (Hierarchy.getChildrenOfType(container, this.getClass(), n -> n.getType() == value).size() > 0) {
                        throw new RuntimeException(
                                "A text note of type " + value.name + " already exists at this level");
                    }
                }
            }
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));

            if (value == Type.TIMESOLVER && getText().isEmpty()) {
                setDefaultTimeSolverText();
            }
        }
    }

    private void setDefaultTimeSolverText() {
        Template template = Hierarchy.getNearestAncestor(this, Template.class);
        if (template == null) {
            setText(defaultTimeSolverText);
        }
    }

}
