
package org.apache.ws.echosample;

import java.io.Serializable;

public class SmallEchoStruct implements Serializable{
	private String val1;
	private String val2;
    /**
     * @return
     */
    public String getVal1() {
        return val1;
    }

    /**
     * @return
     */
    public String getVal2() {
        return val2;
    }

    /**
     * @param string
     */
    public void setVal1(String string) {
        val1 = string;
    }

    /**
     * @param string
     */
    public void setVal2(String string) {
        val2 = string;
    }

}
