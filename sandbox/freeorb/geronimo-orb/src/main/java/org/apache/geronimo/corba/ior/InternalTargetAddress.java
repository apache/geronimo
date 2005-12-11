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

import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.apache.geronimo.corba.AbstractORB;


public abstract class InternalTargetAddress {

    public static class ObjectKeyAddress extends InternalTargetAddress {

        byte[] key;

        public ObjectKeyAddress(byte[] object_key) {
        	   this.key = object_key;
		}

		public void writeObjectKey(OutputStream out) {
            OctetSeqHelper.write(out, key);
        }

        public void write(OutputStream out) {
            out.write_short((short) 0);
            writeObjectKey(out);
        }
    }

    static class ProfileAddress extends InternalTargetAddress {

        IIOPProfile key;
        
        public ProfileAddress(IIOPProfile profile) { this.key = profile; }

        public void writeObjectKey(OutputStream out) {
            OctetSeqHelper.write(out, key.getObjectKey());
        }

        public void write(OutputStream out) {
            out.write_short((short) 1);
            key.write(out);
        }
    }

    static class IORAddressingInfoAddress extends InternalTargetAddress {

        InternalIOR ior;
        int selected;

        public void writeObjectKey(OutputStream out) {
            IIOPProfile profile = (IIOPProfile) ior.getProfile(selected);
            OctetSeqHelper.write(out, profile.getObjectKey());
        }

        public void write(OutputStream out) {
            out.write_short((short) 2);
            out.write_long(selected);
            ior.write(out);
        }
    }

    /**
     * write just the object-key part of the target address (for GIOP 1.0 and 1.1)
     */
    public abstract void writeObjectKey(OutputStream out);

    /**
     * Write the full target address including discriminator
     */
    public abstract void write(OutputStream out);

}
