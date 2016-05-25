class Arrays(object):
    def asList__array__List(array):
        return ListAdapter(array)


class ListAdapter(object):
    def __init__(self, elements):
        self._elements = elements
    
    def iterator__Iterator(self):
        return ListIterator(self._elements)


class ListIterator(object):
    def __init__(self, elements):
        self._elements = elements
        self._next_index = 0
    
    def hasNext__boolean(self):
        return self._next_index < len(self._elements)
    
    def next__java_util_Iterator_T(self):
        value = self._elements[self._next_index]
        self._next_index += 1
        return value
