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
import java.net.InetSocketAddress;
import java.nio.ByteOrder;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.GIOP.ReplyStatusType_1_2Helper;

import org.apache.geronimo.corba.ClientInvocation;
import org.apache.geronimo.corba.Invocation;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.OutputChannel;
import org.apache.geronimo.corba.channel.Transport;
import org.apache.geronimo.corba.channel.TransportManager;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.ior.InternalTargetAddress;

public class GIOPMessageTransport implements InputHandler {

	public boolean isAssignedHere(RequestID id) {
		return id.isAssignedHere(isClient);
	}

	static final int GIOP_MAGIC = ('G' << 24) | ('I' << 16) | ('O' << 8) | 'P';

	static final int POIG_MAGIC = ('P' << 24) | ('O' << 16) | ('I' << 8) | 'G';

	int next_request_id = 2;

	RequestID giop_1_1_request_id = new RequestID(0);

	public class MessageHeader implements InputController {

		GIOPInputStream in;

		private byte major;

		private byte minor;

		private byte flags;

		private byte type;

		private int size;

		private boolean hasMoreFragments;

		private RequestID requestID;

		private int message_start;

		private int position;

		public MessageHeader() {
		}

		public void process(Transport transport) {
			in = new GIOPInputStream(orb, GIOPVersion.V1_0, this, transport.getInputChannel());

			try {
				in.position(0); // reset alignment
				in.limit(12); //
				int magic = in.read_long();
				switch (magic) {
				case GIOP_MAGIC:
				case POIG_MAGIC:
					// THAT's OK!
					break;

				default:
					sendErrorAndClose();
					return;
				}

				this.major = in.read_octet();
				this.minor = in.read_octet();

				in.setGIOPVersion(GIOPVersion.get(major, minor));
				
				this.flags = in.read_octet();
				this.type = in.read_octet();

				boolean littleEndian = ((flags & 1) == 1);
				in.setOrder(littleEndian ? ByteOrder.LITTLE_ENDIAN
						: ByteOrder.BIG_ENDIAN);

				this.size = in.read_long();
				in.limit(size + 12);

				this.hasMoreFragments = false;
				if (minor > 0) {
					hasMoreFragments = ((flags & 2) == 2);
				}

				this.message_start = in.position();

				switch (type) {
				case MsgType_1_1._Fragment:

					if (minor == 2) {
						this.requestID = new RequestID(in.read_long());

						// this position counts as the start of this
						// message with respect to calculation of the
						// stream position
						this.message_start = in.position();
						transport.signalResponse(requestID, this);

					} else if (minor == 1) {

						this.message_start = in.position();

						// in GIOP 1.1 the there is no FragmentHeader, so
						// we need to "guess", that the incoming fragment
						// is the same as that read by a previosus message
						this.requestID = giop_1_1_request_id;

						// reset the guess
						if (!hasMoreFragments) {
							giop_1_1_request_id = new RequestID(0);
						}

						transport.signalResponse(this.requestID, this);

					} else {
						// todo: send message error
						throw new INTERNAL();
					}
					return;

				case MsgType_1_1._Reply:
					if (minor == 2) {
						this.requestID = new RequestID(in.read_long());
						this.position = in.position();
						transport.signalResponse(requestID, this);

					} else if (minor == 1) {
						// here we can have a problem, because the requestID may
						// not be there
						in.mark(in.available());
						try {
							int count = in.read_long();
							for (int i = 0; i < count; i++) {
								int len = in.read_long();
								in.skip(len);
							}
							this.requestID = new RequestID(in.read_long());
						} finally {
							in.reset();
						}
						transport.signalResponse(requestID, this);

					} else if (minor == 0) {

					}
					return;

				case MsgType_1_1._Request:

					this.requestID = new RequestID(in.read_long());
					transport.signalResponse(requestID, this);
					return;

				}

			} catch (IOException e) {
				sendErrorAndClose();
			}

		}

		private void sendErrorAndClose() {
			// TODO Auto-generated method stub

		}

		public void getNextFragment(GIOPInputStream channel) {

			if (!hasMoreFragments) {
				// big problem //
				// TODO: handle
			}

			MessageHeader handler = (MessageHeader) transport
					.waitForResponse(requestID);

			channel.position(handler.message_start);
			channel.setMessageStart(handler.message_start);
			channel.limit(handler.size + 12);
			channel.controller(handler);
		}

		public GIOPVersion getGIOPVersion() {
			return GIOPVersion.get(major, minor);
		}

	}

	private Transport transport;

	private boolean isClient;

	private final ORB orb;

	public GIOPMessageTransport(ORB orb, Transport transport, boolean isClient)
			throws IOException {
		this.orb = orb;
		this.transport = transport;
		this.isClient = isClient;

		if (isClient) {
			next_request_id = 2;
		} else {
			next_request_id = 1;
		}

		transport.setInputHandler(this);
	}

