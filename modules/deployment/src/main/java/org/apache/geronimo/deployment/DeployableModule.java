package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @version 
 */
public interface DeployableModule {

    /**
	 * Returns the uri of the module, foo.ear, if a nested module then
	 * foo.ear/foo.war
	 *
	 * @return
	 */
	String getURI();

	/**
	 * Returns the root of the module, if isArchived then it return absolute
	 * location of the archive, otherwise the root folder of the module.
	 *
	 * @return
	 */
	File getRoot();

    /**
	 * Returns the root folders for the resources in this module that should be
	 * added to the module context. Returns an empty array if this is an
	 * archived module.
	 *
	 * @return a possibly-empty array of resource folders
	 */
	File[] getModuleContextResources();

	/**
	 * returns all the URLs that contain .class files, if isArchive then this
	 * would return an empty array
	 *
	 * @return
	 */
	File[] getClassesFolders();

	/**
	 * Returns true if this is an archived module. The archive can be a JarFile
	 * or an exploded archive.
	 *
	 * @return boolean
	 */
	boolean isArchived();

	/**
	 * Returns all the child modules for this module. For an ear module, this
	 * would return all of its contained j2ee modules and utility modules. For a
	 * web module, it would return its WEB-INF/lib entries.
	 *
	 * @return a possibly empty array of modules contained within this module
	 */
	DeployableModule[] getModules();

    /**
     *
     * @param path
     * @return
     */
    URL resolve(String path) throws IOException;

    DeployableModule resolveModule(String uri) throws IOException;

    /**
     * Peforms any cleanup on the instance, for a JarFile, would close it.
     */
    void cleanup();
    
}
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @version 
 */
public interface DeployableModule {

    /**
	 * Returns the uri of the module, foo.ear, if a nested module then
	 * foo.ear/foo.war
     *
     * //TODO GERONIMO-1526 Perhaps this should be getName() and use this method instead of getRoot().getName() in builders
	 *
	 * @return
	 */
	String getURI();

	/**
	 * Returns the root of the module, if isArchived then it return absolute
	 * location of the archive, otherwise the root folder of the module.
	 *
	 * @return
	 */
	File getRoot();

    /**
	 * Returns the root folders for the resources in this module that should be
	 * added to the module context. Returns an empty array if this is an
	 * archived module.
	 *
	 * @return a possibly-empty array of resource folders
	 */
	File[] getModuleContextResources();

	/**
	 * returns all the URLs that contain .class files, if isArchive then this
	 * would return an empty array
	 *
	 * @return
	 */
	File[] getClassesFolders();

	/**
	 * Returns true if this is an archived module. The archive can be a JarFile
	 * or an exploded archive.
	 *
	 * @return boolean
	 */
	boolean isArchived();

	/**
	 * Returns all the child modules for this module. For an ear module, this
	 * would return all of its contained j2ee modules and utility modules. For a
	 * web module, it would return its WEB-INF/lib entries.
	 *
	 * @return a possibly empty array of modules contained within this module
	 */
	DeployableModule[] getModules();

    /**
     *
     * @param path
     * @return
     */
    URL resolve(String path) throws IOException;

    /**
     * 
     * @param uri
     * @return
     * @throws IOException
     */
    DeployableModule resolveModule(String uri) throws IOException;

    /**
     * Peforms any cleanup on the instance, for a JarFile, would close it.
     */
    void cleanup();
    
}
