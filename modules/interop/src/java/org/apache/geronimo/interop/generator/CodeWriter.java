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
    private GenOptions  genOptions;    
    private String      fileName;
    private String      fileExt = ".java";

    protected File        file;

    public CodeWriter(GenOptions genOptions, String fileName, String ext) {
        this.genOptions = genOptions;
        this.fileName = fileName;
        fileExt = ext;
    }

    public GenOptions getGenOptions() {
        return genOptions;
    }

    public void setGenOptions(GenOptions genOptions) {
        this.genOptions = genOptions;
    }

    public void setFileName(String val) {
        fileName = val;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileExt(String val) {
        fileExt = val;
    }

    public String getFileExt() {
        return fileExt;
    }

    public abstract void openFile() throws GenException;

    public abstract void closeFile() throws GenException;
}

