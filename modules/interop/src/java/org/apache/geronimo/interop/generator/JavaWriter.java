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
    protected GenOptions _genOptions;

    protected PrintWriter _pw;

    protected boolean _needIndent = true;
    protected int _indentPos = 0;
    protected String _indentStr = "";
    protected String _spaces = "                                                        ";

    public JavaWriter(GenOptions genOptions, String fileName, String ext) {
        super(genOptions, fileName, ext);
    }

    protected File getFile()
            throws GenException {
        File file = null;
        GenOptions go = getGenOptions();
        String fileName = getFileName() + getFileExt();

        try {
            file = new File(go.getGenDir(), fileName);

            if (file.exists() && !go.isOverwrite()) {
                fileName = fileName + ".new";

                file = new File(go.getGenDir(), fileName);
            }
        } catch (Exception ex) {
            throw new GenException("Error: Unable to open output dir: " + go.getGenDir() + ", file: " + fileName, ex);
        }

        return file;
    }

    public void openFile()
            throws GenException {
        OutputStream os = null;

        if (_file != null) {
            //System.out.println( "Output file already opened" );
            return;
        }

        _file = getFile();

        if (_file == null) {
            throw new GenException("Error: Unable to obtain output file.");
        }

        if (getGenOptions().isVerbose()) {
            System.out.println("Generating: " + _file);
        }

        os = null;

        //if (_file.isFile())
        //{
        _file.getParentFile().mkdirs();
        //}

        if (_file.exists() && !_file.canWrite()) {
            throw new GenException("Error: Unable to write to file: " + _file);
        }

        if (!_file.exists() && !_file.getParentFile().canWrite()) {
            throw new GenException("Error: Unable to write to directory: " + _file.getParentFile());
        }

        try {
            os = new FileOutputStream(_file);
        } catch (Exception ex) {
            throw new GenException("Error: Unable to init output file: " + _file, ex);
        }

        try {
            _pw = new PrintWriter(new OutputStreamWriter(os));
        } catch (Exception ex) {
            throw new GenException("Error: Unable to init output file: " + _file, ex);
        }
    }

    public void closeFile()
            throws GenException {
        if (_pw != null) {
            try {
                _pw.flush();
                _pw.close();
            } catch (Exception e) {
                throw new GenException("Error: Unable to close output file: " + _file, e);
            }

            _pw = null;
        }

        _file = null;
    }

    public void indent() {
        _indentPos += 4;
        if (_indentPos > _spaces.length()) {
            _indentPos -= 4;
        }
        _indentStr = _spaces.substring(0, _indentPos);
    }

    public void outdent() {
        _indentPos -= 4;
        if (_indentPos < 0) {
            _indentPos = 0;
        }
        _indentStr = _spaces.substring(0, _indentPos);
    }

    public void begin() {
        _needIndent = true;
        println("{");
        indent();
    }

    public void end() {
        outdent();
        _needIndent = true;
        println("}");
    }

    public void newln() {
        println("");
        _needIndent = true;
    }

    public void comment(String msg) {
        println("// " + msg);
    }

    public void println(String line) {
        if (_needIndent) {
            _needIndent = false;
            _pw.print(_indentStr);
        }

        _pw.println(line);
        _needIndent = true;
    }

    public void print(String line) {
        if (_needIndent) {
            _needIndent = false;
            _pw.print(_indentStr);
        }

        _pw.print(line);
    }
}
