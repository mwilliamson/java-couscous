package org.zwobble.couscous.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zwobble.couscous.util.ExtraIterables.iterable;
import static org.zwobble.couscous.util.ExtraLists.*;

public class FileSet {
    public static FileSet globs(List<String> files) {
        return new FileSet(() -> {
            Set<Path> paths = new HashSet<>();

            for (String file : files) {
                if (file.startsWith("!")) {
                    walkFiles(path(file.substring(1))).forEach(paths::remove);
                } else {
                    walkFiles(path(file)).forEach(paths::add);
                }
            }

            return paths.stream()
                .sorted()
                .iterator();
        }, list());
    }

    private static Iterable<Path> walkFiles(Path root) {
        return iterable(() -> {
            try {
                return Files.walk(root)
                    .filter(path -> path.toFile().isFile());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static Path path(String output) {
        return FileSystems.getDefault().getPath(output);
    }

    public static FileSet directory(Path path) {
        return new FileSet(walkFiles(path), list());
    }

    private final Iterable<Path> files;
    private final List<Predicate<Path>> filters;

    private FileSet(Iterable<Path> files, List<Predicate<Path>> filters) {
        this.files = files;
        this.filters = filters;
    }

    public FileSet filter(Predicate<Path> predicate) {
        return new FileSet(files, cons(predicate, filters));
    }

    public List<Path> files() {
        return eagerFilter(files, file -> Iterables.all(filters, filter -> filter.apply(file)));
    }
}
