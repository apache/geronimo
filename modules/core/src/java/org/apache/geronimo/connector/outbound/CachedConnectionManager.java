/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.outbound;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.resource.ResourceException;

/**
 * CachedConnectionManager tracks connections that are in use by 
 * components such as EJB's.  The component must notify the ccm 
 * when a method enters and exits.  On entrance, the ccm will 
 * notify ConnectionManager stacks so the stack can make sure all 
 * connection handles left open from previous method calls are 
 * attached to ManagedConnections of the correct security context, and
 *  the ManagedConnections are enrolled in any current transaction.   
 * On exit, the ccm will notify ConnectionManager stacks of the handles
 * left open, so they may be disassociated if appropriate.
 * In addition, when a UserTransaction is started the ccm will notify 
 * ConnectionManager stacks so the existing ManagedConnections can be 
 * enrolled properly.
 * 
 * @todo make sure tx enlistment on method entry works
 * @todo implement UserTransaction notifications and tx enlistment.
 *
 * @version $VERSION$ $DATE$
 * 
 * */
public class CachedConnectionManager  {

	private final ThreadLocal currentResources = new ThreadLocal();

	private final ThreadLocal currentKeys = new ThreadLocal();

	private final Map keyToResourcesMap = new IdentityHashMap();

	public void enter(Object key, Set unshareableResources)
		throws ResourceException {
		Stack keyStack = (Stack) currentKeys.get();
		Map resources = (Map) currentResources.get();
		Object oldKey = keyStack.peek();
		synchronized (keyToResourcesMap) {
			if (!resources.isEmpty()
				&& !keyToResourcesMap.containsKey(oldKey)) {
				keyToResourcesMap.put(oldKey, resources);
			}
			resources = (Map) keyToResourcesMap.get(key);
		}
		keyStack.push(key);
		if (resources == null) {
			resources = new HashMap();
		}
		currentResources.set(resources);
		for (Iterator i = resources.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			MetaCallConnectionInterceptor mcci =
				(MetaCallConnectionInterceptor) entry.getKey();
			Set connections = (Set) entry.getValue();
			mcci.enter(connections, unshareableResources);
		}
	}

	public void exit(Object key, Set unshareableResources)
		throws ResourceException {
		Stack keyStack = (Stack) currentKeys.get();
		assert key
			== keyStack.pop() : "Did not pop the expected key on method exit";
		Map resources = (Map) currentResources.get();
		for (Iterator i = resources.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			MetaCallConnectionInterceptor mcci =
				(MetaCallConnectionInterceptor) entry.getKey();
			Set connections = (Set) entry.getValue();
			mcci.exit(connections, unshareableResources);
			if (connections.isEmpty()) {
				i.remove();
			}

		}
		Object previousKey = keyStack.peek();
		synchronized (keyToResourcesMap) {
			if (resources.isEmpty()) {
				keyToResourcesMap.remove(key);
			} else {
				keyToResourcesMap.put(key, resources);
			}
			resources = (Map) keyToResourcesMap.get(previousKey);
		}
		currentResources.set(resources);

	}

	public void handleObtained(
		MetaCallConnectionInterceptor mcci,
		ConnectionInfo ci) {
		Map resources = (Map) currentResources.get();
		Set infos = (Set) resources.get(mcci);
		infos.add(ci);
	}

	public void handleReleased(
		MetaCallConnectionInterceptor mcci,
		ConnectionInfo ci) {
		Map resources = (Map) currentResources.get();
		Set infos = (Set) resources.get(mcci);
		//It's not at all clear that an equal ci will be supplied here
		infos.remove(ci);
	}
}