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

    @Ignore
    @Test
    public void canDeclareVariable() {
    }

    @Ignore
    @Test
    public void canDeclareVariableAndThenAssignValues() {
    }

    @Ignore
    @Test
    public void whenConditionIsTrueIfStatementExecutesTrueBranch() {
    }

    @Ignore
    @Test
    public void whenConditionIsFalseIfStatementExecutesTrueBranch() {
    }

    @Ignore
    @Test
    public void whileLoopIsExecutedWhileConditionIsTrue() {
    }

    @Ignore
    @Test
    public void returnWillExitWhileLoopEarly() {
    }

    @Override
    protected MethodRunner buildMethodRunner() {
        return new CsharpMethodRunner();
    }
}
