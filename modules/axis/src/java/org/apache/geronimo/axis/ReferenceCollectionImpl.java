package org.apache.geronimo.axis;

import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ReferenceCollectionImpl implements ReferenceCollection {
    private Set listeners = new HashSet();
    private boolean stopped = false;
    private Map proxies = new HashMap();

    public boolean isStopped() {
        synchronized (this) {
            return stopped;
        }
    }

    public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    public int size() {
        synchronized (this) {
            if (stopped) {
                return 0;
            }
            return proxies.size();
        }
    }

    public boolean isEmpty() {
        synchronized (this) {
            if (stopped) {
                return true;
            }
            return proxies.isEmpty();
        }
    }

    public boolean contains(Object o) {
        synchronized (this) {
            if (stopped) {
                return false;
            }
            return proxies.containsValue(o);
        }
    }

    public Iterator iterator() {
        synchronized (this) {
            if (stopped) {
                return new Iterator() {
                    public boolean hasNext() {
                        return false;
                    }

                    public Object next() {
                        throw new NoSuchElementException();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            return new Iterator() {
                // copy the proxies, so the client can iterate without concurrent modification
                // this is necssary since the client has nothing to synchronize on
                private final Iterator iterator = new ArrayList(proxies.values()).iterator();

                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Object next() {
                    return iterator.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public Object[] toArray() {
        synchronized (this) {
            if (stopped) {
                return new Object[0];
            }
            return proxies.values().toArray();
        }
    }

    public Object[] toArray(Object a[]) {
        synchronized (this) {
            if (stopped) {
                if (a.length > 0) {
                    a[0] = null;
                }
                return a;
            }
            return proxies.values().toArray(a);
        }
    }

    public boolean containsAll(Collection c) {
        synchronized (this) {
            if (stopped) {
                return c.isEmpty();
            }
            return proxies.values().containsAll(c);
        }
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }
}
