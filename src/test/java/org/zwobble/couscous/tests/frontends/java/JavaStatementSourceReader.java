package org.zwobble.couscous.tests.frontends.java;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.StatementNode;

import java.util.List;

import static org.zwobble.couscous.tests.frontends.java.JavaReading.readClass;
import static org.zwobble.couscous.util.ExtraIterables.only;
import static org.zwobble.couscous.util.ExtraLists.append;
import static org.zwobble.couscous.util.ExtraLists.list;

class JavaStatementSourceReader {
    private final List<String> variables;
    private final String returnType;

    JavaStatementSourceReader() {
        this(list(), "void");
    }

    private JavaStatementSourceReader(List<String> variables, String returnType) {
        this.variables = variables;
        this.returnType = returnType;
    }

    JavaStatementSourceReader returns(String returnType) {
        return new JavaStatementSourceReader(variables, returnType);
    }

    JavaStatementSourceReader addVariable(String name, String type) {
        return new JavaStatementSourceReader(append(variables, type + " " + name), returnType);
    }

    public List<StatementNode> readStatement(String statement) {
        String javaClass = generateMethodSource(variables, returnType, statement);
        ClassNode classNode = readClass(javaClass);
        return only(classNode.getMethods()).getBody().get();

    }

    private String generateMethodSource(List<String> variables, String returnType, String statement) {
        return "public static " + returnType + " main(" + String.join(", ", variables) + ") throws Exception {" +
            statement +
            "}";
    }
}
