/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.mavenplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.jelly.JellyContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;


/**
 * @version $Revision$ $Date$
 */
public class VelocityFilter {
    private File sourceDir;
    private File targetDir;
    private boolean force;
    private Context context;

    public void setSourceDir(String sourceDir) {
        File dir = new File(sourceDir);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("sourceDir is not a directory: " + dir.getAbsolutePath());
        }
        this.sourceDir = dir;
    }

    public void setTargetDir(String targetDir) {
        File dir = new File(targetDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalArgumentException("Could not create targetDir: " + dir.getAbsolutePath());
            }
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("targetDir is not a directory: " + dir.getAbsolutePath());
        }
        this.targetDir = dir;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setContext(JellyContext context) {
        this.context = new JellyContextAdapter(context);
    }

    public void execute() throws Exception {
        long processStart = System.currentTimeMillis();

        if (sourceDir == null) {
            throw new IllegalStateException("sourceDir was not set");
        }
        if (targetDir == null) {
            throw new IllegalStateException("targetDir was not set");
        }
        if (context == null) {
            throw new IllegalStateException("context was not set");
        }

        // setup velocity
        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, sourceDir.getAbsolutePath());
        velocity.init();

        List plans = new LinkedList();
        findPlans(sourceDir, plans);
        for (Iterator iterator = plans.iterator(); iterator.hasNext();) {
            File plan = (File) iterator.next();
            processPlan(velocity, plan);
        }

        System.out.println("    Preprocess plans elapse time: " + (System.currentTimeMillis() - processStart) / 1000 + " sec");
        System.out.println();
    }

    private void processPlan(VelocityEngine velocity, File plan) throws Exception {
        // Get the plan name, which is basically plan.getAbsolutePath() - sourceDir.getAbsolutePath()
        String planName = extractPlanName(sourceDir, plan);

        // load the template
        Template template = velocity.getTemplate(planName);

        // determine the output file
        File outputFile = new File(targetDir, planName);

        // create the output directory
        File outputDir = outputFile.getParentFile();
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IllegalArgumentException("Could not create outputDir: " + outputDir.getAbsolutePath());
            }
        }
        if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException("outputDir is not a directory: " + outputDir.getAbsolutePath());
        }

        if (force || outputFile.lastModified() < plan.lastModified()) {
            System.out.println("    Preprocessing " + planName);

            // process the plan
            PrintStream out = null;
            FileReader templateReader = null;
            try {
                out = new PrintStream(new FileOutputStream(outputFile));

                PrintWriter writer = new PrintWriter(out);
                template.merge(context, writer);
                writer.flush();
            } finally {
                close(out);
                close(templateReader);
            }
        }
    }

    private static String extractPlanName(File sourceDir, File plan) {
        String sourcePath = sourceDir.getAbsolutePath();
        String planPath = plan.getAbsolutePath();
        if (!planPath.startsWith(sourcePath)) {
            throw new IllegalStateException("Plan is not located in sourceDir: sourceDir=" + sourcePath + " planPath=" + planPath);
        }
        String planName = planPath.substring(sourcePath.length() + 1);
        if (planName.charAt(0) == '\\' || planName.charAt(0) == '/') {
            planName = planName.substring(1);
        }
        return planName;
    }

    private static void findPlans(File dir, Collection resultsFiles) {
        File[] files = dir.listFiles();
        if (null == files) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                findPlans(files[i], resultsFiles);
            } else if (files[i].getName().endsWith(".xml")
            || files[i].getName().endsWith(".list")
            || files[i].getName().endsWith(".properties")) {
                resultsFiles.add(files[i]);
            }
        }
    }

    private static void close(Reader thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void close(OutputStream thing) {
        if (thing != null && thing != System.out) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static class JellyContextAdapter implements Context {
        private JellyContext jellyContext;
        private HashMap privateContext = new HashMap();

        public JellyContextAdapter(JellyContext jellyContext) {
            this.jellyContext = jellyContext;
        }

        public boolean containsKey(Object key) {
            if (key == null) {
                return false;
            }

            if (privateContext.containsKey(key)) {
                return true;
            }

            return jellyContext.getVariable(key.toString()) != null;
        }

        public Object get(String key) {
            if (key == null) {
                return null;
            }

            if (privateContext.containsKey(key)) {
                return privateContext.get(key);
            }

            return jellyContext.getVariable(key);
        }

        public Object[] getKeys() {
            HashSet keys = new HashSet(jellyContext.getVariables().keySet());
            keys.addAll(privateContext.keySet());
            return keys.toArray();
        }

        public Object put(String key, Object value) {
            if (key == null || value == null) {
                return null;
            }
            return privateContext.put(key, value);
        }

        public Object remove(Object key) {
            if (key == null) {
                return null;
            }
            return privateContext.remove(key);
        }
    }
}
