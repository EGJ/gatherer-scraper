package com.eric.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {
    public static final List<String> colors = Collections.unmodifiableList(new ArrayList<>(5) {{
        add("Black");
        add("Blue");
        add("Green");
        add("Red");
        add("White");
    }});
    private static final List<String> excludedWords = Arrays.asList("a", "for", "so", "an", "in", "the", "and", "nor", "to", "at", "of", "up", "but", "on", "yet", "by", "or");

    public static String toTitleCase(String sentence) {
        if (sentence.isBlank()) {
            return null;
        }

        String titleCaseString = Arrays.stream(sentence.toLowerCase().strip().split(" "))
                .map(word -> {
                    if (excludedWords.contains(word)) {
                        return word;
                    } else {
                        return Character.toTitleCase(word.charAt(0)) + word.substring(1);
                    }
                })
                .collect(Collectors.joining(" "));

        return Character.toTitleCase(titleCaseString.charAt(0)) + titleCaseString.substring(1);
    }
}
