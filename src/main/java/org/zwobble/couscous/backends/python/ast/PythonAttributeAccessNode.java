package org.zwobble.couscous.backends.python.ast;

import javax.annotation.Nullable;

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
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonAttributeAccessNode)) return false;
        final PythonAttributeAccessNode other = (PythonAttributeAccessNode)o;
        final java.lang.Object this$left = this.getLeft();
        final java.lang.Object other$left = other.getLeft();
        if (this$left == null ? other$left != null : !this$left.equals(other$left)) return false;
        final java.lang.Object this$attributeName = this.getAttributeName();
        final java.lang.Object other$attributeName = other.getAttributeName();
        if (this$attributeName == null ? other$attributeName != null : !this$attributeName.equals(other$attributeName)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $left = this.getLeft();
        result = result * PRIME + ($left == null ? 43 : $left.hashCode());
        final java.lang.Object $attributeName = this.getAttributeName();
        result = result * PRIME + ($attributeName == null ? 43 : $attributeName.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonAttributeAccessNode(left=" + this.getLeft() + ", attributeName=" + this.getAttributeName() + ")";
    }
}