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

package org.apache.geronimo.pool;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev$ $Date$
 */
public class ThreadPool implements GeronimoExecutor, GBeanLifecycle {
    private PooledExecutor executor;
    private ClassLoader classLoader;
    private ObjectName objectName;

    public ThreadPool(int poolSize, String poolName, long keepAliveTime, ClassLoader classLoader, String objectName) {
        PooledExecutor p = new PooledExecutor(poolSize);
        p.abortWhenBlocked();
        p.setKeepAliveTime(keepAliveTime);
        p.setMinimumPoolSize(poolSize);
        p.setThreadFactory(new ThreadPoolThreadFactory(poolName, classLoader));
        try {
            this.objectName = ObjectName.getInstance(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Bad object name injected: "+e.getMessage());
        }

        executor = p;
        this.classLoader = classLoader;
    }

    public String getName() {
        return objectName.getKeyProperty("name");
    }

    public String getObjectName() {
        return objectName.getCanonicalName();
    }

    public void execute(Runnable command) throws InterruptedException {
        PooledExecutor p;
        synchronized(this) {
            p = executor;
        }
        if (p == null) {
            throw new IllegalStateException("ThreadPool has been stopped");
        }
        Runnable task = new ContextClassLoaderRunnable(command, classLoader);
        p.execute(task);
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        PooledExecutor p;
        synchronized(this) {
            p = executor;
            executor = null;
            classLoader = null;
        }
        if (p != null) {
            p.shutdownNow();
        }
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    private static final class ThreadPoolThreadFactory implements ThreadFactory {
        private final String poolName;
        private final ClassLoader classLoader;

        private int nextWorkerID = 0;

        public ThreadPoolThreadFactory(String poolName, ClassLoader classLoader) {
            this.poolName = poolName;
            this.classLoader = classLoader;
        }

        public Thread newThread(Runnable arg0) {
            Thread thread = new Thread(arg0, poolName + " " + getNextWorkerID());
            thread.setContextClassLoader(classLoader);
            return thread;
        }

        private synchronized int getNextWorkerID() {
            return nextWorkerID++;
        }
    }

    private static final class ContextClassLoaderRunnable implements Runnable {
        private Runnable task;
        private ClassLoader classLoader;

        public ContextClassLoaderRunnable(Runnable task, ClassLoader classLoader) {
            this.task = task;
            this.classLoader = classLoader;
        }

        public void run() {
            Runnable myTask = task;
            ClassLoader myClassLoader = classLoader;

            // clear fields so they can be garbage collected
            task = null;
            classLoader = null;

            if (myClassLoader != null) {
                // we asumme the thread classloader is already set to our final class loader
                // because the only to access the thread is wrapped with the Runnable or via the initial thread pool
                try {
                    myTask.run();
                } finally {
                    Thread.currentThread().setContextClassLoader(myClassLoader);
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ThreadPool.class);

        infoFactory.addAttribute("poolSize", int.class, true);
        infoFactory.addAttribute("poolName", String.class, true);
        infoFactory.addAttribute("keepAliveTime", long.class, true);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addInterface(GeronimoExecutor.class);

        infoFactory.setConstructor(new String[] {"poolSize", "poolName", "keepAliveTime", "classLoader", "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