	public GIOPMessageTransport(ORB orb, TransportManager tm,
			InetSocketAddress socketAddress, boolean isClient)
			throws IOException {

		this.orb = orb;
		this.isClient = isClient;

		if (isClient) {
			next_request_id = 2;
		} else {
			next_request_id = 1;
		}

		transport = tm.createTransport(socketAddress, this);
	}

	public void inputAvailable(Transport transport) {

		// there is a new message available //
		new MessageHeader().process(transport);

	}

	/**
	 * this will write
	 */
	public GIOPOutputStream startRequest(GIOPVersion version,
			InternalTargetAddress targetAddress, ClientInvocation inv,
			byte[] principal) throws IOException {
		InternalServiceContextList contextList = inv
				.getRequestServiceContextList(true);
		String operation = inv.getOperation();

		RequestID requestID = getNextRequestID();

		inv.setRequestID(requestID);

		// acquire output channel token
		OutputChannel outputChannel = transport.getOutputChannel();
		GIOPOutputStream out = new GIOPOutputStream(orb, outputChannel, version);

		// this will write a GIOP message header
		out.beginGIOPStream(MsgType_1_1._Request, requestID);

		// add stuff like character encoding, and
		// sending context rumtine service contexts...
		add_outgoing_system_contexts(contextList);

		// now write the request

		switch (version.minor) {
		case 0:
		case 1:
		// Write RequestHeader_1_1
		{
			contextList.write(out);
			out.write_long(requestID.value());
			out.write_boolean(inv.isResponseExpected());
			out.skip(3);
			targetAddress.writeObjectKey(out);
			if (principal == null) {
				out.write_long(0);
			} else {
				out.write_long(principal.length);
				out.write_octet_array(principal, 0, principal.length);
			}
		}

		case 2:
		// Write RequestHeader_1_2
		{
			out.write_long(requestID.value());
			switch(inv.getSyncScope()) {
			case Invocation.SYNC_NONE:
			case Invocation.SYNC_WITH_TRANSPORT:
				out.write_octet((byte)0);
				break;
			case Invocation.SYNC_WITH_SERVER:
				out.write_octet((byte)1);
				break;
			case Invocation.SYNC_WITH_TARGET:
				out.write_octet((byte)3);
			}

			out.skip(3); // can be dropped, target address aligns anyway
			targetAddress.write(out);
			out.write_string(operation);
			contextList.write(out);
			
			out.setInsertHeaderPadding(true);
			
			break;
		}
		}

		return out;
	}

	// add stuff like character encoding, and
	// sending context rumtine service contexts...
	private void add_outgoing_system_contexts(
			InternalServiceContextList contextList) {
		// TODO Auto-generated method stub

	}

	private RequestID getNextRequestID() {
		int id;
		synchronized (this) {
			id = next_request_id;
			next_request_id += 1;
		}
		RequestID result = new RequestID(id);
		return result;
	}

	public GIOPInputStream waitForResponse(ClientInvocation inv) {

		MessageHeader header = (MessageHeader) transport.waitForResponse(inv
				.getRequestIDObject());

		GIOPInputStream in = new GIOPInputStream(orb, header.getGIOPVersion(), header, transport
				.getInputChannel());

		in.limit(header.size + 12);
		in.position(header.position);
		in.setMessageStart(header.message_start);

		// now read rest of response header //

		switch (header.minor) {
		case 2:

			// read reply (for GIOP 1.2 we have already read the request id)
			int request_id = inv.getRequestID();

			ReplyStatusType_1_2 reply_status = ReplyStatusType_1_2Helper
					.read(in);
			inv.setReplyStatus(reply_status);

			InternalServiceContextList scl = new InternalServiceContextList();
			scl.read(in);

			inv.setResponseServiceContextList(scl);

			break;
		default:
			throw new NO_IMPLEMENT();
		}

		ApplicationException aex;
		SystemException sex;
		switch (inv.getReplyStatus().value()) {
		case ReplyStatusType_1_2._NO_EXCEPTION:
			return in;
			
		case ReplyStatusType_1_2._USER_EXCEPTION:
			String id = in.read_string();
			aex = new org.omg.CORBA.portable.ApplicationException(id, new UserExceptionInputStream (in, id));
			inv.setUserException(aex);
			return null;
			
		case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
			sex = GIOPHelper.unmarshalSystemException(inv.getResponseServiceContextList(false), in);
			inv.setSystemException(sex);
			return null;

		default:
			// todo: make sure we handle all cases here
			throw new NO_IMPLEMENT();
		}

	}

	public void registerResponse(RequestID requestID) {
		// TODO: HANDLE RACE
		transport.registerResponse((Object) requestID);
	}

}
