package org.zwobble.couscous.backends.python.ast.visitors;

import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonPassNode;
import org.zwobble.couscous.backends.python.ast.PythonReturnNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;

public interface PythonNodeVisitor<T> {
    T visit(PythonIntegerLiteralNode integerLiteral);
    T visit(PythonStringLiteralNode stringLiteral);
    
    T visit(PythonReturnNode pythonReturn);
    T visit(PythonPassNode pass);
    T visit(PythonClassNode pythonClass);
    T visit(PythonFunctionDefinitionNode functionDefinition);

    T visit(PythonModuleNode module);

}
