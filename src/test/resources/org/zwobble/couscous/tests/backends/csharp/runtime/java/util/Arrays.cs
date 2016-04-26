namespace Couscous.java.util {
    internal static class Arrays {
        internal static java.lang.Iterable<T> asList<T>(T[] values) {
            return new _ArrayList<T>(values);
        }
    }
    
    internal class _ArrayList<T> : java.lang.Iterable<T> {
        private readonly T[] values;
        
        internal _ArrayList(T[] values) {
            this.values = values;
        }
        
        public java.util.Iterator<T> iterator() {
            return new _ArrayIterator<T>(values);
        }
    }
    
    internal class _ArrayIterator<T> : java.util.Iterator<T> {
        private readonly T[] values;
        private int nextIndex;
        
        internal _ArrayIterator(T[] values) {
            this.values = values;
            this.nextIndex = 0;
        }
        
        public bool hasNext() {
            return nextIndex < values.Length;
        }
        
        public T next() {
            return values[nextIndex++];
        }
    }
}
