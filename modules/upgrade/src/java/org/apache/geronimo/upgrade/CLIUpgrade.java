/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.upgrade;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev:$ $Date:$
 */
public class CLIUpgrade {

    public void execute(String infile, String outfile) throws IOException, XmlException {
        File inFile = new File(infile);
        if (!inFile.exists() || inFile.isDirectory()) {
            throw new IOException("Input file " + inFile + " does not exist");
        }
        InputStream in = new FileInputStream(inFile);
        File outFile = new File(outfile);
        OutputStream out = new FileOutputStream(outFile);
        PrintWriter outWriter = new PrintWriter(out);
        new Upgrade1_0To1_1().upgrade(in, outWriter);
        outWriter.flush();
        outWriter.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0 || args.length > 2) {
            System.out.println("Parameter usage: ");
            System.out.println("inputPlan outputPlan");
            System.out.println("or");
            System.out.println("inputPlan");
            System.out.println("in which case the output will be in the same location as inputPlan with '.upgraded' appended");
            return;
        }
        String inputFile = args[0];
        String outFile = args.length == 2? args[1]: inputFile + ".upgrade";
        new CLIUpgrade().execute(inputFile, outFile);
    }
}
