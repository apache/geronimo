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
 * @version $Rev$ $Date$
 */
public class ActivationDataFlavor extends DataFlavor {
    private String humanPresentableName;

    public ActivationDataFlavor(Class representationClass, String mimeType, String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
    }

    public ActivationDataFlavor(Class representationClass, String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
    }

    public ActivationDataFlavor(String mimeType, String humanPresentableName) {
        super(mimeType, humanPresentableName);
        this.humanPresentableName = humanPresentableName;
    }

    public String getMimeType() {
        return super.getMimeType();
    }

    public Class getRepresentationClass() {
        return super.getRepresentationClass();
    }

    public String getHumanPresentableName() {
        return humanPresentableName;
    }

    public void setHumanPresentableName(String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
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