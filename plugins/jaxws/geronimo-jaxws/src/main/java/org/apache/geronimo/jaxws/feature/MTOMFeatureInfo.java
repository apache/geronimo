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

package org.apache.geronimo.jaxws.feature;

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

/**
 * @version $Rev$ $Date$
 */
public class MTOMFeatureInfo implements WebServiceFeatureInfo {

    private boolean enabled;

    private int threshold;

    public MTOMFeatureInfo(MTOM mtom) {
        this(mtom.enabled(), mtom.threshold());
    }

    public MTOMFeatureInfo(boolean enabled, int threshold) {
        this.enabled = enabled;
        this.threshold = threshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public WebServiceFeature getWebServiceFeature() {
        return new MTOMFeature(enabled, threshold);
    }

}
