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
package org.apache.geronimo.kernel.config;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * Interface to a store for Configurations.
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/14 22:16:38 $
 */
public interface ConfigurationStore {
    /**
     * Add the CAR at the supplied URL into this store
     * @param source the URL of a CAR format archive
     * @throws java.io.IOException if the CAR could not be read
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a configuration problem with the CAR
     */
    void install(URL source) throws IOException, InvalidConfigException;

    /**
     * Return the Configuration GBean for the specified ID
     * @param id the unique ID of a Configuration
     * @return the GBeanMBean for that configuration
     * @throws org.apache.geronimo.kernel.config.NoSuchConfigException if the store does not contain a Configuration with that id
     * @throws java.io.IOException if there was a problem loading the Configuration from the store
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if the Configuration is invalid
     */
    GBeanMBean getConfig(URI id) throws NoSuchConfigException, IOException, InvalidConfigException;

    /**
     * Return the base URL for the specified ID
     * @param id the unique ID for a Configuration
     * @return the URL of the base location for the Configuration that should be used for resolution
     * @throws org.apache.geronimo.kernel.config.NoSuchConfigException if the store does not contain a Configuration with that id
     */
    URL getBaseURL(URI id) throws NoSuchConfigException;
}
