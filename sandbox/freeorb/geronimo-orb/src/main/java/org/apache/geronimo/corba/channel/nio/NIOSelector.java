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

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class NIOSelector implements Runnable {

    final Selector sel;

    Thread selectorThread = null;

    List regs = new ArrayList();

    private AsyncNIOTransportManager manager;

    public NIOSelector(AsyncNIOTransportManager manager, Selector selector) {
        this.sel = selector;
        this.manager = manager;
    }

    public void wakeup() {
        if (Thread.currentThread() == selectorThread) {
            // do nothing //
        } else {
            if (sel != null) {
                sel.wakeup();
            }
        }
    }

    public void removeInterest(SocketChannel ch, int interest, String why) {
        Selector sel = this.sel;
        if (sel == null) {
            return;
        }
        if (Thread.currentThread() == selectorThread && regs.isEmpty()) {
            SelectionKey sk = ch.keyFor(sel);
            sk.interestOps(sk.interestOps() & ~interest);

            System.out.println("FAST REMOVE " + interest(interest) + " : "
                               + why);

            return;
        }

        RemoveInterest reg = new RemoveInterest(ch, interest);

        synchronized (regs) {
            regs.add(reg);
        }

        sel.wakeup();
    }

    public void addInterest(SocketChannel ch, int interest, String why) {

        if (Thread.currentThread() == selectorThread && regs.isEmpty()) {
            SelectionKey sk = ch.keyFor(sel);
            sk.interestOps(sk.interestOps() | interest);

            System.out.println("FAST ADD " + interest(interest) + " : " + why);

            return;
        }

        AddInterest reg = new AddInterest(ch, interest, why);

        addCommand(reg);
    }

    private void addCommand(Command reg) {
        synchronized (regs) {
            regs.add(reg);
        }

        sel.wakeup();
    }

    public void register(SocketChannel ch, SelectionListener listener)
            throws ClosedChannelException
    {

        Registration reg = new Registration(ch, listener);

        addCommand(reg);
    }

    private String interest(int interest) {
        if (interest == 0) {
            return "NONE";
        }
        StringBuffer sb = new StringBuffer();
        if ((interest & SelectionKey.OP_ACCEPT) != 0) {
            sb.append("OP_ACCEPT ");
        }
        if ((interest & SelectionKey.OP_CONNECT) != 0) {
            sb.append("OP_CONNECT ");
        }
        if ((interest & SelectionKey.OP_READ) != 0) {
            sb.append("OP_READ ");
        }
        if ((interest & SelectionKey.OP_WRITE) != 0) {
            sb.append("OP_WRITE ");
        }
        return sb.toString();
    }

    abstract static class Command {

        abstract void exec();
    }

    class AddInterest extends Command {

        SelectableChannel chan;

        int interest;

        private final String why;

        AddInterest(SelectableChannel chan, int interest, String why) {
            this.chan = chan;
            this.interest = interest;
            this.why = why;
        }

        void exec() {

            System.out.println("add interest : " + interest(interest) + " : "
                               + why);

            SelectionKey key = chan.keyFor(sel);

            if (key != null) {
                key.interestOps(interest | key.interestOps());

                System.out.println("interest is: "
                                   + interest(key.interestOps()));
            }
        }

    }

    class RemoveInterest extends Command {

        SelectableChannel chan;

        int interest;

        RemoveInterest(SelectableChannel chan, int interest) {
            this.chan = chan;
            this.interest = interest;
        }

        void exec() {
            System.out.println("remove interest : " + interest(interest));
            SelectionKey key = chan.keyFor(sel);
            if (key != null) {
                key.interestOps(key.interestOps() & ~interest);

                System.out.println("interest is: "
                                   + interest(key.interestOps()));
            }
        }

    }

    class Registration extends Command {

        private final SocketChannel ch;

        private final SelectionListener listener;

        public Registration(SocketChannel ch, SelectionListener result) {
            this.ch = ch;
            this.listener = result;
        }

        void exec() {
            try {
                SelectionKey key = ch.keyFor(sel);
                if (key == null) {
                    if (ch.isRegistered()) {
                        // TODO: problem //
                    } else {
                        key = ch.register(sel, 0, listener);
                    }
                } else {
                    if (listener != key.attachment()) {
                        System.out.println("wrong attachment");
                    }
                }

            }
            catch (ClosedChannelException e) {
                listener.channelClosed(e);
            }
        }
    }

    public void run() {

        try {
            run0();
            System.out.println("DONE.");
        }
        catch (Error e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private Command[] reg = new Command[0];

    public void run0() {

        this.selectorThread = Thread.currentThread();

        SelectionKey[] sks = new SelectionKey[0];

        if (doCommands()) {
            if (!sel.isOpen()) {
                return;
            }
        }

        while (true) {

            Selector sel = this.sel;
            if (sel == null) {
                return;
            }

            try {
                // print(sel);
                sel.select(100);
            }
            catch (ClosedSelectorException ex) {
                return;
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (sel == null) {
                return;
            }

            if (doCommands()) {

                if (!sel.isOpen()) {
                    return;
                }

                Set selectedKeys = sel.selectedKeys();
                if (selectedKeys != null) {
                    selectedKeys.clear();
                }
                continue;
            }

            Set selectedKeys = sel.selectedKeys();
            int count = selectedKeys.size();
            if (count != 0) {
                sks = (SelectionKey[]) selectedKeys.toArray(sks);
                selectedKeys.clear();

                for (int i = 0; i < count; i++) {
                    SelectionListener listener = (SelectionListener) sks[i]
                            .attachment();

                    System.out.println("doing " + interest(sks[i].readyOps())
                                       + " on " + sks[i].channel());

                    SelectableChannel ch = sks[i].channel();
                    if (!ch.isOpen()) {
                        listener.channelClosed(null);
                        sks[i].cancel();

                    } else {

                        try {

                            if (sks[i].isAcceptable()) {
                                listener.canAccept();
                            }

                            if (sks[i].isConnectable()) {
                                listener.canConnect();
                            }

                            if (sks[i].isReadable()) {
                                listener.canRead();
                            }

                            if (sks[i].isWritable()) {
                                listener.canWrite();
                            }

                        }
                        catch (CancelledKeyException e) {
                            // ignore //
                        }
                    }

                }

            }

        }

    }

    private void print(Selector sel) {

        if (sel == null) {
            return;
        }
        Set s = sel.keys();
        SelectionKey[] sk = (SelectionKey[]) s.toArray(new SelectionKey[s
                .size()]);
        for (int i = 0; i < sk.length; i++) {
            System.out.println(sk[i].channel() + " "
                               + interest(sk[i].interestOps()));
        }
        System.out.println("===");
    }

    private boolean doCommands() {
        int size = 0;
        synchronized (regs) {
            size = regs.size();

            if (size == 0) {
                return false;
            }

            reg = (Command[]) regs.toArray(reg);
            regs.clear();

        }

        for (int i = 0; i < size; i++) {
            Command r = reg[i];
            r.exec();
        }

        return true;
    }

    public boolean isRunning() {
        if (selectorThread == null) {
            return false;
        } else {
            return sel.isOpen();
        }
    }

    public void shutdown() throws IOException {
        addCommand(new Command() {

            void exec() {
                try {
                    sel.close();
                }
                catch (IOException e) {
                }
            }

        });
    }

}
