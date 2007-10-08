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
package org.apache.geronimo.pluto;

import java.util.ArrayList;

import org.apache.pluto.driver.services.portal.PageConfig;

public interface AdminConsoleExtension {

    /*
     * Add the PageConfig to pluto.  This will overwrite any existing pages with the same page name
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#addPage(org.apache.pluto.driver.services.portal.PageConfig)
     */
    public void addPage(PageConfig pageConfig);
    
    /*
     * This will add the portlets to the PageConfig in Pluto.
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#addPortlets(java.lang.String, java.lang.String, java.util.ArrayList)
     */
    public void addPortlets(String pageTitle, String portletContext, ArrayList<String> portletList);
    
    /*
     * Removes a PageConfig object in Pluto with the pageTitle
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#removePage(java.lang.String)
     */
    public void removePage(String pageTitle);
    
    /*
     * Removes the portletList from the PageConfig in Pluto
     * @see org.apache.geronimo.pluto.PlutoAccessInterface#removePortlets(java.lang.String, java.util.ArrayList)
     */
    public void removePortlets(String pageTitle, ArrayList<String> portletList);
    
}
