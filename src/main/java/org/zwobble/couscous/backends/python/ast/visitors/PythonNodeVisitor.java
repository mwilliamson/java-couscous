package org.zwobble.couscous.backends.python.ast.visitors;

import org.zwobble.couscous.backends.python.ast.PythonAssignmentNode;
import org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode;
import org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonCallNode;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonConditionalExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonGetSliceNode;
import org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonPassNode;
import org.zwobble.couscous.backends.python.ast.PythonReturnNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode;

public interface PythonNodeVisitor {
    void visit(PythonIntegerLiteralNode integerLiteral);
    void visit(PythonStringLiteralNode stringLiteral);
    void visit(PythonBooleanLiteralNode booleanLiteral);
    void visit(PythonVariableReferenceNode reference);
    void visit(PythonConditionalExpressionNode conditional);
    void visit(PythonAttributeAccessNode attributeAccess);
    void visit(PythonCallNode call);
    void visit(PythonGetSliceNode getSlice);
    
    void visit(PythonReturnNode pythonReturn);
    void visit(PythonPassNode pass);
    void visit(PythonClassNode pythonClass);
    void visit(PythonFunctionDefinitionNode functionDefinition);
    void visit(PythonAssignmentNode assignment);

    void visit(PythonModuleNode module);

}
