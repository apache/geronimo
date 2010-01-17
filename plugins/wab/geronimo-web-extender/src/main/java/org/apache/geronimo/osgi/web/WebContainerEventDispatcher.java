/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.web;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for dispatching Web container lifecycle events to
 * the EventAdmin service, if it's available.
 * 
 * @version $Rev$, $Date$
 */
public class WebContainerEventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebContainerEventDispatcher.class);

    // our service tracker for the EventAdmin service
    private ServiceTracker tracker;
    // the extender bundle we're working on behalf of
    private Bundle extenderBundle;

    public WebContainerEventDispatcher(BundleContext bundleContext) {
        this.extenderBundle = bundleContext.getBundle();

        if (isEventAdminPresent()) {
            // this will track the availability of the EventAdmin service when we need to dispatch
            tracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
            tracker.open();
        }
    }
    
    private boolean isEventAdminPresent() {
        try {
            getClass().getClassLoader().loadClass("org.osgi.service.event.EventAdmin");
            return true;
        } catch (Throwable t) {
            // Ignore, if the EventAdmin package is not available, just don't use it
            LOGGER.debug("EventAdmin package is not available, just don't use it");
            return false;
        }
    }

    /**
     * Dispatch a deploying event
     *
     * @param bundle The bundle we're deploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void deploying(Bundle bundle, String contextPath) {
        dispatch(WebContainerConstants.TOPIC_DEPLOYING, bundle, contextPath);
    }

    /**
     * Dispatch a deployed event
     *
     * @param bundle The bundle we're deploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void deployed(Bundle bundle, String contextPath) {
        dispatch(WebContainerConstants.TOPIC_DEPLOYED, bundle, contextPath);
    }

    /**
     * Dispatch an undeploying event
     *
     * @param bundle The bundle we're undeploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void undeploying(Bundle bundle, String contextPath) {
        dispatch(WebContainerConstants.TOPIC_UNDEPLOYING, bundle, contextPath);
    }

    /**
     * Dispatch an undeployed event
     *
     * @param bundle The bundle we're undeploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void undeployed(Bundle bundle, String contextPath) {
        dispatch(WebContainerConstants.TOPIC_UNDEPLOYED, bundle, contextPath);
    }

    /**
     * Dispatch a FAILED event
     *
     * @param bundle The bundle we're attempting to deploy
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void failed(Bundle bundle, String contextPath, Throwable cause) {
        EventAdmin eventAdmin = getEventAdmin();
        if (eventAdmin == null) {
            return;
        }
        Dictionary<String, Object> props = createDefaultProperties(bundle, contextPath);
        if (cause != null) {
            props.put(EventConstants.EXCEPTION, cause);
        }
        eventAdmin.postEvent(new Event(WebContainerConstants.TOPIC_FAILED, props));
    }
    
    /**
     * Dispatch a FAILED event indicating a contextPath collision
     *
     * @param bundle The bundle we're attempting to deploy
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void collision(Bundle bundle, String contextPath, List<Long> bundleId) {
        EventAdmin eventAdmin = getEventAdmin();
        if (eventAdmin == null) {
            return;
        }
        Dictionary<String, Object> props = createDefaultProperties(bundle, contextPath);
        /*
         * XXX: The specification doesn't exactly say what type of COLLISION and COLLISION_BUNDLES are
         */
        props.put(WebContainerConstants.COLLISION, Boolean.TRUE);
        props.put(WebContainerConstants.COLLISION_BUNDLES, bundleId);
        eventAdmin.postEvent(new Event(WebContainerConstants.TOPIC_FAILED, props));
    }
    
    /**
     * Dispatch an event to the appropriate listeners.
     *
     * @param topic  The event topic.
     * @param bundle The bundle hosting the web application.
     * @param contextPath
     *               The contextPath information from the bundle.
     */
    private void dispatch(String topic, Bundle bundle, String contextPath) {
        EventAdmin eventAdmin = getEventAdmin();
        if (eventAdmin == null) {
            return;
        }
        Dictionary<String, Object> props = createDefaultProperties(bundle, contextPath);
        eventAdmin.postEvent(new Event(topic, props));
    }

    private Dictionary<String, Object> createDefaultProperties(Bundle bundle, String contextPath) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        
        props.put(EventConstants.BUNDLE_SYMBOLICNAME, bundle.getSymbolicName());
        props.put(EventConstants.BUNDLE_ID, bundle.getBundleId());
        props.put(EventConstants.BUNDLE, bundle);
        props.put(EventConstants.BUNDLE_VERSION, getBundleVersion(bundle));
        props.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
        
        props.put(WebContainerConstants.CONTEXT_PATH, contextPath);
               
        props.put(WebContainerConstants.EXTENDER_BUNDLE, extenderBundle);
        props.put(WebContainerConstants.EXTENDER_BUNDLE_ID, extenderBundle.getBundleId());
        props.put(WebContainerConstants.EXTENDER_BUNDLE_SYMBOLICNAME, extenderBundle.getSymbolicName());
        props.put(WebContainerConstants.EXTENDER_BUNDLE_VERSION, getBundleVersion(extenderBundle));
        
        return props;
    }
    
    private EventAdmin getEventAdmin() {
        return (tracker != null) ? (EventAdmin) tracker.getService() : null;
    }
    
    public void destroy() {
        if (tracker != null) {
            tracker.close();
        }
    }

    private Version getBundleVersion(Bundle bundle) {
        Dictionary headers = bundle.getHeaders();
        String version = (String) headers.get(Constants.BUNDLE_VERSION);
        return (version != null) ? Version.parseVersion(version) : Version.emptyVersion;
    }
}

