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

package org.apache.geronimo.common.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * A property editor for {@link Collection}.
 *
 * @version $Rev$ $Date$
 */
public class CollectionEditor
    extends PropertyEditorSupport
{
    protected Collection createCollection()
    {
        return new LinkedList();
    }
    
    public void setAsText(final String text)
    {
        Collection bag = createCollection();
        StringTokenizer stok = new StringTokenizer(text, ",");
        
        // need to handle possible "[" and "]"
        
        while (stok.hasMoreTokens()) {
            bag.add(stok.nextToken().trim());
        }
        
        setValue(bag);
    }
}
