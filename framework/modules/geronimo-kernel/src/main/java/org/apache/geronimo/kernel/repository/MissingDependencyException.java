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

package org.apache.geronimo.kernel.repository;

import java.util.Stack;
import java.io.Serializable;

import org.apache.geronimo.gbean.AbstractNameQuery;

/**
 * @version $Rev$ $Date$
 */
public class MissingDependencyException extends Exception {
    private static final long serialVersionUID = -2557777157677213124L;
    private Artifact query;
    private Stack<Artifact> stack;


    public MissingDependencyException(Artifact query) {
        super();
        this.query = query;
    }

    public MissingDependencyException(Artifact query, Stack<Artifact> stack) {
        super();
        this.query = query;
        this.stack = stack;
    }

    public MissingDependencyException(Artifact query, Artifact parent) {
        super();
        this.query = query;
        this.stack = new Stack<Artifact>();
        if (parent != null) {
            this.stack.add(parent);
        }
    }
    public MissingDependencyException(String message, Artifact query, Stack<Artifact> stack) {
        super(message);
        this.query = query;
        this.stack = stack;
    }

    public MissingDependencyException(String message, Artifact query, Artifact parent) {
        super(message);
        this.query = query;
        this.stack = new Stack<Artifact>();
        if (parent != null) {
            this.stack.add(parent);
        }
    }

    public String getMessage() {
        String s = super.getMessage();
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s).append("\n");
        }
        if (query != null) {
            sb.append("Missing dependency: ").append(query);
        }
        if (stack != null && !stack.isEmpty()) {
            sb.append("\nParent stack:\n");
            for (Artifact parent: stack) {
                sb.append("    ").append(parent).append("\n");
            }

        }
        return sb.toString();
    }


    public Artifact getQuery() {
        return query;
    }

    public void setQuery(Artifact query) {
        this.query = query;
    }

    public Stack<Artifact> getStack() {
        return stack;
    }

    public void setStack(Stack<Artifact> stack) {
        this.stack = stack;
    }
}
