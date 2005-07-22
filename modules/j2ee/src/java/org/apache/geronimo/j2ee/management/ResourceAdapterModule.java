package org.apache.geronimo.j2ee.management;

/**
 * Represents the JSR-77 type with the same name
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public interface ResourceAdapterModule extends J2EEModule {
    /**
     * A list of Resource Adapters included in this RAR
     * @see "JSR77.3.18.1.1"
     * @return the ObjectNames of the Resource Adapters in this RAR
     */
    String[] getResourceAdapters();
}