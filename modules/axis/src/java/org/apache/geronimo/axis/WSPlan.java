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

import java.io.File;
import java.net.URI;

import javax.management.ObjectName;

/**
  * @version $Rev: $ $Date: $ 
  */
public class WSPlan {
    private final boolean isEJBbased;
    private final ObjectName ejbConfName;
    private final ObjectName wsName;
    private final File module; 
    private final URI configURI;
    
    private WSPlan(URI configURI,ObjectName wsName,ObjectName ejbConfName,File module){
        this.wsName = wsName;
        this.ejbConfName = ejbConfName;
        this.module = module;
        isEJBbased = true;
        this.configURI = configURI;
    }
    
    private WSPlan(URI configURI,ObjectName wsName,File module){
        this.wsName = wsName;
        this.ejbConfName = null;
        this.module = module;
        isEJBbased = false;
        this.configURI = configURI;
    }
    public static WSPlan createPlan(URI configURI,ObjectName wsName,ObjectName ejbConfName,File module){
        return new WSPlan(configURI,wsName,ejbConfName,module);
    }
    
    public static WSPlan createPlan(URI configURI,ObjectName wsName,File module){
        return new WSPlan(configURI,wsName,module);
    }
    /**
     * @return
     */
    public ObjectName getEjbConfName() {
        return ejbConfName;
    }

    /**
     * @return
     */
    public boolean isEJBbased() {
        return isEJBbased;
    }

    /**
     * @return
     */
    public File getModule() {
        return module;
    }

    /**
     * @return
     */
    public ObjectName getWsName() {
        return wsName;
    }

    /**
     * @return
     */
    public URI getConfigURI() {
        return configURI;
    }

}
