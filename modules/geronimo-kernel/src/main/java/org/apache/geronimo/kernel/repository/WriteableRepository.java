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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A repository that accepts new entries.
 *
 * @version $Rev$ $Date$
 */
public interface WriteableRepository extends Repository {
    /**
     * Copies a file from the server's filesystem into the repository.
     * Obviously to use this remotely, you must have some other way
     * to upload the file to the server's filesystem, even if the
     * the server is just going to turn around and upload it to some
     * other remote location.
     *
     * @param source       A file representing the data for the new repository
     *                     entry
     * @param destination  A fully-resolved artifact that tells the repository
     *                     where it should save the data to
     * @param monitor      Tracks the progress of the installation
     */
    public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException;

    /**
     * Copies the contents of an arbitrary stream into the repository.
     * Obviously to use this remotely, you must have some other way
     * to upload the content to the server's JVM, even if the the server
     * is just going to turn around and upload it to some other remote
     * location.  The source will be closed when the write completes.
     *
     * @param source       A stream representing the data for the new
     *                     repository entry
     * @param destination  A fully-resolved artifact that tells the repository
     *                     where it should save the data to
     * @param monitor      Tracks the progress of the installation
     */
    public void copyToRepository(InputStream source, int size, Artifact destination, FileWriteMonitor monitor) throws IOException;
}
