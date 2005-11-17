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
package org.apache.geronimo.jetty.interceptor;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;


/**
 * @version $Rev$ $Date$
 */
public class ThreadClassloaderBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int threadIndex;
    private final int classLoaderIndex;
    private final ClassLoader classLoader;

    public ThreadClassloaderBeforeAfter(BeforeAfter next, int threadIndex, int classLoaderIndex, ClassLoader classLoader) {
        this.next = next;
        this.threadIndex = threadIndex;
        this.classLoaderIndex = classLoaderIndex;
        this.classLoader = classLoader;
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        Thread thread = Thread.currentThread();
        context[threadIndex] = thread;
        context[classLoaderIndex] = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        ((Thread)context[threadIndex]).setContextClassLoader((ClassLoader)context[classLoaderIndex]);
    }

}
