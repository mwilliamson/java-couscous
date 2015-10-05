package org.zwobble.couscous.tests.util;

import java.io.File;

import lombok.val;

public class ExtraFiles {
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (val child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
