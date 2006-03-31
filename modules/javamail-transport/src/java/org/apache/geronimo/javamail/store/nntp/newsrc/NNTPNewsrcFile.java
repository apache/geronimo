/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.nntp.newsrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class NNTPNewsrcFile extends NNTPNewsrc {
    // source for the file data
    File source;

    /**
     * Construct a NNTPNewsrc object that is targetted at a file-based backing
     * store.
     * 
     * @param source
     *            The source File for the .newsrc data.
     */
    public NNTPNewsrcFile(File source) {
        this.source = source;
    }

    /**
     * Retrieve an input reader for loading the newsrc file.
     * 
     * @return A BufferedReader object for reading from the newsrc file.
     * @exception IOException
     */
    public BufferedReader getInputReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(source)));
    }

    /**
     * Obtain a writer for saving a newsrc file.
     * 
     * @return The output writer targetted to the newsrc file.
     * @exception IOException
     */
    public Writer getOutputWriter() throws IOException {
        // open this for overwriting
        return new OutputStreamWriter(new FileOutputStream(source, false));
    }
}
