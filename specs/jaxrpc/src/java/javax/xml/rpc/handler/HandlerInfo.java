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
package javax.xml.rpc.handler;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>javax.xml.rpc.handler.HandlerInfo</code> represents
 * information about a handler in the HandlerChain. A HandlerInfo
 * instance is passed in the <code>Handler.init</code> method to
 * initialize a <code>Handler</code> instance.
 *
 * @version 1.0
 * @see HandlerChain
 */
public class HandlerInfo implements Serializable {

    /** Default constructor. */
    public HandlerInfo() {
        handlerClass = null;
        config       = new HashMap();
    }

    /**
     *  Constructor for HandlerInfo.
     *
     *  @param  handlerClass Java Class for the Handler
     *  @param  config Handler Configuration as a java.util.Map
     *  @param  headers QNames for the header blocks processed
     *          by this Handler.  QName is the qualified name
     *          of the outermost element of a header block
     */
    public HandlerInfo(Class handlerClass, Map config, QName[] headers) {

        this.handlerClass = handlerClass;
        this.config       = config;
        this.headers      = headers;
    }

    /**
     *  Sets the Handler class.
     *
     *  @param  handlerClass Class for the Handler
     */
    public void setHandlerClass(Class handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     *  Gets the Handler class.
     *
     *  @return Returns null if no Handler class has been
     *    set; otherwise the set handler class
     */
    public Class getHandlerClass() {
        return handlerClass;
    }

    /**
     *  Sets the Handler configuration as <code>java.util.Map</code>
     *  @param  config Configuration map
     */
    public void setHandlerConfig(Map config) {
        this.config = config;
    }

    /**
     *  Gets the Handler configuration.
     *
     *  @return  Returns empty Map if no configuration map
     *     has been set; otherwise returns the set configuration map
     */
    public Map getHandlerConfig() {
        return config;
    }

    /**
     * Sets the header blocks processed by this Handler.
     * @param headers QNames of the header blocks. QName
     *            is the qualified name of the outermost
     *            element of the SOAP header block
     */
    public void setHeaders(QName[] headers) {
        this.headers = headers;
    }

    /**
     * Gets the header blocks processed by this Handler.
     * @return Array of QNames for the header blocks. Returns
     *      <code>null</code> if no header blocks have been
     *      set using the <code>setHeaders</code> method.
     */
    public QName[] getHeaders() {
        return headers;
    }

    /** Handler Class. */
    private Class handlerClass;

    /** Configuration Map. */
    private Map config;

    /** Headers. */
    private QName[] headers;
}

