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

public interface PlutoAccessInterface {

    //add a page to the Pluto container
    public void addPage(PageConfig pageConfig);
    
    //add a list of portlets to the page
    public void addPortlets(String pageTitle, String portletContext, ArrayList<String> portletList);
    
    //remove a page from the Pluto container
    public void removePage(String pageTitle);

    //remove a list of portlets from the page, and will remove a page if it is empty of portlets
    public void removePortlets(String pageTitle, ArrayList<String> portletList);
}
