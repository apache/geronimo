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

/**
 * adapter interface for multiple copies of xml gbean type.
 *
 * @version $Rev$ $Date$
 *
 * */
public interface GBeanAdapter {
    String getName();
    String getClass1();
    int getAttributeCount();
    String getAttributeName(int i);
    String getAttributeType(int i);
    String getAttributeStringValue(int i);
    int getReferenceCount();
    String getReferenceName(int i);
    String getReferenceStringValue(int i);
    int getReferencesCount();
    String getReferencesName(int i);
    String[] getReferencesPatternArray(int i);
}
