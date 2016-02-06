class Integer(object):
    @staticmethod
    def parseInt__java_lang_String__int(value):
        return int(value)

    @staticmethod
    def valueOf__int__java_lang_Integer(value):
        return value
    
    def __init__(self, value):
        self._value = value
    
    def equals__java_lang_Object__boolean(self, other):
        # TODO: test this properly
        return isinstance(other, Integer) and other._value == self._value

    def __str__(self):
        return str(self._value)

    def __repr__(self):
        return str(self)