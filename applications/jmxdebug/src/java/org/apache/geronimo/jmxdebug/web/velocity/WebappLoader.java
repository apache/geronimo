/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jmxdebug.web.velocity;

import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;

import javax.servlet.ServletContext;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;

/**
 *  Simple webapp loader.  This code has been recycled from contributions to Velocity
 *  by me.
 *
 * @version $Id$
 */
public class WebappLoader extends ResourceLoader {

    public ServletContext servletContext = null;

    public static String KEY = "org.apache.geronimo.console.web.velocity.WebappLoader";

    /**
     * This is abstract in the base class, so we need it
     */
    public void init(ExtendedProperties configuration) {

        rsvc.info("WebappLoader : initialization starting.");

        Object o = rsvc.getApplicationAttribute(KEY);

        if (o instanceof WebappLoaderAppContext) {
            servletContext = ((WebappLoaderAppContext) o).getServletContext();
        }
        else
            rsvc.error("WebappLoader : unable to retrieve ServletContext");

        rsvc.info("WebappLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     *          if template not found
     *          in  classpath.
     */
    public synchronized InputStream getResourceStream(String name)
            throws ResourceNotFoundException {

        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException("No template name provided");
        }

        try {
            if (!name.startsWith("/"))
                name = "/" + name;

            return servletContext.getResourceAsStream(name);
        }
        catch (Exception fnfe) {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */

            throw new ResourceNotFoundException(fnfe.getMessage());
        }
    }

    /**
     * Defaults to return false.
     */
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    /**
     * Defaults to return 0
     */
    public long getLastModified(Resource resource) {
        return 0;
    }

    public interface WebappLoaderAppContext {

        public ServletContext getServletContext();

    }
}

