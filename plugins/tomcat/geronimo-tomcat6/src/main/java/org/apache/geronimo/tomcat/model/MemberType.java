
package org.apache.geronimo.tomcat.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MemberType")
public class MemberType {

    @XmlAttribute
    protected String className = StaticMember.class.getName();

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    public String getClassName() {
        return className;
    }

    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public Member getMember(ClassLoader cl) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Map.Entry<QName, String> entry : otherAttributes.entrySet()) {
            String name = entry.getKey().getLocalPart();
            properties.put(name, entry.getValue());
        }
        ObjectRecipe recipe = new ObjectRecipe(className, properties);
        /*
         * Use specified constructor will make sure the host attribute to be set prior other attributes 
         * for the default static member implementation
        */ 
        if (className.equals(StaticMember.class.getName())) {            
            recipe.setConstructorArgNames(new String[] { "host", "port", "aliveTime" });
        }
        recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        Member member = (Member) recipe.create(cl);
        return member;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
