/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.servlet;

/**
 * This is the event class for notifications about changes to the servlet context
 * of a web application.
 *
 * @see ServletContextListener
 *
 * @since Servlet 2.3
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 10:00:19 $
 */
public class ServletContextEvent extends java.util.EventObject {
    /**
     * Construct a ServletContextEvent from the given context.
     *
     * @param source the ServletContext that is sending the event
     */
    public ServletContextEvent(ServletContext source) {
        super(source);
    }

    /**
     * Return the ServletContext that changed.
     *
     * @return the ServletContext that sent the event
     */
    public ServletContext getServletContext() {
        return (ServletContext) super.getSource();
    }
}

