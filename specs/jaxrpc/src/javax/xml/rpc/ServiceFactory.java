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
package javax.xml.rpc;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * The <code>javax.xml.rpc.ServiceFactory</code> is an abstract class
 * that provides a factory for the creation of instances of the type
 * <code>javax.xml.rpc.Service</code>. This abstract class follows the
 * abstract static factory design pattern. This enables a J2SE based
 * client to create a <code>Service instance</code> in a portable manner
 * without using the constructor of the <code>Service</code>
 * implementation class.
 * <p>
 * The ServiceFactory implementation class is set using the
 * system property <code>SERVICEFACTORY_PROPERTY</code>.
 *
 * @version 1.0
 */
public abstract class ServiceFactory {

    /** Protected constructor. */
    protected ServiceFactory() {}

    /**
     * A constant representing the property used to lookup the
     * name of a <code>ServiceFactory</code> implementation
     * class.
     */
    public static final java.lang.String SERVICEFACTORY_PROPERTY =
        "javax.xml.rpc.ServiceFactory";

    /**
     * Gets an instance of the <code>ServiceFactory</code>
     *
     * <p>Only one copy of a factory exists and is returned to the
     * application each time this method is called.
     *
     * <p> The implementation class to be used can be overridden by
     * setting the javax.xml.rpc.ServiceFactory system property.
     *
     * @return  ServiceFactory.
     * @throws  ServiceException
     */
    public static ServiceFactory newInstance() throws ServiceException {

        try {
            return (ServiceFactory) FactoryFinder.find(
                /* The default property name according to the JAXRPC spec */
                SERVICEFACTORY_PROPERTY,
                /* The fallback implementation class name */
                "org.apache.axis.client.ServiceFactory");
        } catch (FactoryFinder.ConfigurationError e) {
            throw new ServiceException(e.getException());
        }
    }

    /**
     * Create a <code>Service</code> instance.
     *
     * @param   wsdlDocumentLocation URL for the WSDL document location
     * @param   serviceName  QName for the service.
     * @return  Service.
     * @throws  ServiceException If any error in creation of the
     *                specified service
     */
    public abstract Service createService(
        URL wsdlDocumentLocation, QName serviceName) throws ServiceException;

    /**
     * Create a <code>Service</code> instance.
     *
     * @param   serviceName QName for the service
     * @return  Service.
     * @throws  ServiceException If any error in creation of the specified service
     */
    public abstract Service createService(QName serviceName)
        throws ServiceException;
    
    public abstract Service loadService(java.lang.Class class1)
                             throws ServiceException;
    
    public abstract Service loadService(java.net.URL url,
                                    java.lang.Class class1,
                                    java.util.Properties properties)
                             throws ServiceException;
    
    public abstract Service loadService(java.net.URL url,
                                    QName qname,
                                    java.util.Properties properties)
                             throws ServiceException;
}

