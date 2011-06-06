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

package org.apache.geronimo.myfaces.facelet;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.faces.context.ExternalContext;

import org.apache.geronimo.myfaces.webapp.MyFacesWebAppContext;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.myfaces.spi.FaceletConfigResourceProvider;
import org.apache.myfaces.spi.FaceletConfigResourceProviderFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoFaceletConfigResourceProviderFactory extends FaceletConfigResourceProviderFactory {

    private FaceletConfigResourceProvider faceletConfigResourceProvider;

    @Override
    public FaceletConfigResourceProvider createFaceletConfigResourceProvider(ExternalContext externalContext) {
        if (faceletConfigResourceProvider == null) {
            String webModuleName = (String) externalContext.getApplicationMap().get(WebApplicationConstants.WEB_APP_NAME);
            final MyFacesWebAppContext myFacesWebAppContext = MyFacesWebAppContext.getMyFacesWebAppContext(webModuleName);
            faceletConfigResourceProvider = new FaceletConfigResourceProvider() {

                @Override
                public Collection<URL> getFaceletTagLibConfigurationResources(ExternalContext arg0) throws IOException {
                    return myFacesWebAppContext.getRuntimeFaceletConfigResources();
                }

            };
        }
        return faceletConfigResourceProvider;
    }

}
