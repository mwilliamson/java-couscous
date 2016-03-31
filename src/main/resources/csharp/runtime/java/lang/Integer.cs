namespace Couscous.java.lang {
    internal class Integer : Object {
        private readonly int _value;

        internal Integer(int value) {
            _value = value;
        }

        internal override bool equals__Object__boolean(Object other) {
            var integer = other as Integer;
            return integer != null && integer._value == _value;
        }
    }
}
