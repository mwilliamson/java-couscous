package org.zwobble.couscous.backends.python.ast.visitors;

import org.zwobble.couscous.backends.python.ast.*;

public interface PythonNodeVisitor {
    void visit(PythonIntegerLiteralNode integerLiteral);
    void visit(PythonStringLiteralNode stringLiteral);
    void visit(PythonBooleanLiteralNode booleanLiteral);
    void visit(PythonVariableReferenceNode reference);
    void visit(PythonConditionalExpressionNode conditional);
    void visit(PythonAttributeAccessNode attributeAccess);
    void visit(PythonCallNode call);
    void visit(PythonGetSliceNode getSlice);
    void visit(PythonNotNode notOperation);
    void visit(PythonBinaryOperation operation);

    void visit(PythonExpressionStatement statement);
    void visit(PythonReturnNode pythonReturn);
    void visit(PythonPassNode pass);
    void visit(PythonClassNode pythonClass);
    void visit(PythonFunctionDefinitionNode functionDefinition);
    void visit(PythonAssignmentNode assignment);
    void visit(PythonImportNode importNode);
    void visit(PythonIfStatementNode ifStatement);
    void visit(PythonWhileNode whileLoop);

    void visit(PythonModuleNode module);
}
