package org.zwobble.couscous.tests.util;

import java.io.File;

public class ExtraFiles {
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}