/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import org.apache.axis.wsdl.fromJava.Emitter;
import org.apache.geronimo.ews.ws4j2ee.toWs.GenerationConstants;
import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2eeDeployContext;
import org.apache.geronimo.ews.ws4j2ee.toWs.impl.Ws4J2eeDeployContextImpl;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWsDeployContext extends Ws4J2eeDeployContextImpl implements Ws4J2eeDeployContext {
    /**
     * Constructor GeronimoWsDeployContext
     *
     * @param moduleLocation
     * @param outputLocation
     */
    public GeronimoWsDeployContext(String moduleLocation,
                                   String outputLocation) {
        super(moduleLocation,outputLocation,Thread.currentThread().getContextClassLoader());
    }

    /**
     * Method getMode
     *
     * @return
     */
    public int getMode() {
        return Emitter.MODE_ALL;
    }

    /**
     * Method getWsdlImplFilename
     *
     * @return
     */
    public String getWsdlImplFilename() {
        return null;
    }


    /**
     * Method getContanier
     *
     * @return
     */
    public String getContanier() {
        return GenerationConstants.GERONIMO_CONTAINER;
    }

    /**
     * Method getImplStyle
     *
     * @return
     */
    public String getImplStyle() {
        return GenerationConstants.USE_INTERNALS;
    }

    /**
     * Method getOutPutLocation
     *
     * @return
     */
    public String getOutPutLocation() {
        return outputLocation;
    }

    public boolean isCompile() {
        return true;
    }
}
