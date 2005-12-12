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

import java.util.LinkedList;
import java.util.List;

public class GenOptions {
    // -gs  genSrcDir
    // -gc  genClassDir
    // -o   overwrite
    // -v   verbose
    // -s   simpleidl
    // -cp  classpath
    // -g   generate
    // -c   compile
    // -cd  compiledebug
    // -lc  loadclass

    private String genSrcDir = "./src";
    private String genClassDir = "./classes";
    private boolean overwrite = false;
    private boolean verbose = false;
    private boolean simpleIdl = false;
    private String classpath = "";
    private boolean generate = true;
    private boolean compile = false;
    private boolean compileDebug = false;
    private boolean loadclass = false;

    private List interfaces = new LinkedList();

    public GenOptions() {
    }

    public GenOptions(String defaultSrcDir, String args[]) throws GenWarning, GenException {
        genSrcDir = defaultSrcDir;
        parseOptions(args);
    }

    public String getGenSrcDir() {
        return genSrcDir;
    }

    public void setGenSrcDir(String genSrcDir) {
        this.genSrcDir = genSrcDir;
    }

    public String getGenClassDir() {
        return genClassDir;
    }

    public void setGenClassDir(String genClassDir) {
        this.genClassDir = genClassDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isSimpleIdl() {
        return simpleIdl;
    }

    public void setSimpleIdl(boolean simpleIdl) {
        this.simpleIdl = simpleIdl;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    public boolean isCompileDebug() {
        return compileDebug;
    }

    public void setCompileDebug(boolean compileDebug) {
        this.compileDebug = compileDebug;
    }

    public boolean isLoadclass() {
        return loadclass;
    }

    public void setLoadclass(boolean loadclass) {
        this.loadclass = loadclass;
    }

    public List getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List interfaces) {
        this.interfaces = interfaces;
    }

    public void parseOptions(String args[]) throws GenException, GenWarning {
        GenWarning genWarning = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-g")) {
                generate = true;
            } else if (args[i].equals("-c")) {
                compile = true;
            } else if (args[i].equals("-cd")) {
                compileDebug = true;
            } else if (args[i].equals("-l")) {
                loadclass = true;
            } else if (args[i].equals("-s")) {
                simpleIdl = true;
            } else if (args[i].equals("-gs")) {
                if ((i + 1) < args.length) {
                    genSrcDir = args[++i];
                } else {
                    throw new GenException("-gs requires an source output diretory.");
                }
            } else if (args[i].equals("-cp")) {
                if ((i + 1) < args.length) {
                    classpath = args[++i];
                } else {
                    throw new GenException("-cp requires a classpath directory.");
                }
            } else if (args[i].equals("-gc")) {
                if ((i + 1) < args.length) {
                    genClassDir = args[++i];
                } else {
                    throw new GenException("-gc requires an class output diretory.");
                }
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else if (args[i].equals("-o")) {
                overwrite = true;
            } else if (args[i].startsWith("-")) {
                String msg = "Ignoring unrecognized options: '" + args[i] + "'";
                if (genWarning != null) {
                    // just a cheap way of chaining the warnings...
                    genWarning = new GenWarning(msg, genWarning);
                } else {
                    genWarning = new GenWarning(msg);
                }
            } else {
                interfaces.add(args[i]);
            }
        }
    }
}
