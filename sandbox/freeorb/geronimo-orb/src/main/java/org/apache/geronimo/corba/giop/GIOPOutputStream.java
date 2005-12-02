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
package org.apache.geronimo.corba.giop;

import java.io.IOException;

import org.omg.CORBA.NO_RESOURCES;
import org.omg.GIOP.MsgType_1_1;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ClientInvocation;
import org.apache.geronimo.corba.channel.MarkHandler;
import org.apache.geronimo.corba.channel.OutputChannel;
import org.apache.geronimo.corba.channel.OutputChannelMarker;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.OutputStreamBase;

public class GIOPOutputStream extends OutputStreamBase {

	final OutputChannel ch;

	private int pos_in_giop_message;

	private final GIOPVersion version;

	private final AbstractORB orb;

	boolean message_finished;

	private OutputChannelMarker mark;

	private RequestID requestID;

	private int fragment_start;

	private int bytes_in_previous_fragments;

	private boolean insertHeaderPadding;

	protected GIOPOutputStream(AbstractORB orb, OutputChannel ch,
			GIOPVersion version) {
		this.orb = orb;
		this.ch = ch;
		this.version = version;
	}

	class FragmentHandler implements MarkHandler {

		public void bufferFull(OutputChannelMarker state) throws IOException {
			complete_message(state);
			if (!message_finished) {
				begin_fragment();
			}
		}

		void begin_fragment() throws IOException {

			// reset position and mark
			pos_in_giop_message = 0;

			// mark this as the beginning og a GIOP message
			// so that we can rewrite the size info later
			ch.mark(this);

			if (version.minor == 2) {

				// write new header
				ch.writeInt(GIOPMessageTransport.GIOP_MAGIC);
				ch.writeInt(0x01020000 | MsgType_1_1._Fragment);

				// write empty size
				ch.writeInt(0);

				// write requestID (FragmentHeader)
				ch.writeInt(requestID.value());

				pos_in_giop_message = 16;
				fragment_start = 16;

			} else if (version.minor == 1) {

				// write new header
				ch.writeInt(GIOPMessageTransport.GIOP_MAGIC);
				ch.writeInt(0x01010000 | MsgType_1_1._Fragment);

				// write empty size
				ch.writeInt(0);

				// GIOP 1.1 fragments have no fragment header
				// TODO: make sure we let noone else send fragments
				// until this fragment is finished

				pos_in_giop_message = 12;
				fragment_start = 12;

			} else {
				//
				// we cannot send fragments in GIOP 1.0

				throw new org.omg.CORBA.INTERNAL();
			}

		}

	}

	//
	public void beginGIOPStream(int message_type, RequestID requestID) {

		this.requestID = requestID;
		this.pos_in_giop_message = 0;
		this.message_finished = false;
		this.mark = ch.mark(new FragmentHandler());

		byte flags = 0;

		write_long(GIOPMessageTransport.GIOP_MAGIC);
		write_octet((byte) version.major);
		write_octet((byte) version.minor);
		write_octet(flags);
		write_octet((byte) message_type);

		// message size
		write_long(0);
	}

	public void finishGIOPMessage() {
		if (message_finished) {
			throw new IllegalStateException();
		}

		message_finished = true;
		try {
			complete_message(mark);

			// push message through transport layer
			ch.flush();

		} catch (IOException e) {
			throw translate_exception(e);
		}

		// relinquish the right to write to the underlying transport;
		// this will let other threads write a message
		ch.relinquish();
	}

	public void close() {
		if (message_finished) {
			return;
		}
		finishGIOPMessage();
	}

	/**
	 * rewrite the message header to reflect message size and the
	 * hasMoreFragments bit
	 */
	private void complete_message(OutputChannelMarker state) throws IOException {

		boolean lastFragment = message_finished;

		// write "has_more_fragments"
		if (!lastFragment) {
			state.putByte(6, (byte) 2);
		} else {
			state.putByte(6, (byte) 0);
		}

		// write size
		int padding = 0;
		int minor = version.minor;

		switch (minor) {
		case 0:
			// big trouple! What do we do?
			// the buffer has run full, but in GIOP 1.0
			// messages cannot be fragmented. We're out
			// of luck here, really.

			// convert pending message into a close request, and
			// thrown an error condition. In a future version,
			// we might consider adding a dynamic buffer in this
			// case to swallow the rest of the input stream...
			state.putByte(7, (byte) MsgType_1_1._CloseConnection);
			state.putInt(8, 0); // size zero
			ch.close();
			state.release();

			throw new NO_RESOURCES();

		case 1:
			// GIOP 1.1 fragments are not padded in the end.
			padding = 0;
			break;

		case 2:
			// GIOP 1.2
			if (!lastFragment) {
				padding = computeAlignment(pos_in_giop_message, 8);
			}
		}

		// actually write the size into the GIOP header
		state.putInt(8, pos_in_giop_message + padding - 12);

		// let the underlying buffer flush contents (makes space for more
		// fragments)
		state.release();

		// now that the output stream has been released, we can
		// write the padding needed for framented messages in GIOP 1.2
		if (padding != 0) {
			ch.skip(padding);
			pos_in_giop_message += padding;
		}

		bytes_in_previous_fragments += (pos_in_giop_message - fragment_start);
		pos_in_giop_message = 0;
		fragment_start = 0;
	}

	public void align(int align) {

		if (insertHeaderPadding) {
			insertHeaderPadding = false;
			align = 8;
		}
		
		try {
			int skip = computeAlignment(pos_in_giop_message, align);
			if (skip != 0) {
				skip(skip);
			}
		} catch (IOException e) {
			throw translate_exception(e);
		}

	}

	public void write(byte[] data, int off, int len) throws IOException {
		ch.write(data, off, len);
		pos_in_giop_message += len;
	}

	public void skip(int count) throws IOException {
		align(1);
		ch.skip(count);
		pos_in_giop_message += count;
	}

	public OutputChannelMarker mark(MarkHandler handler) {
		return ch.mark(handler);
	}

	public void flush() throws IOException {
		ch.flush();
	}

	public GIOPVersion getGIOPVersion() {
		return version;
	}

	public AbstractORB __orb() {
		return orb;
	}

	public void write(int value) throws IOException {
		ch.write(value);
		pos_in_giop_message += 1;
	}

	public void write_octet(byte value) {
		align(1);
		try {
			ch.write(value);
		} catch (IOException e) {
			throw translate_exception(e);
		}
		pos_in_giop_message += 1;
	}

	public void write_short(short value) {
		align(2);
		try {
			ch.writeShort(value);
		} catch (IOException e) {
			throw translate_exception(e);
		}
		pos_in_giop_message += 2;
	}

	public void write_long(int value) {
		align(4);
		try {
			ch.writeInt(value);
		} catch (IOException e) {
			throw translate_exception(e);
		}
		pos_in_giop_message += 4;
	}

	public void write_longlong(long value) {
		align(8);
		try {
			ch.writeLong(value);
		} catch (IOException e) {
			throw translate_exception(e);
		}
		pos_in_giop_message += 8;
	}

	public int __stream_position() {
		return bytes_in_previous_fragments
				+ (pos_in_giop_message - fragment_start);
	}

	public void setInsertHeaderPadding(boolean b) {
		this.insertHeaderPadding = true;
	}

}
