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


package org.apache.geronimo.kernel.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class RecordingLifecycleMonitor implements LifecycleMonitor{
    private final List<Event> events = new ArrayList<Event>();

    public void addConfiguration(Artifact configurationId) {
        events.add(new Event(Action.ADD, configurationId));
    }

    public void resolving(Artifact configurationId) {
        events.add(new Event(Action.RESOLVING, configurationId));
    }

    public void reading(Artifact configurationId) {
        events.add(new Event(Action.READING, configurationId));
    }

    public void loading(Artifact configurationId) {
        events.add(new Event(Action.LOADING, configurationId));
    }

    public void starting(Artifact configurationId) {
        events.add(new Event(Action.STARTING, configurationId));
    }

    public void stopping(Artifact configurationId) {
        events.add(new Event(Action.STOPPING, configurationId));
    }

    public void unloading(Artifact configurationId) {
        events.add(new Event(Action.UNLOADING, configurationId));
    }

    public void succeeded(Artifact configurationId) {
        events.add(new Event(Action.SUCCEEDED, configurationId));
    }

    public void failed(Artifact configurationId, Throwable cause) {
        events.add(new FailedEvent(configurationId, cause));
    }

    public void finished() {
        events.add(new Event(Action.FINISHED, null));
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Event event: events) {
            buf.append(event.toString()).append("\n");
        }
        return buf.toString();
    }

    private static enum Action {

        ADD,
        RESOLVING,
        READING,
        LOADING,
        STARTING,
        STOPPING,
        UNLOADING,
        SUCCEEDED,
        FAILED,
        FINISHED
    }

    private static class Event {
        private final Action action;
        private final Artifact artifact;

        private Event(Action action, Artifact artifact) {
            this.action = action;
            this.artifact = artifact;
        }

        public Action getAction() {
            return action;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        @Override
        public String toString() {
            if (artifact == null) {
                return action.toString();
            }
            return artifact.toString() + ": " + action.toString();
        }
    }

    private static class FailedEvent extends Event {
        private final Throwable cause;

        private FailedEvent(Artifact artifact, Throwable cause) {
            super(Action.FAILED, artifact);
            this.cause = cause;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String toString() {
            return super.toString() + ": " + cause.getMessage();
        }
    }
}
