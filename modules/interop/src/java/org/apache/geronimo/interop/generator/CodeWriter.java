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

import java.io.File;


public abstract class CodeWriter {
    protected GenOptions _genOptions;

    protected File _file;

    protected String _fileName;
    protected String _fileExt = ".java";

    public CodeWriter(GenOptions genOptions, String fileName, String ext) {
        _genOptions = genOptions;
        _fileName = fileName;
        _fileExt = ext;
    }

    public GenOptions getGenOptions() {
        return _genOptions;
    }

    public void setGenOptions(GenOptions genOptions) {
        _genOptions = genOptions;
    }

    public void setFileName(String val) {
        _fileName = val;
    }

    public String getFileName() {
        return _fileName;
    }

    public void setFileExt(String val) {
        _fileExt = val;
    }

    public String getFileExt() {
        return _fileExt;
    }

    public abstract void openFile() throws GenException;

    public abstract void closeFile() throws GenException;
}

