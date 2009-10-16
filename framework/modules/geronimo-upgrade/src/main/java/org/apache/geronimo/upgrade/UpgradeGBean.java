/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.xmlbeans.XmlException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class UpgradeGBean {

    public void execute(String[] args) throws Exception {
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
        execute(inputFile, outFile);
    }

    public void execute(String infile, String outfile) throws IOException, XmlException {
        File inFile = new File(infile);
        if (!inFile.exists() || inFile.isDirectory()) {
            throw new IOException("Input file " + inFile + " does not exist");
        }
        InputStream in = new FileInputStream(inFile);
        File outFile = new File(outfile);
        Writer out = new FileWriter(outFile);
        PrintWriter outWriter = new PrintWriter(out);
        new Upgrade1_0To1_1().upgrade(in, outWriter);
        outWriter.flush();
        outWriter.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
        new UpgradeGBean().execute(args);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(UpgradeGBean.class);
//        infoBuilder.addOperation("execute", new Class[] {String[].class});
//        infoBuilder.addOperation("execute", new Class[] {String.class, String.class});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
