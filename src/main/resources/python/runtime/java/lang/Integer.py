class Integer(object):
    def __init__(self, value):
        self._value = value
    
    def equals__Object__boolean(self, other):
        # TODO: test this properly
        return isinstance(other, Integer) and other._value == self._value

    def __str__(self):
        return str(self._value)

    def __repr__(self):
        return str(self)