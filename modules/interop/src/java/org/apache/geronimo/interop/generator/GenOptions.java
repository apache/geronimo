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

import org.apache.geronimo.interop.rmi.iiop.compiler.StubCompiler;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

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

    private String      genSrcDir = "./src";
    private String      genClassDir = "./classes";
    private boolean     overwrite = false;
    private boolean     verbose = false;
    private boolean     simpleidl = false;
    private String      classpath = "";
    private boolean     generate = true;
    private boolean     compile = false;
    private boolean     compileDebug = false;
    private boolean     loadclass = false;

    private List        interfaces = new LinkedList();

    public GenOptions( String defaultSrcDir, String args[] )
        throws GenWarning, GenException {
        genSrcDir = defaultSrcDir;
        parseOptions( args );
    }

    public String getGenSrcDir() {
        return genSrcDir;
    }

    public String getGenClassDir() {
        return genClassDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isSimpleIdl() {
        return simpleidl;
    }

    public String getClasspath() {
        return classpath;
    }

    public boolean isGenerate() {
        return generate;
    }

    public boolean isCompile() {
        return compile;
    }

    public boolean isCompileDebug() {
        return compileDebug;
    }

    public boolean isLoadclass() {
        return loadclass;
    }

    public List getInterfaces() {
        return interfaces;
    }

    protected void parseOptions(String args[]) throws GenException, GenWarning {
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
                simpleidl = true;
            } else if (args[i].equals("-gs")) {
                if ((i + 1) < args.length) {
                    genSrcDir = args[++i];
                } else {
                    throw new GenException( "-gs requires an source output diretory." );
                }
            } else if (args[i].equals("-cp")) {
                if ((i + 1) < args.length) {
                    classpath = args[++i];
                } else {
                    throw new GenException( "-cp requires a classpath directory." );
                }
            } else if (args[i].equals("-gc")) {
                if ((i + 1) < args.length) {
                    genClassDir = args[++i];
                } else {
                    throw new GenException( "-gc requires an class output diretory." );
                }
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else if (args[i].equals("-o")) {
                overwrite = true;
            } else if (args[i].startsWith("-")) {
                String msg = "Ignoring unrecognized options: '" + args[i] + "'";
                if (genWarning != null) {
                    // just a cheap way of chaining the warnings...
                    genWarning = new GenWarning( msg, genWarning);
                } else {
                    genWarning = new GenWarning( msg );
                }
            } else {
                interfaces.add(args[i]);
            }
        }
    }
}
