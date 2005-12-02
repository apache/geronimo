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
package org.apache.geronimo.corba.channel.nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.Mutex;
import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.SyncList;
import EDU.oswego.cs.dl.util.concurrent.SyncMap;

public class ParticipationExecutor implements Executor {

	static void assertTrue(boolean x) {
		if (x == false) {
			throw new Error("assertion failed");
		}
	}

	private SyncMap map = new SyncMap(new HashMap(), new Mutex(),
			new ReentrantLock());

	private SyncList available = new SyncList(new ArrayList(), new Mutex(),
			new Mutex());

	private final Executor backing;

	public ParticipationExecutor(Executor backing) {
		this.backing = backing;
	}

	class Participation {

		Semaphore sem = new Semaphore(0);

		Thread participant = null;

		private final Object key;

		Participation(Object key) {
			this.key = key;
		}

		private boolean isReleased;

		private Runnable task;

		private Object value;

		public Object run() throws InterruptedException {
			
			
			
			try {
				if (isReleased) {
					return value;
				}
				
				participant = Thread.currentThread();				
				available.add(this);

				while (true) {
					sem.acquire();

					assertTrue(!available.contains(this));

					if (task != null) {
						task.run();
						task = null;
						available.add(this);
					}

					if (isReleased) {
						break;
					}
				}

			} finally {
				participant = null;
				map.remove(key);
			}

			return value;
		}

		void executeTask(Runnable task) {

			assertTrue(!available.contains(this));

			this.task = task;
			sem.release();
		}

		void releaseWithValue(Object value) {
			this.value = value;
			this.isReleased = true;

			if (available.remove(this)) {
				// ok.  the participant is not active, and so it
				// will wake up immediately				
			} else {
				// possible race?  the participant is currently activ
			}

			sem.release();
		}
	}

	public void execute(Runnable task) throws InterruptedException {

		Participation p = null;
		/*
		available.writerSync().acquire();
		try {
			if (!available.isEmpty()) {
				p = (Participation) available.remove(available.size() - 1);
			}
		} finally {
			available.writerSync().release();
		}
*/
		
		if (p == null) {
			backing.execute(task);
		} else {
			p.executeTask(task);
		}

	}

	public Participation create(Object key) {
		Participation result = new Participation(key);
		map.put(key, result);
		return result;
	}

	/**
	 * @throws InterruptedException
	 * @deprecated
	 */
	public Object participate(Object key) throws InterruptedException {

		Participation p = (Participation) map.get(key);
		if (p == null) {
			p = create(key);
		}

		return p.run();
	}

	public void release(Object key, Object value) {
		Participation p = (Participation) map.get(key);

		if (p != null) {
			p.releaseWithValue(value);
		} else {
			System.out.println("NO PARTICIPANT WAITING FOR " + key + " in "
					+ this);
		}
	}
}
