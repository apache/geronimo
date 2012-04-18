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

package org.apache.geronimo.shell.diagnose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Constants;

public class PackageUsesHelper {
            
    private final State state;
    
    public PackageUsesHelper(State state) {
        this.state = state;
    }
    
    public void analyzeConflict(BundleDescription bundle, int level) {
        // ignore fragment bundles
        if (bundle.getHost() != null) {
            return;
        }
        
        PackageGraph graph = new PackageGraph();
        
        Map<ImportPackageSpecification, PackageEdge> edges = new HashMap<ImportPackageSpecification, PackageEdge>();
        ImportPackageSpecification[] importPackages = bundle.getImportPackages();
        for (ImportPackageSpecification importPackage : importPackages) {
            PackageEdge edge = processImportPackage(graph, bundle, importPackage);
            if (edge != null) {
                edges.put(importPackage, edge);
            }
        }
        for (ExportPackageDescription exportPackage : bundle.getExportPackages()) {
            processExportPackage(graph, exportPackage, null);
        }

        for (Map.Entry<String, Map<PackageNode, ImportPackageSpecification>> entry : graph.getPackageMap().entrySet()) {
            Map<PackageNode, ImportPackageSpecification> versions = entry.getValue();
            if (hasMultipleDifferentExporters(versions.keySet())) {
                
                String packageName = entry.getKey();
                System.out.println();
                System.out.println(Utils.formatMessage(level, "Found multiple versions of package " + packageName + " in bundle's dependency graph:"));
                
                for (PackageNode version : versions.keySet()) {
                    System.out.println(Utils.formatErrorMessage(level + 1, "package " + version.getPackageNameAndVersion() + " is exported from " + Utils.bundleToString(version.getPackageExporter())));                            
                }
                
                System.out.println();
                System.out.println(Utils.formatMessage(level, "Package " + packageName + " paths:"));
                
                // just display one path to each conflicting package version found
                for (Map.Entry<PackageNode, ImportPackageSpecification> version : versions.entrySet()) {
                    PackageNode node = version.getKey();
                    ImportPackageSpecification importPackage = version.getValue();
                    if (importPackage == null) {
                        System.out.println(Utils.formatErrorMessage(level + 1, Utils.bundleToString(node.getPackageExporter()) + " exports " + Utils.exportPackageToString(node.getExportPackage())));                            
                        System.out.println();
                    } else {
                        PackageEdge edge = edges.get(importPackage);
                        PackagePath path = edge.findPathToPackage(node);
                        System.out.println(path.toString(level + 1));
                    }
                }
            }
        }
        
    }
    
