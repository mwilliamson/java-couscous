namespace Couscous.java.lang {
    internal class Integer : Object {
        internal static int valueOf__int(int value) {
            return value;
        }

        private readonly int _value;

        internal Integer(int value) {
            _value = value;
        }

        internal override bool equals__java_lang_Object(Object other) {
            var integer = other as Integer;
            return integer != null && integer._value == _value;
        }
    }
}
