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

package javax.activation;

import java.awt.datatransfer.DataFlavor;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class ActivationDataFlavor extends DataFlavor {
    public ActivationDataFlavor(Class representationClass, String mimeType, String humanPresentableName) {
        /*@todo implement*/
    }

    public ActivationDataFlavor(Class representationClass, String humanPresentableName) {
        /*@todo implement*/
    }

    public ActivationDataFlavor(String mimeType, String humanPresentableName) {
        /*@todo implement*/
    }

    public String getMimeType() {
        /*@todo implement*/
        return null;
    }

    public Class getRepresentationClass() {
        /*@todo implement*/
        return null;
    }

    public String getHumanPresentableName() {
        /*@todo implement*/
        return null;
    }

    public void setHumanPresentableName(String humanPresentableName) {
        /*@todo implement*/
    }

    public boolean equals(DataFlavor dataFlavor) {
        /*@todo implement*/
        return false;
    }

    public boolean isMimeTypeEqual(String mimeType) {
        /*@todo implement*/
        return false;
    }

    protected String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
        /*@todo implement*/
        return null;
    }

    protected String normalizeMimeType(String mimeType) {
        /*@todo implement*/
        return null;
    }
}