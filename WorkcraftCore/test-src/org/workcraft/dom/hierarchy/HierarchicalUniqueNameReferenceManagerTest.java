package org.workcraft.dom.hierarchy;

import org.junit.Test;
import org.workcraft.types.Pair;

import java.util.HashMap;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;

public class HierarchicalUniqueNameReferenceManagerTest {

    static HashMap<String, Pair<String, String>> headTails = new HashMap<String, Pair<String, String>>() {
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc", Pair.of("abc", ""));
            put("abc.def", Pair.of("abc", "def"));
            put("abc.def.ghi", Pair.of("abc", "def.ghi"));
            put("abc/123.def.ghi", Pair.of("abc/123", "def.ghi"));
            put("a.b.c+/123", Pair.of("a", "b.c+/123"));
        }
    };

    @Test
    public void testGetReferenceHead() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String head = en.getValue().getFirst();
            String answer = NamespaceHelper.getReferenceHead(reference);
            assertEquals(head, answer);
        }
    }

    @Test
    public void testGetReferenceTail() {
        for (Entry<String, Pair<String, String>> en: headTails.entrySet()) {
            String reference = en.getKey();
            String tail = en.getValue().getSecond();
            String answer = NamespaceHelper.getReferenceTail(reference);
            assertEquals(tail, answer);
        }
    }

    static HashMap<String, String> referencePaths = new HashMap<String, String>() {
        private static final long serialVersionUID = -2931077011392124649L;
        {
            put("abc", "");
            put("abc.def", "abc.");
            put("abc.def.ghi", "abc.def.");
            put("abc.def.ghi/123", "abc.def.");
            put("abc.def.ghi+", "abc.def.");
            put("abc.def.ghi+/123", "abc.def.");
        }
    };

    @Test
    public void testReferencePaths() {
        for (Entry<String, String> en: referencePaths.entrySet()) {
            String reference = en.getKey();
            String path = en.getValue();
            String answer = NamespaceHelper.getReferencePath(reference);
            assertEquals(path, answer);
        }
    }

}
