package org.apache.geronimo.common;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.management.ObjectName;
import org.apache.geronimo.management.State;

/**
 * Base class for a Container that can be accecpt invocation.
 *
 * @version $Revision: 1.5 $ $Date: 2003/08/22 02:08:41 $
 */
public class AbstractRPCContainer extends AbstractContainer implements RPCContainer {
    // @todo access to these objects must be synchronized
    private final Map plugins = new LinkedHashMap();
    private final Map pluginObjects = new LinkedHashMap();
    private final LinkedList interceptors = new LinkedList();
    private Interceptor firstInterceptor;

    public void postDeregister() {
        plugins.clear();
        pluginObjects.clear();
        interceptors.clear();
        firstInterceptor = null;
        super.postDeregister();
    }

    public final InvocationResult invoke(Invocation invocation) throws Exception {
        if (getStateInstance() != State.RUNNING) {
            throw new IllegalStateException("invoke can only be called after the Container has started");
        }
        return firstInterceptor.invoke(invocation);
    }

    /**
     * Add a Component to this Container.
     *
     * @param component a <code>Component</code> value
     */
    public final void addComponent(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof Interceptor) {
            addInterceptor((Interceptor) component);
            return;
        }

        throw new IllegalStateException("Cannot add component of type " + component.getClass() + " to an RPCContainer");
    }

    /**
     * Add an Interceptor to the end of the Interceptor list.
     *
     * @param interceptor
     */
    public final void addInterceptor(Interceptor interceptor) {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException("Interceptors cannot be added unless the Container is stopped");
        }

        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            interceptors.addLast(interceptor);
        } else {
            Interceptor lastInterceptor = (Interceptor) interceptors.getLast();
            lastInterceptor.setNext(interceptor);
            interceptors.addLast(interceptor);
        }
        if( interceptor instanceof Component)
            super.addComponent((Component) interceptor);
    }

    public final ObjectName getPlugin(String logicalPluginName) {
        return (ObjectName) plugins.get(logicalPluginName);
    }

    public final void putPlugin(String logicalPluginName, ObjectName objectName) {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException(
                    "putPluginObject can only be called while in the stopped state: state="
                    + getState());
        }
        plugins.put(logicalPluginName, objectName);
    }

    /**
     * @deprecated
     * @see org.apache.geronimo.common.RPCContainer#getPluginObject(java.lang.String)
     */
    public final Object getPluginObject(String logicalPluginName) {
        return pluginObjects.get(logicalPluginName);
    }

    /**
     * @deprecated
     * @see org.apache.geronimo.common.RPCContainer#putPluginObject(java.lang.String, java.lang.Object)
     */
    public final void putPluginObject(String logicalPluginName, Object plugin) {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException(
                    "putPluginObject can only be called while in the not-created or destroyed states: state="
                    + getState());
        }
        pluginObjects.put(logicalPluginName, plugin);
    }
}
