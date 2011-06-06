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

package org.apache.geronimo.myfaces.info;

import javax.faces.context.ExternalContext;

import org.apache.geronimo.myfaces.webapp.MyFacesWebAppContext;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.myfaces.config.element.FacesConfigData;
import org.apache.myfaces.spi.FacesConfigurationMerger;
import org.apache.myfaces.spi.FacesConfigurationMergerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoFacesConfigurationMergerFactory extends FacesConfigurationMergerFactory {

    private FacesConfigurationMerger facesConfigurationMerger;

    @Override
    public FacesConfigurationMerger getFacesConfigurationMerger(ExternalContext externalContext) {
        if (facesConfigurationMerger == null) {
            String webModuleName = (String) externalContext.getApplicationMap().get(WebApplicationConstants.WEB_APP_NAME);
            final MyFacesWebAppContext myFacesWebAppContext = MyFacesWebAppContext.getMyFacesWebAppContext(webModuleName);
            facesConfigurationMerger = new FacesConfigurationMerger() {

                @Override
                public FacesConfigData getFacesConfigData(ExternalContext arg0) {
                    return myFacesWebAppContext.getFacesConfigData();
                }
            };
        }
        return facesConfigurationMerger;
    }

}
