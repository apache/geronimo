/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.kernel.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean(j2eeType = "Repository")
public class RepositoryGBean extends AbstractServiceWrapper<WritableListableRepository> implements WritableListableRepository {
    public RepositoryGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, WritableListableRepository.class);
    }

    @Override
    public SortedSet<Artifact> list() {
        return get().list();
    }

    @Override
    public SortedSet<Artifact> list(Artifact query) {
        return get().list(query);
    }

    @Override
    public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        get().copyToRepository(source, destination, monitor);
    }

    @Override
    public void copyToRepository(InputStream source, int size, Artifact destination, FileWriteMonitor monitor) throws IOException {
        get().copyToRepository(source, size, destination, monitor);
    }

    @Override
    public boolean contains(Artifact artifact) {
        return get().contains(artifact);
    }

    @Override
    public File getLocation(Artifact artifact) {
        return get().getLocation(artifact);
    }
}
