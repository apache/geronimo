package org.apache.geronimo.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.apache.geronimo.naming.java.RootContext;

public abstract class AbstractURLContextFactory implements ObjectFactory {
    public String urlScheme;

    public AbstractURLContextFactory(String urlScheme) {
        this.urlScheme = urlScheme + ":";
    }

    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        if (o == null) {
            return getContext();
        }
        if (o instanceof String) {
            return getContext().lookup((String) o);
        }
        if (o instanceof String[]) {
            for (String s : (String[]) o) {
                if (s.startsWith(urlScheme)) {
                    return getContext().lookup(s);
                }
            }
        }
        throw new NamingException("Could not locate a way to look up " + o + " in url context for " + urlScheme);
    }

    protected abstract Context getContext() throws NamingException;
}