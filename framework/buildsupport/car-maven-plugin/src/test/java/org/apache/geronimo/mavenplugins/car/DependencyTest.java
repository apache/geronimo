/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.mavenplugins.car;

import java.util.LinkedHashSet;

import junit.framework.TestCase;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.kernel.repository.ImportType;

/**
 * @version $Rev$ $Date$
 */
public class DependencyTest extends TestCase {
    
    public void testEquals() throws Exception {
        Dependency d1 = newDependency(null);
        Dependency d2 = newDependency(null);
        assertTrue(d1.equals(d2));
        d1.setStart(true);
        assertTrue(d1.equals(d2));
        assertTrue(d2.equals(d1));
        d2.setStart(true);
        assertTrue(d1.equals(d2));
        Dependency d3 = newDependency(null);
        Dependency d4 = newDependency("1.1");
        assertFalse(d3.equals(d4));
        Dependency d5 = newDependency("1.1");
        Dependency d6 = newDependency("1.1");
        assertTrue(d5.equals(d6));
    }

    public void testRemove() throws Exception {
        Dependency d1 = newDependency(null);
        Dependency d2 = newDependency(null);
        d1.setStart(true);
        LinkedHashSet<Dependency> dependencies = new LinkedHashSet<Dependency>();
        dependencies.add(d1);
        dependencies.remove(d2);
        assertTrue(dependencies.isEmpty());
        dependencies.add(d2);
        dependencies.remove(d1);
        assertTrue(dependencies.isEmpty());
    }
    
    public void testStart() throws Exception {
        Dependency d1 = newDependency(null, null);
        Dependency d2 = newDependency(null, null);
        assertTrue(d1.equals(d2));
        d1.setStart(true);
        assertTrue(d1.equals(d2));
        d2.setStart(true);
        assertTrue(d1.equals(d2));
        d1.setStart(false);
        assertFalse(d1.equals(d2));
        d2.setStart(null);
        assertFalse(d1.equals(d2));
    }

    public void testDependencyTypeConversion() throws Exception {
        Dependency d1 = newDependency(null);
        DependencyType td = d1.toDependencyType();
        Dependency d2 = newDependencyFromType(td);
        assertTrue(d1.equals(d2));
    }

    private Dependency newDependency(String version) {
        return newDependency(version, null);
    }
    private Dependency newDependency(String version, Boolean start) {
        Dependency d = new Dependency();
        d.setGroupId("foo");
        d.setArtifactId("bar");
        d.setVersion(version);
        d.setType("car");
        d.setStart(start);
        d.setImport("all");
        return d;
    }

    public static Dependency newDependencyFromType(DependencyType dependencyType) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(dependencyType.getGroupId());
        dependency.setArtifactId(dependencyType.getArtifactId());
        dependency.setVersion(dependencyType.getVersion());
        dependency.setType(dependencyType.getType());
        dependency.setStart(dependencyType.isStart());
        dependency.setImport(ImportType.ALL.toString());
        return dependency;
    }

}
