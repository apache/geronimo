package org.apache.geronimo.corba.test;

import org.apache.geronimo.corba.ORB;
import org.omg.CORBA.Object;

import junit.framework.TestCase;

public class ORBTest extends TestCase {

    public void testObjectToString() {
        ORB orb = new ORB();
        final String origIOR = "IOR:000000000000003149444C3A696E732E6F70656E6F72622E6F72672F63616C6C6261636B2F43616C6C6261636B4D616E616765723A312E300000000000000001000000000000005C000102000000000E3139322E3136382E362E31303000041500000014004F4F01D2AB1DFD07010000504F41FEE046654A0000000100000001000000200000000000010001000000020001000F00010020000101090000000100010100";
        final Object obj1 = orb.string_to_object(origIOR);
        String str = orb.object_to_string(obj1);
        assertTrue("Not an IOR: '" + str + "'", str.startsWith("IOR:"));
        assertEquals("Not same IOR as original", origIOR, str);
        Object obj2 = orb.string_to_object(str);
        assertNotSame(obj1, obj2);
    }
}
