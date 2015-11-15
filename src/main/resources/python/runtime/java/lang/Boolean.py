class Boolean(object):
    def __init__(self, value):
        self._value = value

    def equals(self, other):
        # TODO: test this properly
        return isinstance(other, Boolean) and other._value == self._value

    def __str__(self):
        return str(self._value)

    def __repr__(self):
        return str(self)