    private boolean hasMultipleDifferentExporters(Set<PackageNode> versions) {
        if (versions.size() > 1) {
            BundleDescription exporter = null;
            for (PackageNode version : versions) {
                if (exporter == null) {
                    exporter = version.getExportPackage().getExporter();
                } else if (exporter != version.getExportPackage().getExporter()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private PackageEdge processImportPackage(PackageGraph graph, BundleDescription importer, ImportPackageSpecification importPackage) {
        return processImportPackage(graph, importer, importPackage, importPackage);
    }
        
    private PackageEdge processImportPackage(PackageGraph graph, BundleDescription importer, ImportPackageSpecification importPackage, ImportPackageSpecification startImportPackage) {
        ExportPackageDescription exportedPackage = null;
        if (importer.isResolved()) {
            exportedPackage = findExportPackage(importPackage.getName(), importer.getResolvedImports());
        } else {
            exportedPackage = findExportPackage(importPackage, state.getExportedPackages());
            if (exportedPackage == null) {
                exportedPackage = findExportPackage(importPackage, importer.getExportPackages());
            }
        }
        if (exportedPackage == null) {
            String resolution = (String) importPackage.getDirective(Constants.RESOLUTION_DIRECTIVE);
            if (ImportPackageSpecification.RESOLUTION_OPTIONAL.equals(resolution) ||
                ImportPackageSpecification.RESOLUTION_DYNAMIC.equals(resolution)) {
                return null;
            } else {
                throw new RuntimeException(importPackage + " cannot be satisfied for " + Utils.bundleToString(importer));
            }
        }
        return new PackageEdge(processExportPackage(graph, exportedPackage, startImportPackage), importPackage);
    }
    
    private PackageNode processExportPackage(PackageGraph graph, ExportPackageDescription exportedPackage, ImportPackageSpecification startImportPackage) {
        PackageNode node = graph.getNode(exportedPackage);
        if (node != null) {
            return node;
        }
        node = graph.addNode(exportedPackage, startImportPackage);
        
        String[] uses = (String[]) exportedPackage.getDirective("uses");
        if (uses != null) {
            BundleDescription bundle = exportedPackage.getExporter();
            for (String usePackageName : uses) {
                // see uses clause points to import or export package
                ImportPackageSpecification useImportPackage = findImportPackage(usePackageName, bundle.getImportPackages());
                if (useImportPackage == null) {
                    ExportPackageDescription useExportPackage = findExportPackage(usePackageName, bundle.getExportPackages());
                    if (useExportPackage == null) {
                        // Ignore bad package in "uses" directive
                        // throw new RuntimeException("No import or export package for an 'uses' package " + usePackageName + " in " + Utils.bundleToString(bundle));
                    } else {
                        PackageEdge edge = new PackageEdge(processExportPackage(graph, useExportPackage, startImportPackage));
                        node.addEdge(edge);
                    }
                } else {
                    PackageEdge edge = processImportPackage(graph, bundle, useImportPackage, startImportPackage);
                    if (edge != null) {
                        node.addEdge(edge);
                    }
                }
            }
        }
        
        return node;
    }
    
    private static ExportPackageDescription findExportPackage(String packageName, ExportPackageDescription[] exports) {
        for (ExportPackageDescription pkg : exports) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }
    
    private static ImportPackageSpecification findImportPackage(String packageName, ImportPackageSpecification[] imports) {
        for (ImportPackageSpecification pkg : imports) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }
    
    private static ExportPackageDescription findExportPackage(ImportPackageSpecification packageName, ExportPackageDescription[] exports) {
        List<ExportPackageDescription> matches = new ArrayList<ExportPackageDescription>(2);
        for (ExportPackageDescription pkg : exports) {
            if (packageName.getName().equals(pkg.getName()) && packageName.getVersionRange().isIncluded(pkg.getVersion())) {
                matches.add(pkg);
            }
        }
        int size = matches.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return matches.get(0);
        } else {
            Collections.sort(matches, ExportPackageComparator.INSTANCE);
            return matches.get(0);
        }      
    }
    
    private static class ExportPackageComparator implements Comparator<ExportPackageDescription> {
        
        static final ExportPackageComparator INSTANCE = new ExportPackageComparator();
        
        public int compare(ExportPackageDescription object1, ExportPackageDescription object2) {
            return object2.getVersion().compareTo(object1.getVersion());
        }            
    }
    
    private static class PackageGraph {
    
        private Map<String, Map<PackageNode, ImportPackageSpecification>> packageMap = new HashMap<String, Map<PackageNode, ImportPackageSpecification>>();
        private Map<ExportPackageDescription, PackageNode> nodes = new HashMap<ExportPackageDescription, PackageNode>();
        
        public PackageNode getNode(ExportPackageDescription exportPackage) {
            return nodes.get(exportPackage);
        }
        
        public PackageNode addNode(ExportPackageDescription exportPackage, ImportPackageSpecification startImportPackage) {
            PackageNode node = new PackageNode(exportPackage);
            nodes.put(exportPackage, node);
            
            Map<PackageNode, ImportPackageSpecification> versions = packageMap.get(exportPackage.getName());
            if (versions == null) {
                versions = new LinkedHashMap<PackageNode, ImportPackageSpecification>();
                packageMap.put(exportPackage.getName(), versions);
            }
            versions.put(node, startImportPackage);

            return node;
        }

        public Map<String, Map<PackageNode, ImportPackageSpecification>> getPackageMap() {
            return packageMap;
        }
    }
    
    private static class PackageEdge {
        private ImportPackageSpecification importPackage;
        private PackageNode target;
        
        public PackageEdge(PackageNode target) {
            this(target, null);
        }
        
        public PackageEdge(PackageNode target, ImportPackageSpecification importPackage) {
            this.target = target;
            this.importPackage = importPackage;
        }
        
        public PackageNode getTarget() {
            return target;
        }
        
        public ImportPackageSpecification getImportPackage() {
            return importPackage;
        }
        
        public PackagePath findPathToPackage(PackageNode packageNode) {
            return findPathToPackage(new HashMap<Object, Object>(), new PackagePath(), packageNode);
        }
        
        public PackagePath findPathToPackage(Map<Object, Object> context, PackagePath currentPath, PackageNode packageNode) {
            if (currentPath.containsEdgeWithNode(target)) {
                return null;
            }
                        
            currentPath.addLast(this);
            
            PackagePath pathFound = null;
            
            if (target == packageNode) {
                pathFound = new PackagePath(currentPath);
            } else {
                pathFound = target.findPathToPackage(context, currentPath, packageNode);
            }
            
            currentPath.removeLast();
            
            return pathFound;
        }

    }
    
    private static class PackageNode {
        
        private ExportPackageDescription exportPackage;
        // list of package uses
        private List<PackageEdge> edges;
        
        public PackageNode(ExportPackageDescription exportPackage) {
            this.exportPackage = exportPackage;
            this.edges = new ArrayList<PackageEdge>();
        }
        
        public ExportPackageDescription getExportPackage() {
            return exportPackage;
        }
        
        public void addEdge(PackageEdge usedPackage) {
            edges.add(usedPackage);
        }
        
        private PackagePath findPathToPackage(Map<Object, Object> context, PackagePath currentPath, PackageNode packageNode) {
            Boolean visited = (Boolean) context.get(this);
            if (visited != null) {                
                return null;
            }
                
            for (PackageEdge edge : edges) {
                PackagePath path = edge.findPathToPackage(context, currentPath, packageNode);
                if (path != null) {
                    return path;
                }
            }
                
            context.put(this, Boolean.TRUE);
            return null;            
        }
                
        public String toString() {
            return getPackageNameAndVersion();
        }
        
        public String getPackageNameAndVersion() {
            return exportPackage.getName() + "; version=\"" + exportPackage.getVersion() + "\"";
        }
        
        public BundleDescription getPackageExporter() {
            return exportPackage.getExporter();
        }
    }
    
    private static class PackagePath {
        
        private LinkedList<PackageEdge> edges = new LinkedList<PackageEdge>();
        private Set<PackageNode> nodes = new HashSet<PackageNode>();
        
        public PackagePath() {            
        }
        
        public PackagePath(PackagePath path) {
            edges = new LinkedList<PackageEdge>(path.edges);
        }
        
        public void addLast(PackageEdge edge) {
            edges.addLast(edge);
            nodes.add(edge.getTarget());
        }
        
        public PackageEdge removeLast() {
            PackageEdge edge = edges.removeLast();
            nodes.remove(edge.getTarget());
            return edge;
        }
        
        public boolean containsEdgeWithNode(PackageNode target) {
            return nodes.contains(target);
        }
        
        public String toString() {
            return toString(0);
        }
        
        public String toString(int level) {
            StringBuilder builder = new StringBuilder();
            Iterator<PackageEdge> iterator = edges.iterator();
            if (iterator.hasNext()) {
                PackageEdge edge = iterator.next();
                PackageNode node = edge.getTarget();
               
                builder.append(importToString(level, edge, !iterator.hasNext()));                
                builder.append(Utils.LINE_SEPARATOR);

                while (iterator.hasNext()) {
                    PackageEdge nextEdge = iterator.next(); 
                    PackageNode nextNode = nextEdge.getTarget();
                    
                    ImportPackageSpecification importPackage = nextEdge.getImportPackage();
                        
                    builder.append(usesToString(level + 1, node, nextNode, importPackage == null && !iterator.hasNext()));
                    builder.append(Utils.LINE_SEPARATOR);
                        
                    if (importPackage != null) {
                        if (importPackage.getBundle() == nextEdge.getTarget().getPackageExporter()) {
                            builder.append(selfImportToString(level + 1, nextEdge, !iterator.hasNext()));
                        } else {
                            builder.append(importToString(level + 1, nextEdge, !iterator.hasNext()));
                            level++;
                        }
                        builder.append(Utils.LINE_SEPARATOR);
                    }
                    
                    edge = nextEdge;
                    node = nextNode;
                }
            }
            return builder.toString();
        }
        
        private static String importToString(int level, PackageEdge edge, boolean isLast) {
            StringBuilder builder = new StringBuilder();            
            builder.append(Utils.bundleToString(edge.getImportPackage().getBundle())).append(" imports package ").append(Utils.importPackageToString(edge.getImportPackage()));
            if (edge.getImportPackage().isResolved()) {
                builder.append(" and is wired to ");
            } else {
                builder.append(" wants to wire to ");
            }
            builder.append(Utils.bundleToString(edge.getTarget().getPackageExporter()));
            return formatMessage(level, builder.toString(), isLast);
        }
        
        private static String selfImportToString(int level, PackageEdge edge, boolean isLast) {
            StringBuilder builder = new StringBuilder();            
            builder.append(Utils.bundleToString(edge.getImportPackage().getBundle())).append(" imports package ").append(Utils.importPackageToString(edge.getImportPackage()));
            if (edge.getImportPackage().isResolved()) {
                builder.append(" and is wired to itself");
            } else {
                builder.append(" wants to wire to itself");
            }
            return formatMessage(level, builder.toString(), isLast);
        }
        
        private static String usesToString(int level, PackageNode source, PackageNode target, boolean isLast) {
            StringBuilder builder = new StringBuilder();  
            builder.append("package ").append(source.getExportPackage().getName());
            builder.append(" uses package ").append(target.getExportPackage().getName());
            return formatMessage(level, builder.toString(), isLast);
        }

        private static String formatMessage(int level, String message, boolean isLast) {
            if (isLast) {
                return Utils.formatErrorMessage(level, message);
            } else {
                return Utils.formatMessage(level, message);
            }
        }
    }
}
