package org.workcraft.plugins.nta.utils;

import org.workcraft.plugins.nta.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSolverExportUtils {

    private static final Pattern variablePattern = Pattern.compile("([_A-Za-z][_A-Za-z0-9]*)\\s*\\W?=");

    public static String getResets(String assignments) {
        if (assignments == null) {
            return "";
        }

        Pattern pattern = Pattern.compile("\\b([a-zA-Z]\\w*)\\s*:?=\\s*0"); // e.g. x1 := 0
        Matcher matcher = pattern.matcher(assignments);
        ArrayList<String> resets = new ArrayList<>();
        while(matcher.find()) {
            resets.add(matcher.group(1));
        }
        if (resets.isEmpty()) {
            return "";
        } else {
            return "{" + String.join(",", resets) + "}";
        }
    }

    public static Collection<String> getClocks(Nta nta) {
        Set<String> clocks = new HashSet<>();
        for (Location location : nta.getLocations()) {
            addClocks(clocks, location.getInvariant());
        }
        for (Transition transition : nta.getTransitions()) {
            addClocks(clocks, transition.getAssignments());
            addClocks(clocks, transition.getGuard());
        }
        return clocks;
    }

    private static void addClocks(Set<String> clocks, String expression) {
        // all variables are assumed to be clocks
        if (expression != null) {
            Matcher matcher = variablePattern.matcher(expression);
            while (matcher.find()) {
                clocks.add(matcher.group(1));
            }
        }
    }

    public static String replaceNewlines(String text) {
        if (text == null) {
            return null;
        }
        return text.replace(
                '|', // Workcraft's newline character
                '\n' // actual newline character
        );
    }

    public static String removeNewlines(String text) {
        if (text == null) {
            return null;
        }
        return text.replace('\n', ' ');
    }

}
