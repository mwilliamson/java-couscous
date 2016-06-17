from ..._couscous import couscous_type, signature, Array
from .Iterator import Iterator
from .List import List


@couscous_type
class Arrays(object):
    @staticmethod
    @signature(Array, List)
    def asList(array):
        return ListAdapter(array)


@couscous_type
class ListAdapter(object):
    def __init__(self, elements):
        self._elements = elements
    
    @signature(Iterator)
    def iterator(self):
        return ListIterator(self._elements)


@couscous_type
class ListIterator(object):
    def __init__(self, elements):
        self._elements = elements
        self._next_index = 0
    
    @signature(bool)
    def hasNext(self):
        return self._next_index < len(self._elements)
    
    @signature(Iterator.T)
    def next(self):
        value = self._elements[self._next_index]
        self._next_index += 1
        return value
