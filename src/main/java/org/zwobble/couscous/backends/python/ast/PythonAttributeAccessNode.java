package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonAttributeAccessNode implements PythonExpressionNode {
    private final PythonExpressionNode left;
    private final String attributeName;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonAttributeAccessNode(final PythonExpressionNode left, final String attributeName) {
        this.left = left;
        this.attributeName = attributeName;
    }
    
    public static PythonAttributeAccessNode pythonAttributeAccess(final PythonExpressionNode left, final String attributeName) {
        return new PythonAttributeAccessNode(left, attributeName);
    }
    
    public PythonExpressionNode getLeft() {
        return this.left;
    }
    
    public String getAttributeName() {
        return this.attributeName;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonAttributeAccessNode(left=" + this.getLeft() + ", attributeName=" + this.getAttributeName() + ")";
    }
}