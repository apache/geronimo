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
package org.apache.geronimo.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class TransformerCollection implements ClassFileTransformer {

    private final List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();

    // hack to force load of ArrayList$Itr class. This avoids a potential Classloader deadlock during startup
    // see GERONIMO-3687
    {
        for (ClassFileTransformer transformer : transformers) {
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        boolean changed = false;
        for (ClassFileTransformer transformer : transformers) {
            byte[] transformed = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            if (transformed != null) {
                changed = true;
                classfileBuffer = transformed;
            }
        }
        return changed? classfileBuffer: null;
    }

    public void addTransformer(ClassFileTransformer classFileTransformer) {
        transformers.add(classFileTransformer);
    }

    public void removeTransformer(ClassFileTransformer classFileTransformer) {
        transformers.remove(classFileTransformer);
    }
}
