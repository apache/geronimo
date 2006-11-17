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
package org.apache.geronimo.common.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * An abstract collection editor.  Subclasses should provide the correct type of collection from
 * the createCollection method and should override setValue to check the type of the value.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractCollectionEditor extends PropertyEditorSupport {

    /**
     * Concrete subclasses should implement this method to create the correct type of collection.
     *
     * @return an empty instance of the type of collection  the subclass edits.
     */
    protected abstract Collection createCollection();

    public void setAsText(String text) {
        if (text == null) {
            setValue(null);
        } else {
            if (text.startsWith("[")) {
                text = text.substring(1);
            }
            if (text.endsWith("]")) {
                text = text.substring(0, text.length() - 1);
            }
            Collection collection = createCollection();
            StringTokenizer stok = new StringTokenizer(text, ",");

            while (stok.hasMoreTokens()) {
                collection.add(stok.nextToken().trim());
            }

            setValue(collection);
        }
    }
}
