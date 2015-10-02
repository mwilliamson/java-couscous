package org.zwobble.couscous;

import org.zwobble.couscous.values.IntegerValue;

public class JavaProject {
    public static MapBackedProject.MapBackedProjectBuilder builder() {
        return MapBackedProject.builder()
            .addClass("java.lang.Integer", IntegerValue.TYPE);
    }
}
