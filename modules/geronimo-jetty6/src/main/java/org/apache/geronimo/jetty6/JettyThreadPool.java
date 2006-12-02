package org.apache.geronimo.jetty6;

import org.mortbay.thread.ThreadPool;

/**
 * JettyThreadPool
 * 
 * Class to implement Jetty org.mortbay.jetty.thread.ThreadPool interface
 * and delegate to a Geronimo thread pool impl.
 *
 */
public class JettyThreadPool implements ThreadPool {

    private org.apache.geronimo.pool.ThreadPool geronimoThreadPool;
    
    public JettyThreadPool(org.apache.geronimo.pool.ThreadPool geronimoThreadPool) {
        this.geronimoThreadPool = geronimoThreadPool;
    }

    
    public boolean dispatch(Runnable work) {
        this.geronimoThreadPool.execute(work);
        return true;
        //what has changed?
//        try {
//            this.geronimoThreadPool.execute(work);
//            return true;
//        }
//        catch (Exception e) {
//            log.warn(e);
//            return false;
//        }
    }

    /**
     * Jetty method. Caller wants to wait until the
     * thread pool has stopped.
     * 
     * @see org.mortbay.thread.ThreadPool#join()
     */
    public void join() throws InterruptedException {
        throw new UnsupportedOperationException("join not supported");
    }

    public int getThreads() {
        return this.geronimoThreadPool.getPoolSize();
    }

    public int getIdleThreads() {
        //TODO: not supported in geronimo thread pool
        return 0;
    }

    public boolean isLowOnThreads() {
        // TODO: not supported in geronimo thread pool
        return false;
    }
    
}
