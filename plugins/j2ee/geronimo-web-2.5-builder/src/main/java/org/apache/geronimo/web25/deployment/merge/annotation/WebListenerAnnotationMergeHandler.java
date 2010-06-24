/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.web25.deployment.merge.annotation;

import java.util.Arrays;

import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.webfragment.ListenerMergeHandler;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.Text;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class WebListenerAnnotationMergeHandler implements AnnotationMergeHandler {

    public static final Class<?>[] SUPPORTED_WEBLISTENER_INTERFACES = { ServletContextListener.class, ServletContextAttributeListener.class, ServletRequestListener.class,
            ServletRequestAttributeListener.class, HttpSessionListener.class, HttpSessionAttributeListener.class, javax.servlet.AsyncListener.class };

    @Override
    public void merge(Class<?>[] classes, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Class<?> cls : classes) {
            //Check whether any supported web listener interface is implemented
            Class<?> implementedWebListenerInterface = null;
            for (Class<?> supportedWebListenerInterface : SUPPORTED_WEBLISTENER_INTERFACES) {
                if (supportedWebListenerInterface.isAssignableFrom(cls)) {
                    implementedWebListenerInterface = supportedWebListenerInterface;
                    break;
                }
            }
            if (implementedWebListenerInterface == null) {
                throw new DeploymentException("One of supported web listener interface " + Arrays.toString(SUPPORTED_WEBLISTENER_INTERFACES) + " should  be implemented by class " + cls.getName()
                        + " while WebListener annotation is used");
            }
            WebListener webListener = cls.getAnnotation(WebListener.class);
            if (ListenerMergeHandler.isListenerConfigured(cls.getName(), mergeContext)) {
                return;
            }
            Listener newListener = new Listener();
            if (!webListener.value().isEmpty()) {
                newListener.addDescription(new Text(null, webListener.value()));
            }
            newListener.setListenerClass(cls.getName());
            webApp.getListener().add(newListener);
            //
            ListenerMergeHandler.addListener(newListener, mergeContext);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web25.deployment.merge.annotation.AnnotationMergeHandler#postProcessWebXmlElement(org.apache.openejb.jee.WebApp, org.apache.geronimo.web25.deployment.merge.MergeContext)
     */
    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web25.deployment.merge.annotation.AnnotationMergeHandler#preProcessWebXmlElement(org.apache.openejb.jee.WebApp, org.apache.geronimo.web25.deployment.merge.MergeContext)
     */
    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        // TODO Auto-generated method stub
    }
}
