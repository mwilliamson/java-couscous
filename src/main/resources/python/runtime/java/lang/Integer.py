class Integer(object):
    @staticmethod
    def parseInt(value):
        return int(value)
    
    def __init__(self, value):
        self._value = value
    
    def equals(self, other):
        # TODO: test this properly
        return isinstance(other, Integer) and other._value == self._value
