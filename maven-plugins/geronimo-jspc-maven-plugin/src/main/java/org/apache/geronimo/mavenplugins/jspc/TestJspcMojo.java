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
/*
 * Copyright 2005 Jeff Genender.
 */

package org.apache.geronimo.mavenplugins.jspc;

import java.util.List;

/**
 * @goal testCompile
 * @phase process-classes
 * @execute phase="test-compile"
 * @requiresDependencyResolution test
 * @description Compiles Jsps.
 * @author jgenender@apache.org
 * @author jgenender <jgenender@apache.org>
 * @author Grzegorz Slowikowski
 * @version $Id: TestJspcMojo.java 2422 2006-09-28 23:31:49Z jgenender $
 */
public class TestJspcMojo extends AbstractJspcMojo {


    /**
     * Project classpath.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements; 

    
    protected List getClasspathElements()
    {
        return classpathElements;
    }
}
