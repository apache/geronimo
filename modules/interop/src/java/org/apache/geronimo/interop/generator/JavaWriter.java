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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class JavaWriter extends CodeWriter {
    private GenOptions      genOptions;
    private PrintWriter     pw;
    private boolean         needIndent = true;
    private int             indentPos = 0;
    private String          indentStr = "";
    private String          spaces = "                                                        ";

    public JavaWriter(GenOptions genOptions, String fileName, String ext) {
        super(genOptions, fileName, ext);
    }

    protected File getFile()
            throws GenException {
        File file = null;
        GenOptions go = getGenOptions();
        String fileName = getFileName() + getFileExt();

        try {
            file = new File(go.getGenSrcDir(), fileName);

            if (file.exists() && !go.isOverwrite()) {
                fileName = fileName + ".new";

                file = new File(go.getGenSrcDir(), fileName);
            }
        } catch (Exception ex) {
            throw new GenException("Error: Unable to open output dir: " + go.getGenSrcDir() + ", file: " + fileName, ex);
        }

        return file;
    }

    public void openFile()
            throws GenException {
        OutputStream os = null;

        if (file != null) {
            //System.out.println( "Output file already opened" );
            return;
        }

        file = getFile();

        if (file == null) {
            throw new GenException("Error: Unable to obtain output file.");
        }

        if (getGenOptions().isVerbose()) {
            System.out.println("Generating: " + file);
        }

        os = null;

        //if (_file.isFile())
        //{
        file.getParentFile().mkdirs();
        //}

        if (file.exists() && !file.canWrite()) {
            throw new GenException("Error: Unable to write to file: " + file);
        }

        if (!file.exists() && !file.getParentFile().canWrite()) {
            throw new GenException("Error: Unable to write to directory: " + file.getParentFile());
        }

        try {
            os = new FileOutputStream(file);
        } catch (Exception ex) {
            throw new GenException("Error: Unable to init output file: " + file, ex);
        }

        try {
            pw = new PrintWriter(new OutputStreamWriter(os));
        } catch (Exception ex) {
            throw new GenException("Error: Unable to init output file: " + file, ex);
        }
    }

    public void closeFile()
            throws GenException {
        if (pw != null) {
            try {
                pw.flush();
                pw.close();
            } catch (Exception e) {
                throw new GenException("Error: Unable to close output file: " + file, e);
            }

            pw = null;
        }

        file = null;
    }

    public void indent() {
        indentPos += 4;
        if (indentPos > spaces.length()) {
            indentPos -= 4;
        }
        indentStr = spaces.substring(0, indentPos);
    }

    public void outdent() {
        indentPos -= 4;
        if (indentPos < 0) {
            indentPos = 0;
        }
        indentStr = spaces.substring(0, indentPos);
    }

    public void begin() {
        needIndent = true;
        println("{");
        indent();
    }

    public void end() {
        outdent();
        needIndent = true;
        println("}");
    }

    public void newln() {
        println("");
        needIndent = true;
    }

    public void comment(String msg) {
        println("// " + msg);
    }

    public void println(String line) {
        if (needIndent) {
            needIndent = false;
            pw.print(indentStr);
        }

        pw.println(line);
        needIndent = true;
    }

    public void print(String line) {
        if (needIndent) {
            needIndent = false;
            pw.print(indentStr);
        }

        pw.print(line);
    }
}
