package org.apache.geronimo.common;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import javax.management.ObjectName;
/**
 * AbstractRPCContainer.java
 *
 * Base class for a Container that can be remotely invoked.
 * 
 *
 * 
 * @version $Revision: 1.1 $ $Date: 2003/08/15 14:11:26 $
 */
public class AbstractRPCContainer
	extends AbstractContainer
	implements RPCContainer {

	protected final Map plugins = new LinkedHashMap();
	protected final Map pluginObjects = new LinkedHashMap();
	protected final LinkedList interceptors = new LinkedList();
	// for efficency keep a reference to the first interceptor
	protected Interceptor firstInterceptor;

	/**
	 * Begin the invocation chain.
	 *
	 * @todo Check that invoke() is illegal unless Container has started
	 * @param invocation 
	 * @return InvocationResult
	 * @exception Exception if an error occurs
	 */
	public InvocationResult invoke(Invocation invocation) throws Exception {

		if (getStateInstance() != State.RUNNING)
			throw new IllegalStateException("invoke can only be called after the Container has started");

		return firstInterceptor.invoke(invocation);
	}

	/**
	 * Add a Component to this Container.
	 *
	 * @param component a <code>Component</code> value
	 */
	public void addComponent(Component component) {
		if (component == null)
			return;

		if (component instanceof Interceptor) {
			addInterceptor((Interceptor) component);
			return;
		}

		//Is there a type for Plugins?

		throw new IllegalStateException(
			"Cannot add component of type "
				+ component.getClass()
				+ " to an RPCContainer");
	}

	/**
	 * Add an Interceptor to the end of the Interceptor list.
	 * 
	 * @todo Can interceptors be added after the Container is started?
	 * @param interceptor 
	 */
	public void addInterceptor(Interceptor interceptor) {

		if (getStateInstance() != State.STOPPED)
			throw new IllegalStateException("Interceptors cannot be added unless the Container is stopped");

		if (firstInterceptor == null) {
			firstInterceptor = interceptor;
			interceptors.addLast(interceptor);
		} else {
			Interceptor lastInterceptor = (Interceptor) interceptors.getLast();
			lastInterceptor.setNext(interceptor);
			interceptors.addLast(interceptor);
		}

		super.addComponent(interceptor);
	}

	public ObjectName getPlugin(String logicalPluginName) {
		return (ObjectName) plugins.get(logicalPluginName);
	}

	public void putPlugin(String logicalPluginName, ObjectName objectName) {
		if (getStateInstance() != State.STOPPED) {
			throw new IllegalStateException(
				"putPluginObject can only be called while in the stopped state: state="
					+ getState());
		}
		plugins.put(logicalPluginName, objectName);
	}

	public Object getPluginObject(String logicalPluginName) {
		return pluginObjects.get(logicalPluginName);
	}

	public void putPluginObject(String logicalPluginName, Object plugin) {
		if (getStateInstance() != State.STOPPED) {
			throw new IllegalStateException(
				"putPluginObject can only be called while in the not-created or destroyed states: state="
					+ getState());
		}
		pluginObjects.put(logicalPluginName, plugin);
	}



	/**
	 * Start the Container
	 * The Interceptors will be handled by 
	 * @exception Exception if an error occurs
	 */
	public void doStart() throws Exception {
		// Start all the Plugins in forward insertion order
		for (Iterator iterator = pluginObjects.values().iterator();iterator.hasNext();) {
			Object object = iterator.next();
			// TODO Start the plugin - are these Components also maybe they should just be StateManageable
		}
	}

	/**
	 * Stop the container
	 *
	 */
	public void doStop() {
		// Stop all the plugins in reverse insertion order
		LinkedList list = new LinkedList();
		for (Iterator iterator = pluginObjects.values().iterator();iterator.hasNext();) {
			Object object = iterator.next();
			//TODO work out what has to be done to stop a Plugin
		}
	}

} // AbstractRPCContainer
