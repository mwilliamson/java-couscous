package org.zwobble.couscous.interpreter;

public final class NoSuchMethod extends InterpreterException {
    private static final long serialVersionUID = 1L;
    private MethodSignature signature;

    public NoSuchMethod(MethodSignature signature) {
        this.signature = signature;
    }

    public MethodSignature getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "NoSuchMethod(" +
            "signature=" + signature +
            ") " + super.toString();
    }
}