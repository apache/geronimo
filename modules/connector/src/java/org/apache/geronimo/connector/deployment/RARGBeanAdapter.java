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

package org.apache.geronimo.connector.deployment;

import org.apache.geronimo.deployment.service.GBeanAdapter;
import org.apache.geronimo.xbeans.geronimo.GerGbeanType;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class RARGBeanAdapter implements GBeanAdapter {

    private final GerGbeanType gbean;

    public RARGBeanAdapter(GerGbeanType gbean) {
        this.gbean = gbean;
    }

    public String getName() {
        return gbean.getName();
    }

    public String getClass1() {
        return gbean.getClass1();
    }

    public int getAttributeCount() {
        return gbean.getAttributeArray().length;
    }

    public String getAttributeName(int i) {
        return gbean.getAttributeArray(i).getName();
    }

    public String getAttributeType(int i) {
        return gbean.getAttributeArray(i).getType();
    }

    public String getAttributeStringValue(int i) {
        return gbean.getAttributeArray(i).getStringValue();
    }

    public int getReferenceCount() {
        return gbean.getReferenceArray().length;
    }

    public String getReferenceName(int i) {
        return gbean.getReferenceArray(i).getName();
    }

    public String getReferenceStringValue(int i) {
        return gbean.getReferenceArray(i).getStringValue();
    }

    public int getReferencesCount() {
        return gbean.getReferencesArray().length;
    }

    public String getReferencesName(int i) {
        return gbean.getReferencesArray(i).getName();
    }

    public String[] getReferencesPatternArray(int i) {
        return gbean.getReferencesArray(i).getPatternArray();
    }
}
