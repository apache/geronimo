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
package org.apache.geronimo.corba.io;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.CustomMarshal;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.ValueBase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.channel.MarkHandler;
import org.apache.geronimo.corba.channel.OutputChannelMarker;


/**
 * This class is not done.
 */
public class ValueWriter {

	
	static final Log log = LogFactory.getLog(ValueWriter.class);
	
    static final int TAG_VALUE = 0x7fffff00;

    static final int TAG_CODEBASE_PRESENT = 1;

    static final int TAG_SINGLE_ID_PRESENT = 2;

    static final int TAG_MULTI_ID_PRESENT = 6;

    static final int TAG_CHUNKED = 8;

    static String[] EMPTY_SARR = new String[0];

    private boolean isSet(int value, int bits) {
        return (value & bits) == bits;
    }

    private Map instanceTable = new IdentityHashMap();

    /**
     * indirection table for value headers �15.3.4.3
     */
    private Map valueInfoTable = new HashMap();

    private final OutputStreamBase out;

    private OutputChannelMarker chunkMark;

    private boolean currentValueIsChunked;

    private int chunkingLevel;

    private int startPosOfCurrentChunk;

    private int lastEndTagPos;

    private ValueHandler valueHandler;

    private int valueOfLastEndTag;

    public ValueWriter(OutputStreamBase out) {
        this.out = out;
    }

    static class IDArray implements Serializable {

        String[] ids;

        int hashCode;

        IDArray(String[] ids) {
            this.ids = ids;

            hashCode = 0;
            for (int i = 0; i < ids.length; i++)
                hashCode ^= ids[i].hashCode();
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object other) {

            if (!(other instanceof IDArray) || other == null)
                return false;

            return java.util.Arrays.equals(ids, ((IDArray) other).ids);
        }
    }

    public void writeValue(java.io.Serializable value, String repositoryID) {
        if (value == null) {
            out.write_long(0);
            return;
        }

        if (write_indirection(instanceTable, value)) {
            return;
        }

        boolean isStreamable = (value instanceof StreamableValue);
        boolean isCustom = (value instanceof CustomMarshal);

        if (!(isStreamable || isCustom)) {
            BoxedValueHelper helper = getHelper(value, null);
            if (helper == null) {
                writeRMIValue(value, repositoryID);
            } else {
                writeValue(value, helper);
            }
            return;
        }

        ValueBase base = (ValueBase) value;
        String[] ids = base._truncatable_ids();
        String valueID = ids[0];
        boolean isTruncatable = ids.length > 1;

        boolean sameType = false; // (valueID.equals (repositoryID));

        boolean isChunked = (isCustom | (isTruncatable & !sameType));

        int tag;

        if (!sameType || (chunkingLevel > 1 && isTruncatable)) {

            if (isTruncatable) {
                tag = TAG_VALUE | TAG_MULTI_ID_PRESENT;

            } else {
                tag = TAG_VALUE | TAG_SINGLE_ID_PRESENT;

                if (ids.length > 1) {
                    ids = new String[1];
                    ids[0] = valueID;
                }
            }
        } else {
            tag = TAG_VALUE;
            ids = EMPTY_SARR;
        }

        // System.out.println ("write_value (1) "+repositoryID);

        int pos = startValue(tag, ids, null, isChunked);
        instanceTable.put(value, new Integer(pos));

        if (isStreamable) {
            ((StreamableValue) value)._write(out);
        } else {
            ((CustomMarshal) value).marshal(out);
        }

        endValue();
    }

