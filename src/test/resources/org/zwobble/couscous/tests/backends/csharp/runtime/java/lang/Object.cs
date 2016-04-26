namespace Couscous.java.lang {
    internal class Object {
        internal virtual bool equals(Object other) {
            return System.Object.ReferenceEquals(this, other);
        }

        public static implicit operator Object(int value) {
            return new Integer(value);
        }
    }
}
