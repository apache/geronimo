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
package org.apache.geronimo.osgi.web.extender;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.geronimo.osgi.web.WebApplicationListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
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
    // service tracker for WebApplicationListener
    private ServiceTracker listenerTracker;
    private final Set<WebApplicationListener> listeners = new CopyOnWriteArraySet<WebApplicationListener>();

    public WebContainerEventDispatcher(final BundleContext bundleContext) {
        this.extenderBundle = bundleContext.getBundle();

        if (isEventAdminPresent()) {
            // this will track the availability of the EventAdmin service when we need to dispatch
            tracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
            tracker.open();
        }
        
        listenerTracker = new ServiceTracker(bundleContext, WebApplicationListener.class.getName(), new ServiceTrackerCustomizer() {
            public Object addingService(ServiceReference reference) {
                WebApplicationListener listener = (WebApplicationListener) bundleContext.getService(reference);
                listeners.add(listener);
                return listener;
            }

            public void modifiedService(ServiceReference reference, Object service) {
            }

            public void removedService(ServiceReference reference, Object service) {
                listeners.remove(service);
                bundleContext.ungetService(reference);
            }
        });
        listenerTracker.open();
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
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        for (WebApplicationListener listener : listeners) {
            try {
                listener.deploying(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_DEPLOYING, properties);
    }

    /**
     * Dispatch a deployed event
     *
     * @param bundle The bundle we're deploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void deployed(Bundle bundle, String contextPath) {
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        for (WebApplicationListener listener : listeners) {
            try {
                listener.deployed(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_DEPLOYED, properties);
    }

    /**
     * Dispatch an undeploying event
     *
     * @param bundle The bundle we're undeploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void undeploying(Bundle bundle, String contextPath) {
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        for (WebApplicationListener listener : listeners) {
            try {
                listener.undeploying(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_UNDEPLOYING, properties);
    }

    /**
     * Dispatch an undeployed event
     *
     * @param bundle The bundle we're undeploying.
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void undeployed(Bundle bundle, String contextPath) {
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        for (WebApplicationListener listener : listeners) {
            try {
                listener.undeployed(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_UNDEPLOYED, properties);
    }

    /**
     * Dispatch a FAILED event
     *
     * @param bundle The bundle we're attempting to deploy
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void failed(Bundle bundle, String contextPath, Throwable cause) {
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        if (cause != null) {
            properties.put(EventConstants.EXCEPTION, cause);
        }
        for (WebApplicationListener listener : listeners) {
            try {
                listener.failed(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_FAILED, properties);
    }
    
    /**
     * Dispatch a FAILED event indicating a contextPath collision
     *
     * @param bundle The bundle we're attempting to deploy
     * @param contextPath
     *               The context path information from the bundle.
     */
    public void collision(Bundle bundle, String contextPath, Collection<Long> bundleId) {
        Dictionary<String, Object> properties = createDefaultProperties(bundle, contextPath);
        properties.put(WebContainerConstants.COLLISION, contextPath);
        properties.put(WebContainerConstants.COLLISION_BUNDLES, bundleId);
        for (WebApplicationListener listener : listeners) {
            try {
                listener.failed(bundle, contextPath, properties);
            } catch (Exception e) {
                LOGGER.warn("Error notifying listener: " + listener, e);
                break;
            }
        }
        dispatch(WebContainerConstants.TOPIC_FAILED, properties);
    }
        
    /**
     * Dispatch an event to the appropriate listeners.
     *
     * @param topic  The event topic.
     * @param bundle The bundle hosting the web application.
     * @param contextPath
     *               The contextPath information from the bundle.
     */
    private void dispatch(String topic, Dictionary<String, Object> props) {
        EventAdmin eventAdmin = getEventAdmin();
        if (eventAdmin == null) {
            return;
        }
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
        listenerTracker.close();
    }

    private Version getBundleVersion(Bundle bundle) {
        Dictionary headers = bundle.getHeaders();
        String version = (String) headers.get(Constants.BUNDLE_VERSION);
        return (version != null) ? Version.parseVersion(version) : Version.emptyVersion;
    }
}