    private void writeRMIValue(java.io.Serializable value, String id) {

        if (value instanceof java.lang.String) {
            org.omg.CORBA.WStringValueHelper.write(out, (String) value);
            return;
        }

        if (valueHandler == null) {
            valueHandler = out.getValueHandler();
        }

        //
        // Needs writeReplace?
        //
        java.io.Serializable repValue = valueHandler.writeReplace(value);

        //
        // Repeat base checks if value was replaced
        //
        if (value != repValue) {
            if (repValue == null) {
                out.write_long(0);
                return;
            }

            if (write_indirection(instanceTable, repValue)) {
                return;
            }

            value = repValue;
        }

        //
        // Get the class object for the value
        //
        Class clz = value.getClass();

        //
        // 0x7fffff00 + SINGLE_ID
        //
        int tag = TAG_VALUE | TAG_SINGLE_ID_PRESENT;

        String codebase = javax.rmi.CORBA.Util.getCodebase(clz);
        if (codebase != null && codebase.length() != 0)
            tag |= TAG_CODEBASE_PRESENT;

        //
        // Determine the repository ID
        //
        String[] ids = new String[1];
        ids[0] = valueHandler.getRMIRepositoryID(clz);

        //
        // Determine if chunked encoding is needed
        //
        // TODO: this was always true in Trifork codebase, find out why!

        boolean isChunked = valueHandler.isCustomMarshaled(clz);

        // System.out.println ("write_value (2) "+ids[0]);

        int pos = startValue(tag, ids, codebase, isChunked);
        instanceTable.put(value, new Integer(pos));
        valueHandler.writeValue(out, value);
        endValue();
    }

    public void writeValue(Serializable value, BoxedValueHelper helper) {

        if (value == null) {
            out.write_long(0);
            return;
        }

        if (write_indirection(instanceTable, value)) {
            return;
        }

        if (helper == null) {
            helper = getHelper(value, null);
        }

        if (helper == null)
            throw new org.omg.CORBA.MARSHAL("Can't locate helper");

        String[] ids = new String[1];
        ids[0] = helper.get_id();
        int tag = TAG_VALUE | TAG_SINGLE_ID_PRESENT;

        // System.out.println ("write_value (3) "+ids[0]);

        int pos = startValue(tag, ids, null, false);
        instanceTable.put(value, new Integer(pos));
        helper.write_value(out, value);
        endValue();

    }

    private int startValue(int tag, String[] ids, String codebase,
                           boolean forceChunk)
    {

        currentValueIsChunked |= forceChunk;

        if (currentValueIsChunked) {
            tag |= TAG_CHUNKED;
            chunkingLevel += 1;

            //
            // Since chunks cannot be nested, we need to finish off
            // the previous chunk before we can start...
            //
            if ((chunkingLevel > 1) && (chunkMark != null))
                endChunk();

            // TODO: understand why we need to write a chunk end here.
            // the next thing written to the output stream is a value
            // tag, and that should be fine as an chunk-end-marker
        }

        out.write_long(tag);
        int startPos = out.__stream_position() - 4;

        if (isSet(tag, TAG_CODEBASE_PRESENT)) {
            write_value_metadata_string(codebase);
        }

        if (isSet(tag, TAG_MULTI_ID_PRESENT)) {

            IDArray idlist = new IDArray(ids);
            if (!write_indirection(valueInfoTable, idlist)) {
                out.align(4);
                valueInfoTable
                        .put(idlist, new Integer(out.__stream_position()));
                out.write_long(ids.length);

                for (int i = 0; i < ids.length; i++) {
                    write_value_metadata_string(ids[i]);
                }
            }

        } else if (isSet(tag, TAG_SINGLE_ID_PRESENT)) {

            write_value_metadata_string(ids[0]);
        }

        if (currentValueIsChunked) {

            //
            // start the next chunk as soon as anything is written
            // to the stream
            //
            lastEndTagPos = 0;

            startChunk();
        }

        return startPos;
    }

