/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.repository;

import java.net.URL;
import java.io.Serializable;

/**
 * Description of a component stored in a repository.
 * Information used to describe a component that may be deployed into a server.
 * Attributes are:
 * <table>
 * <tr><td>Group</td><td>Optional</td><td>A project or product name</td></tr>
 * <tr><td>Name</td><td>Required</td><td>Component name</td></tr>
 * <tr><td>Version</td><td>Required</td><td>Component version</td></tr>
 * <tr><td>Description</td><td>Optional</td><td>Wordy description</td></tr>
 * <tr><td>Homepage</td><td>Optional</td><td>Product home page</td></tr>
 * <tr><td>Location</td><td>Required</td><td>Specific location to download from</td></tr>
 * </table>
 * @version $Revision: 1.1 $ $Date: 2003/08/12 04:16:47 $
 */
public class ComponentDescription implements Serializable {
    private String group;
    private String name;
    private String version;
    private String description;
    private URL homepage;
    private String location;

    public ComponentDescription() {
    }

    public ComponentDescription(String name, String version, String location) {
        this.name = name;
        this.version = version;
        this.location = location;
    }

    /**
     * Return the description of this component
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description for this component
     * @param description a description of this component
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the group for this component
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the group to which this component belongs.
     * @param group the group for this project
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Return the homepage for this component
     * @return the homepage for this component
     */
    public URL getHomepage() {
        return homepage;
    }

    /**
     * Set the homepage for this component
     * @param homepage the homepage
     */
    public void setHomepage(URL homepage) {
        this.homepage = homepage;
    }

    /**
     * Return the location of this component in the repository
     * @return the path to the component
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set the location of this component in the repository
     * @param location the path to this component relative to the root of the repository
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Return the name of this component
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this component
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the version id for this component. No meaning should be attached
     * to the syntax of the version
     * @return the version id
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version for this component
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
