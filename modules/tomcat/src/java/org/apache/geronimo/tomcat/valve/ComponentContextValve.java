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
package org.apache.geronimo.tomcat.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.naming.java.RootContext;

import javax.naming.Context;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @version $Rev: $ $Date: $
 */
public class ComponentContextValve extends ValveBase {

    private final Context componentContext;

    public ComponentContextValve(Context context){
        componentContext = context;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {

        //Get the old context
        Context oldContext = RootContext.getComponentContext();

        //Set the new context
        RootContext.setComponentContext(componentContext);
        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);

        //Set the old one back
        RootContext.setComponentContext(oldContext);
    }
}
