package org.apache.geronimo.console.classloaderview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.console.util.TreeEntry;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.util.ClassLoaderRegistry;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

@RemoteProxy
public class ClassLoaderViewHelper {
    Map<String, TreeEntry> nodeHash;
    private static final String NO_CHILD = "none";
    
    private static final String NORMAL_TYPE = "normal";
    
    private static final CmpTreeEntry cmp = new CmpTreeEntry();

    @RemoteMethod
    public String getTrees(boolean inverse) {
        nodeHash = new HashMap<String, TreeEntry>();

        List list = ClassLoaderRegistry.getList();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            if (!inverse)
                updateTree((ClassLoader) iter.next());
            else
                inverseTree((ClassLoader) iter.next());
        }

        return this.printClassLoaders();
    }

    public TreeEntry inverseTree(ClassLoader classloader) {
        TreeEntry node = nodeHash.get(classloader.toString());
        if (null != node)
            return node;
        node = new TreeEntry(classloader.toString(), "root");
        node = addClasses(node, classloader);
        nodeHash.put(node.getName(), node);

        if (classloader instanceof MultiParentClassLoader) {
            MultiParentClassLoader mpclassloader = (MultiParentClassLoader) classloader;
            ClassLoader[] parents = mpclassloader.getParents();
            if (null != parents && 0 < parents.length) {
                for (int i = 0; i < parents.length; i++) {
                    TreeEntry parentNode = inverseTree(parents[i]);
                    node.addChild(parentNode);
                }
            }
        } else if (classloader.getParent() != null) {
            TreeEntry parentNode = inverseTree(classloader.getParent());
            node.addChild(parentNode);
        }

        return node;
    }

    public TreeEntry updateTree(ClassLoader classloader) {

        TreeEntry node = nodeHash.get(classloader.toString());
        if (null != node)
            return node;

        node = new TreeEntry(classloader.toString(), NORMAL_TYPE);
        node = addClasses(node, classloader);
        nodeHash.put(node.getName(), node);

        if (classloader instanceof MultiParentClassLoader) {
            MultiParentClassLoader mpclassloader = (MultiParentClassLoader) classloader;
            ClassLoader[] parents = mpclassloader.getParents();
            if (null != parents && 0 < parents.length) {
                for (int i = 0; i < parents.length; i++) {
                    TreeEntry parentNode = updateTree(parents[i]);
                    parentNode.addChild(node);
                }
            }
        } else if (classloader.getParent() != null) {
            TreeEntry parentNode = updateTree(classloader.getParent());
            parentNode.addChild(node);
        } else {
            node.setType("root");
        }

        return node;
    }

    private TreeEntry addClasses(TreeEntry node, ClassLoader loader) {
        try {
            java.lang.reflect.Field CLASSES_VECTOR_FIELD = ClassLoader.class.getDeclaredField("classes");

            if (CLASSES_VECTOR_FIELD.getType() != java.util.Vector.class) {
                return node;
            }
            CLASSES_VECTOR_FIELD.setAccessible(true);

            final java.util.Vector classes = (java.util.Vector) CLASSES_VECTOR_FIELD.get(loader);
            if (classes == null)
                return node;

            final Class[] result;

            synchronized (classes) {
                result = new Class[classes.size()];
                classes.toArray(result);
            }

            CLASSES_VECTOR_FIELD.setAccessible(false);

            TreeEntry classNames = new TreeEntry("Classes", NORMAL_TYPE);
            TreeEntry interfaceNames = new TreeEntry("Interfaces", NORMAL_TYPE);
            node.addChild(classNames);
            node.addChild(interfaceNames);

            for (int i = 0; i < result.length; i++) {
                if (result[i].isInterface())
                    interfaceNames.addChild(new TreeEntry(result[i].toString(), NORMAL_TYPE));
                else
                    classNames.addChild(new TreeEntry(result[i].toString(), NORMAL_TYPE));
            }
            if (classNames.getChildren().size() < 1)
                classNames.addChild(new TreeEntry(NO_CHILD, NORMAL_TYPE));
            if (interfaceNames.getChildren().size() < 1)
                interfaceNames.addChild(new TreeEntry(NO_CHILD, NORMAL_TYPE));
            return node;
        } catch (Exception e) {
            return node;
        }
    }

    /*
     * Usually we can directly give the java objects to clients via dwr, but we found that it is too slow when the
     * objects become larger and more complex because of the bad efficiency of dwr. So we need to translate the java
     * object to json text by hand in such cases. But it is more specific to the implementation of dojo, so it is not
     * very recommended.
     */
    String printClassLoaders() {
        // generate an ordered id
        List<TreeEntry> rootNodes = new ArrayList<TreeEntry>();
        for (TreeEntry entry : nodeHash.values()) {
            if (entry.getType().equals("root"))
                rootNodes.add(entry);
        }
        markupId(-1, rootNodes); // here root nodes have already been sorted

        List<TreeEntry> allNodes = rootNodes;
        for (TreeEntry entry : nodeHash.values()) {
            if (!entry.getType().equals("root"))
                allNodes.add(entry);
        }
        StringBuilder sb = new StringBuilder(512);

        sb.append("{label:\"name\",identifier:\"id\",items:[");
        Iterator<TreeEntry> list = allNodes.iterator();
        while (list.hasNext()) {
            TreeEntry curr = list.next();
            sb.append("{name:\"").append(curr.getName())
            .append("\",id:\"").append(curr.getId())
            .append("\",type:\"").append(curr.getType())
            .append("\",children:[");

            Iterator<TreeEntry> children = curr.getChildren().iterator();
            // the first child is Classes and the second one is Interfaces
            printClasses(sb, children.next());
            sb.append(",");
            printClasses(sb, children.next());
            while (children.hasNext()) {
                TreeEntry child = children.next();
                sb.append(",{_reference:\"").append(child.getId()).append("\"}");
            }
            if (list.hasNext())
                sb.append("]},");
            else
                sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private void printClasses(StringBuilder sb, TreeEntry classes) {
        sb.append("{name:\"").append(classes.getName())
        .append("\",id:\"").append(classes.getId())
        .append("\",children:[");
        Iterator<TreeEntry> children = classes.getChildren().iterator();
        while (children.hasNext()) {
            TreeEntry child = children.next();
            sb.append("{name:\"").append(child.getName())
            .append("\",id:\"").append(child.getId())
            .append("\"}");
            if (children.hasNext())
                sb.append(",");
        }
        sb.append("]}");
    }

    private int markupId(int pre, List<TreeEntry> list) {
        Collections.sort(list, cmp);
        for (TreeEntry child : list) {
            if (null == child.getId())
                child.setId(String.valueOf(++pre));
            pre = markupId(pre, child.getChildren());
        }
        return pre;
    }

    static class CmpTreeEntry implements Comparator<TreeEntry> {
        public int compare(TreeEntry x, TreeEntry y) {
            if (x.getName() == null)
                return -1;
            return x.getName().compareTo(y.getName());
        }
    }
}
