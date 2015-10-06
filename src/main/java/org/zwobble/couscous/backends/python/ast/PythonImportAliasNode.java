package org.zwobble.couscous.backends.python.ast;

import lombok.Value;

@Value(staticConstructor="pythonImportAlias")
public class PythonImportAliasNode {
    String name;
}
