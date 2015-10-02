package org.zwobble.couscous;

import org.zwobble.couscous.values.IntegerValue;

public class JavaProject {
    public static MapBackedProject.Builder builder() {
        return MapBackedProject.builder()
            .addClass(IntegerValue.TYPE);
    }
}
