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
import java.util.HashSet;
import java.util.Set;

/**
 * A property editor for {@link Set}.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:26 $
 */
public class SetEditor
    extends PropertyEditorSupport
{
    protected Collection createCollection()
    {
        return createSet();
    }
    
    protected Set createSet()
    {
        return new HashSet();
    }
    
    protected void setValue(Set list)
    {
        super.setValue(list);
    }
    
    public void setValue(Collection bag)
    {
        setValue((Set)bag);
    }
}
