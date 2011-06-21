package org.apache.geronimo.system.bundle;

import java.io.File;
import java.io.IOException;

public interface BundleRecorder {
    
    /**
     * Record the bundle in recorded-bundles.properties and add to the bundleRecords map
     * @param bundleFile
     * @param inplace
     * @param startLevel
     * @return the installed bundle id, return -1 if installed failed
     * @throws IOException
     */
    public long recordInstall(File bundleFile, boolean inplace, int startLevel) throws IOException;
    
    
}