    void endValue() {

        if (!currentValueIsChunked)
            return;

        //
        // We need to make sure no chunks are started in the middle of
        // writing the end tag, and if we need to have a chunk here.
        //

        if (false && lastEndTagPos > 0 && out.__stream_position() == lastEndTagPos + 4) {

            // if we just terminated the previous value (which must
            // then have been nested inside the current value), then
            // we simply step back one and write the new end tag.

            valueOfLastEndTag++;

            //
            // TODO: figure a way to rewrite the last end-tag
            // TODO: revisit buf.pos = lastEndTagPos;

            if (log.isDebugEnabled()) {
                log.debug("rewriting endTag" + valueOfLastEndTag
                          + ", chunkingLevel=" + chunkingLevel);
            }

            out.write_long(valueOfLastEndTag);

        } else {

            // end the current chunk
            endChunk();

            //
            // Write the end tag and remember where it is
            //
            valueOfLastEndTag = -chunkingLevel;

            out.write_long(valueOfLastEndTag);
            if (chunkingLevel > 1)
                lastEndTagPos = out.__stream_position() - 4;
        }

        //
        // At the end of a chunked value, we must have ended the chunk
        // inside the value, i.e., right after a value we must never
        // be inside a chunk.
        //
        if (startPosOfCurrentChunk != 0)
            out.__fatal("startPosOfCurrentChunk is " + startPosOfCurrentChunk
                        + "; there should be no chunk here!");

        if (chunkingLevel == 1) {
            currentValueIsChunked = false;
            lastEndTagPos = 0;
            valueOfLastEndTag = 0;
        }

        chunkingLevel -= 1;
    }

    private void write_value_metadata_string(String string) {
        if (!write_indirection(valueInfoTable, string)) {
            out.align(4);
            valueInfoTable.put(string, new Integer(out.__stream_position()));
            out.write_string(string);
        }
    }

    MarkHandler chunkHandler = new MarkHandler() {
        public void bufferFull(OutputChannelMarker state) throws IOException {

            // end the chunk by writing the size at the start of the chunk.
            // endChunk will call state.release()
            endChunk();

            // start a new chunk here //
            // TODO: outer mark handler needs to be run first...
            startChunk();
        }
    };

    private void startChunk() {
        if (currentValueIsChunked == false)
            out.__fatal("not chunked");

        out.align(4);
        // TODO: revisit chunkMark = out.mark(chunkHandler);
        startPosOfCurrentChunk = out.__stream_position();
        out.write_long(0);
    }

    private void endChunk() {
        if (currentValueIsChunked == false)
            out.__fatal("not chunked");

        // compute chunk size
        int size = out.__stream_position() - startPosOfCurrentChunk;

        // TODO: align chunk size? That which follows a chunk must
        // be a 4-byte integer (chunk end marker) so we will do the
        // alignment...
        // TODO: revisit size += out.computeAlignment(size, 4);

        try {
            chunkMark.putInt(0, size);
        }
        catch (IOException e) {
            throw out.translate_exception(e);
        }

        chunkMark.release();
        chunkMark = null;
    }

    private boolean write_indirection(Map table, Serializable value) {
        Integer pos = (Integer) table.get(value);
        if (pos == null) {
            return false;
        }

        out.write_long(-1);
        int off = pos.intValue() - out.__stream_position();
        out.write_long(off);

        return true;
    }

    private BoxedValueHelper getHelper(Serializable value,
                                       String id)
    {
        /*
        Class helper = null;

        // TODO: Cache this info somewhere (contextclassloader)?

        try {
            String name = value.getClass().getName() + "Helper";
            Class c = Util.classForName(name);
            if (BoxedValueHelper.class.isAssignableFrom(c))
                helper = c;
        }
        catch (ClassNotFoundException ex) {
            // ignore //
        }

        if (helper == null && id != null) {
            try {
                String name = Util.idToClassName(id) + "Helper";
                helper = Util.classForName(name);
            }
            catch (ClassNotFoundException ex) {
                // ignore //
            }
        }

        if (helper != null) {
            try {
                return (BoxedValueHelper) helper.newInstance();
            }
            catch (ClassCastException ex) {
                // ignore //
            }
            catch (InstantiationException ex) {
                // ignore //
            }
            catch (IllegalAccessException ex) {
                // ignore //
            }
        }
        */
        return null;
    }

}
