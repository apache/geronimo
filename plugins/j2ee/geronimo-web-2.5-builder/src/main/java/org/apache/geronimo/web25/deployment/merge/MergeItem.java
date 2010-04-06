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

package org.apache.geronimo.web25.deployment.merge;

/**
 * @version $Rev$ $Date$
 */
public class MergeItem {

    private String belongedURL;

    private Object value;

    private ElementSource sourceType;

    public MergeItem(Object value, String belongedURL, ElementSource sourceType) {
        this.value = value;
        this.belongedURL = belongedURL;
        this.sourceType = sourceType;
    }

    public String getBelongedURL() {
        return belongedURL;
    }

    public Object getValue() {
        return value;
    }

    public ElementSource getSourceType() {
        return sourceType;
    }

    public boolean isFromAnnotation() {
        return sourceType.equals(ElementSource.ANNOTATION);
    }

    public boolean isFromWebFragment() {
        return sourceType.equals(ElementSource.WEB_FRAGMENT);
    }

    public boolean isFromWebXml() {
        return sourceType.equals(ElementSource.WEB_XML);
    }

    public void setBelongedURL(String belongedURL) {
        this.belongedURL = belongedURL;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setSourceType(ElementSource sourceType) {
        this.sourceType = sourceType;
    }
}