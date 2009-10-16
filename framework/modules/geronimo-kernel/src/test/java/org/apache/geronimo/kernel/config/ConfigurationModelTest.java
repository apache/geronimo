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
package org.apache.geronimo.kernel.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationModelTest extends TestCase {
    private static final Artifact rootId = new Artifact("root", "", "", "");
    private static final Artifact midId = new Artifact("mid", "", "", "");
    private static final Artifact leftId = new Artifact("left", "", "", "");
    private static final Artifact rightId = new Artifact("right", "", "", "");
    private static final Artifact childId = new Artifact("child", "", "", "");
    private static final Set all = asSet(rootId, midId, leftId, rightId, childId);


    /**
     * The model below, with root and child user loaded and started
     *
     *        root
     *         |
     *        mid
     *        / \
     *     left right
     *        \ /
     *       child
     *
     */
    private final ConfigurationModel diamondModel = new ConfigurationModel();

    protected void setUp() throws Exception {
        super.setUp();

        diamondModel.addConfiguration(rootId, Collections.EMPTY_SET, Collections.EMPTY_SET);
        diamondModel.addConfiguration(midId, Collections.singleton(rootId), Collections.singleton(rootId));
        diamondModel.addConfiguration(leftId, Collections.singleton(midId), Collections.singleton(midId));
        diamondModel.addConfiguration(rightId, Collections.singleton(midId), Collections.singleton(midId));

        Set leftAndRight = asSet(leftId, rightId);
        diamondModel.addConfiguration(childId, leftAndRight, leftAndRight);

        // Load and start the root and child
        diamondModel.load(rootId);
        diamondModel.start(rootId);
        diamondModel.load(childId);
        diamondModel.start(childId);

        // all  nodes should be loaded  and started
        assertEquals(all, diamondModel.getLoaded());
        assertEquals(all, diamondModel.getStarted());

        // only root and child should be user loaded and started
        assertEquals(asSet(rootId, childId), diamondModel.getUserLoaded());
        assertEquals(asSet(rootId, childId), diamondModel.getUserStarted());
    }

    public void testStopChild() throws NoSuchConfigException {
        LinkedHashSet stopList = diamondModel.stop(childId);

        // the only thing left running should be the root node
        assertEquals(asSet(rootId), diamondModel.getStarted());
        assertEquals(asSet(rootId), diamondModel.getUserStarted());

        // everything should still be loaded
        assertEquals(all, diamondModel.getLoaded());
        assertEquals(asSet(rootId, childId), diamondModel.getUserLoaded());

        // checke the order of the list
        assertEquals(4, stopList.size());
        assertFalse(stopList.contains(rootId));
        assertBefore(childId, leftId, stopList);
        assertBefore(childId, rightId, stopList);
        assertBefore(leftId, midId, stopList);
        assertBefore(rightId, midId, stopList);
    }

    public void testStopLeft() throws NoSuchConfigException {
        LinkedHashSet stopList = diamondModel.stop(leftId);

        // the only thing left running should be the root node
        assertEquals(asSet(rootId), diamondModel.getStarted());
        assertEquals(asSet(rootId), diamondModel.getUserStarted());

        // everything should still be loaded
        assertEquals(all, diamondModel.getLoaded());
        assertEquals(asSet(rootId, childId), diamondModel.getUserLoaded());

        // checke the order of the list
        assertEquals(4, stopList.size());
        assertFalse(stopList.contains(rootId));
        assertBefore(childId, leftId, stopList);
        assertBefore(childId, rightId, stopList);
        assertBefore(leftId, midId, stopList);
        assertBefore(rightId, midId, stopList);
    }

    public void testPinRightStopLeft() throws NoSuchConfigException {
        LinkedHashSet startList = diamondModel.start(rightId);
        assertTrue(startList.isEmpty());

        LinkedHashSet stopList = diamondModel.stop(leftId);

        // the right, mid and root nodes should be started
        assertEquals(asSet(rootId, midId, rightId), diamondModel.getStarted());
        assertEquals(asSet(rootId, rightId), diamondModel.getUserStarted());

        // everything should still be loaded
        assertEquals(all, diamondModel.getLoaded());
        assertEquals(asSet(rootId, rightId, childId), diamondModel.getUserLoaded());

        // checke the order of the list
        assertContainsNone(stopList, asSet(rootId, midId, rightId));
        assertEquals(2, stopList.size());
        assertBefore(childId, leftId, stopList);
    }

    public void testAddRightChildStopLeft() throws NoSuchConfigException {
        Artifact rightChildId = new Artifact("rightChild", "", "", "");
        diamondModel.addConfiguration(rightChildId, Collections.singleton(rightId), Collections.singleton(rightId));

        LinkedHashSet loadList = diamondModel.load(rightChildId);
        assertEquals(asSet(rightChildId), asSet(loadList));
        LinkedHashSet startList = diamondModel.start(rightChildId);
        assertEquals(asSet(rightChildId), asSet(startList));

        LinkedHashSet stopList = diamondModel.stop(leftId);

        // the right, mid, root, and new right child nodes should be started
        assertEquals(asSet(rootId, midId, rightId, rightChildId), diamondModel.getStarted());
        assertEquals(asSet(rootId, rightChildId), diamondModel.getUserStarted());

        // everything should still be loaded
        assertEquals(asSet(all, rightChildId), diamondModel.getLoaded());
        assertEquals(asSet(rootId, childId, rightChildId), diamondModel.getUserLoaded());

        // checke the order of the list
        assertContainsNone(stopList, asSet(rootId, midId, rightId));
        assertEquals(2, stopList.size());
        assertBefore(childId, leftId, stopList);
    }


    public static void assertContainsNone(Collection collection, Collection unexpected) {
        for (Iterator iterator = unexpected.iterator(); iterator.hasNext();) {
            Object item = iterator.next();
            assertFalse("Did not expecte " + item + " in the collection " + collection,
                    collection.contains(item));
        }
    }

    public static void assertBefore(Object before, Object after, LinkedHashSet set) {
        List list = new ArrayList(set);
        int beforeIndex = list.indexOf(before);
        assertTrue("Expected " + before + " to be contained in the list " + list,
                beforeIndex >= 0);

        int afterIndex = list.indexOf(after);
        assertTrue("Expected " + after + " to be contained in the list " + list,
                afterIndex >= 0);

        assertTrue("Expected " + before + " to be before " + after + " in the list " + list,
                beforeIndex < afterIndex);
    }

    public static LinkedHashSet asSet(Object a) {
        return asSet(new Object[] {a});
    }

    public static LinkedHashSet asSet(Object a, Object b) {
        return asSet(new Object[] {a, b});
    }
    public static LinkedHashSet asSet(Object a, Object b, Object c) {
        return asSet(new Object[] {a, b, c});
    }
    public static LinkedHashSet asSet(Object a, Object b, Object c, Object d) {
        return asSet(new Object[] {a, b, c, d});
    }
    public static LinkedHashSet asSet(Object a, Object b, Object c, Object d, Object e) {
        return asSet(new Object[] {a, b, c, d, e});
    }
    public static LinkedHashSet asSet(Object[] list) {
        LinkedHashSet set = new LinkedHashSet();
        for (int i = 0; i < list.length; i++) {
            Object o = list[i];
            if (o instanceof Collection) {
                set.addAll((Collection)o);
            } else {
                set.add(o);
            }
        }
        return set;
    }
}
