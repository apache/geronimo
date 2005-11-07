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
package org.apache.geronimo.kernel.repository;

import java.net.URI;
import java.io.File;
import java.io.IOException;

/**
 * A repository that accepts new entries.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface WriteableRepository {
    /**
     * Copies a file from the server's filesystem into the repository.
     * Obviously to use this remotely, you must have some other way
     * to upload the file to the server's filesystem, even if the
     * the server is just going to turn around and upload it to some
     * other remote location.
     */
    public void copyToRepository(File source, URI destination) throws IOException;
}
