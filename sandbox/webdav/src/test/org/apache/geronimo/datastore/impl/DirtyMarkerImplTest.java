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

package org.apache.geronimo.datastore.impl;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/29 13:14:11 $
 */
public class DirtyMarkerImplTest extends TestCase {

    public void testNew() {
        DirtyMarkerImpl dirtyMarker = new DirtyMarkerImpl();
        dirtyMarker.setIsNew(true);

        Exception ex = null;
        try {
            dirtyMarker.setIsDelete(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
        
        try {
            dirtyMarker.setIsDirty(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
    }

    public void testDirty() {
        DirtyMarkerImpl dirtyMarker = new DirtyMarkerImpl();
        dirtyMarker.setIsDirty(true);

        Exception ex = null;
        try {
            dirtyMarker.setIsNew(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
        
        try {
            dirtyMarker.setIsDelete(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
    }
    
    public void testDelete() {
        DirtyMarkerImpl dirtyMarker = new DirtyMarkerImpl();
        dirtyMarker.setIsDelete(true);

        Exception ex = null;
        try {
            dirtyMarker.setIsNew(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
        
        try {
            dirtyMarker.setIsDirty(true);
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull("Transition should ne impossible", ex);
    }
    
}
