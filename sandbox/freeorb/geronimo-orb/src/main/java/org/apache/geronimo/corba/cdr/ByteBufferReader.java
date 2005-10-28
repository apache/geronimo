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
package org.apache.geronimo.corba.cdr;

import java.nio.ByteBuffer;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;


public class ByteBufferReader {

    Semaphore full;

    Semaphore empty;

    int readpos;

    int writepos;

    ByteBuffer storage;

    ByteBufferReader(ByteBuffer buf) {
        storage = buf;
        full = new Semaphore(buf.remaining());
        empty = new Semaphore(0);
    }

    void readSome() {
        int howmuch = (int) full.permits();
        int howmany = acquire(full, Math.min(howmuch, 1));

        if (readpos < writepos) {

            int first_write = Math.min(howmany, storage.limit() - writepos);

            storage.position(writepos);
            storage.limit(writepos + first_write);

            ByteBuffer buf1 = storage.slice();

            storage.position(0);

            int howmuchread;
            if (first_write == howmany) {
                storage.limit(storage.capacity());
                howmuchread = single_read(buf1);
            } else {
                int second_write = howmany - first_write;
                storage.limit(second_write);
                ByteBuffer buf2 = storage.slice();
                howmuchread = vector_read(buf1, buf2);
            }

            if (howmuchread < howmany) {
                full.release(howmany - howmuchread);
            }
            empty.release(howmuchread);

            writepos = (writepos + howmuchread) % storage.capacity();

        }
    }

    private int acquire(Semaphore sem, int howmany) {
        for (int l = 0; l < howmany; l++) {
            try {
                sem.acquire();
            }
            catch (InterruptedException ex) {
                return l;
            }
        }

        return howmany;
    }

}
