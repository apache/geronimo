/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class GBeanHelper {
    public static void addGbean(GBeanAdapter gbean, ClassLoader cl, DeploymentContext context) throws DeploymentException {
        GBeanBuilder builder = new GBeanBuilder(gbean.getName(), cl, gbean.getClass1());

        // set up attributes
        for (int j = 0; j < gbean.getAttributeCount(); j++) {
            builder.setAttribute(gbean.getAttributeName(j), gbean.getAttributeType(j), gbean.getAttributeStringValue(j));
        }

        // set up all single pattern references
        for (int j = 0; j < gbean.getReferenceCount(); j++) {
            builder.setReference(gbean.getReferenceName(j), gbean.getReferenceStringValue(j));
        }

        // set up app multi-patterned references
        for (int j = 0; j < gbean.getReferencesCount(); j++) {
            builder.setReference(gbean.getReferencesName(j), gbean.getReferencesPatternArray(j));
        }

        context.addGBean(builder.getGBeanData());
    }
}
