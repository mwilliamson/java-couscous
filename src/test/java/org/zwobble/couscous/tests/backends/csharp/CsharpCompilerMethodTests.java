package org.zwobble.couscous.tests.backends.csharp;

import org.junit.Ignore;
import org.junit.Test;
import org.zwobble.couscous.tests.BackendMethodTests;
import org.zwobble.couscous.tests.MethodRunner;

public class CsharpCompilerMethodTests extends BackendMethodTests {
    @Ignore
    @Test
    public void methodWithNoStatementsReturnsUnit() {
    }

    @Override
    protected MethodRunner buildMethodRunner() {
        return new CsharpMethodRunner();
    }
}
