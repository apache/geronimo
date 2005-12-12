/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.ior;

import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.IOP.TAG_CODE_SETS;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class CodeSetsComponent extends Component {

    private final CodeSetComponentInfo info;

    public CodeSetsComponent(CodeSetComponentInfo info) {
        this.info = info;
    }

    public int tag() {
        return TAG_CODE_SETS.value;
    }

    public static Component read(AbstractORB orb, byte[] data) {
        InputStream eis = new EncapsulationInputStream(orb, data);
        return new CodeSetsComponent(CodeSetComponentInfoHelper.read(eis));
    }

    protected void write_content(OutputStream eo) {
        CodeSetComponentInfoHelper.write(eo, info);
    }

    public int getNativeCharCS() {
    		return info.ForCharData.native_code_set;
	}

	public int[] getCharConversionCS() {
		return info.ForCharData.conversion_code_sets;
	}

	public int[] getWCharConversionCS() {
		return info.ForWcharData.conversion_code_sets;
	}

	public int getNativeWCharCS() {
		return info.ForWcharData.native_code_set;
	}

}
