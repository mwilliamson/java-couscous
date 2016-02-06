package org.zwobble.couscous.tests.backends.csharp;

import org.zwobble.couscous.tests.BackendMethodTests;
import org.zwobble.couscous.tests.MethodRunner;

public class CsharpCompilerMethodTests extends BackendMethodTests {
    @Override
    protected MethodRunner buildMethodRunner() {
        return new CsharpMethodRunner();
    }
}
