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

package org.apache.geronimo.twiddle.console;

import java.util.List;
import java.util.LinkedList;

import org.apache.geronimo.common.NullArgumentException;

/**
 * A transient history backing.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public class TransientHistory
    implements History
{
    /** The list which provides the backing. */
    protected List backing;
    
    /**
     * Construct a <code>TransientHistory</code> with the given list for backing.
     *
     * @param backing   The list which provides actual backing.
     */
    public TransientHistory(final List backing)
    {
        if (backing == null) {
            throw new NullArgumentException("backing");
        }
        
        this.backing = backing;
    }
    
    /**
     * Construct a <code>TransientHistory</code> with default linked list backing.
     */
    public TransientHistory()
    {
        this(new LinkedList());
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                               History                               //
    /////////////////////////////////////////////////////////////////////////
    
    public int add(final String line)
    {
        backing.add(line);
        return backing.size() - 1;
    }
    
    public String get(final int i)
    {
        return (String)backing.get(i);
    }
    
    public int size()
    {
        return backing.size();
    }
    
    public void clear()
    {
        backing.clear();
    }
}
