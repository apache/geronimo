package org.apache.geronimo.enterprise.deploy.server.j2ee;

/**
 * 
 *
 * @version $Revision: 1.1 $
 */
public class ClassSpace {
    private String name;
    private String parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ClassSpace)) return false;

        final ClassSpace classSpace = (ClassSpace)o;

        if(!name.equals(classSpace.name)) return false;
        if(!parent.equals(classSpace.parent)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 29 * result + parent.hashCode();
        return result;
    }

    public String toString() {
        return "ClassSpace";
    }
}
