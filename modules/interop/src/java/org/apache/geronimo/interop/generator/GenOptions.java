/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.generator;

public class GenOptions {
    protected String _genDir = "./";
    protected boolean _overwrite = false;
    protected boolean _verbose = false;

    public GenOptions() {
    }

    public GenOptions(String genDir, boolean overwrite, boolean verbose) {
        _genDir = genDir;
        _overwrite = overwrite;
        _verbose = verbose;
    }

    public String getGenDir() {
        return _genDir;
    }

    public void setGenDir(String genDir) {
        _genDir = genDir;
    }

    public boolean isOverwrite() {
        return _overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        _overwrite = overwrite;
    }

    public boolean isVerbose() {
        return _verbose;
    }

    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }

}
