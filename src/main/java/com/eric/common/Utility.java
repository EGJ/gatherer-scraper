package com.eric.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utility {
    public static List<String> colors = Collections.unmodifiableList(new ArrayList<>(5) {{
        add("Black");
        add("Blue");
        add("Green");
        add("Red");
        add("White");
    }});
}
