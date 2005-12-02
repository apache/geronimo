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

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.ObjectImpl;
import org.omg.IOP.IOR;
import org.omg.IOP.IORHelper;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ClientDelegate;

public class InternalIOR {

	static final boolean DIRECT = true;

	IOR ior;

	Profile[] profiles;

	public final AbstractORB orb;

	private String type;

	public InternalIOR(AbstractORB orb, IOR ior) {
		this.orb = orb;
		this.ior = ior;
		this.type = ior.type_id;
	}

	public InternalIOR(AbstractORB orb, String type, Profile[] profiles)
	{
		this.orb = orb;
		this.type = type;
		this.profiles = profiles;
	}
	
	public static InternalIOR read(AbstractORB orb, InputStream in) {
		return new InternalIOR(orb, IORHelper.read(in));
	}

	public int getProfileCount() {
		return ior.profiles.length;
	}

	public Profile getProfile(int idx) {

		if (profiles == null) {
			profiles = new Profile[getProfileCount()];
		}

		if (profiles[idx] == null) {
			profiles[idx] = Profile.read(orb, ior.profiles[idx].tag,
					ior.profiles[idx].profile_data);
		}

		return profiles[idx];
	}

	public static InternalIOR extract(Object forward) {
		if (forward instanceof ObjectImpl) {
			Delegate del = ((ObjectImpl) forward)._get_delegate();
			if (del instanceof ClientDelegate) {
				return ((ClientDelegate) del).getInternalIOR();
			}
		}

		// todo: better exception here?
		throw new NO_IMPLEMENT();
	}

	public int profileTag(int i) {
		if (profiles != null) {
			return profiles[i].tag();
		}
		return ior.profiles[i].tag;
	}

	public String getType() {
		if (type != null) { return type; }
		return ior.type_id;
	}

	public void write(OutputStream out) {
		if (ior == null) {
			throw new NO_IMPLEMENT();
		}
		// TODO: reconstruct IOR if changed
		IORHelper.write(out, ior);
	}

	public boolean equals(Object other) {
		if (super.equals(other)) {
			return true;
		}
		
		if (other instanceof InternalIOR) {
			InternalIOR oior = (InternalIOR) other;

			if (!getType().equals(oior.getType())) return false;
			if (getProfileCount() != oior.getProfileCount()) return false;
			
			for (int i = 0; i < getProfileCount(); i++) {
				Profile p1 = getProfile(i);
				Profile p2 = oior.getProfile(i);
				
				if (!p1.equals(p2)) { return false; }
			}
			
			return true;
		}

		return true;
	}
	
}
