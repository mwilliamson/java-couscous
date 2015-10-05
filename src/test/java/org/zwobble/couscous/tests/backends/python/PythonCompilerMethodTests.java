package org.zwobble.couscous.tests.backends.python;

import org.zwobble.couscous.tests.BackendMethodTests;
import org.zwobble.couscous.tests.MethodRunner;

public class PythonCompilerMethodTests extends BackendMethodTests {
    @Override
    protected MethodRunner buildMethodRunner() {
        return new PythonMethodRunner();
    }
}
