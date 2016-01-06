class Integer(object):
    @staticmethod
    def parseInt__java_lang_String(value):
        return int(value)

    @staticmethod
    def valueOf__int(value):
        return value
    
    def __init__(self, value):
        self._value = value
    
    def equals__java_lang_Object(self, other):
        # TODO: test this properly
        return isinstance(other, Integer) and other._value == self._value

    def __str__(self):
        return str(self._value)

    def __repr__(self):
        return str(self)