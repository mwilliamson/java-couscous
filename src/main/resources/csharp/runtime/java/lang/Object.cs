namespace Couscous.java.lang {
    internal class Object {
        internal virtual bool equals__java_lang_Object__boolean(Object other) {
            return System.Object.ReferenceEquals(this, other);
        }

        public static implicit operator Object(int value) {
            return new Integer(value);
        }
    }
}
