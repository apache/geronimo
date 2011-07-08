/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.jsr77;

import java.io.Serializable;
import java.text.NumberFormat;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

/**
 * @version $Rev$ $Date$
 */
@DataTransferObject
public class DynamicServerInfo implements Serializable {
    private final static long BYTES_MAX = 2048;
    private final static long KB_MAX = BYTES_MAX * 1024l;
    private final static long MB_MAX = KB_MAX * 1024l;
    private final static long GB_MAX = MB_MAX * 1024l;
    private final static long TB_MAX = GB_MAX * 1024l;
    private final static double KB_DIV = 1024;
    private final static double MB_DIV = KB_DIV*1024d;
    private final static double GB_DIV = MB_DIV*1024d;
    private final static double TB_DIV = GB_DIV*1024d;
    private NumberFormat dec2Format;
    private String memoryCurrent;
    private String memoryMost;
    private String memoryAllocated;
    private String upTime;
    private long bytesCurrent, bytesMost, bytesAllocated;

    public DynamicServerInfo(long upTime) {
        this.upTime = calculateTime(upTime);
        memoryAllocated = memoryCurrent = memoryMost = "Unknown";
    }

    public DynamicServerInfo(long memoryCurrent, long memoryMost, long memoryAllocated, long upTime) {
        dec2Format = NumberFormat.getNumberInstance();
        dec2Format.setMaximumFractionDigits(2);
        bytesCurrent = memoryCurrent;
        bytesMost = memoryMost;
        bytesAllocated = memoryAllocated;
        this.memoryCurrent = calculateMemory(memoryCurrent);
        this.memoryMost = calculateMemory(memoryMost);
        this.memoryAllocated = calculateMemory(memoryAllocated);
        this.upTime = calculateTime(upTime);
    }

    private String calculateMemory(long bytes) {
        if(bytes < BYTES_MAX) {
            return bytes+" B";
        } else if(bytes < KB_MAX) {
            return dec2Format.format((double)bytes/KB_DIV)+" kB";
        } else if(bytes < MB_MAX) {
            return dec2Format.format((double)bytes/MB_DIV)+" MB";
        } else if(bytes < GB_MAX) {
            return dec2Format.format((double)bytes/GB_DIV)+" GB";
        } else if(bytes < TB_MAX) {
            return dec2Format.format((double)bytes/TB_DIV)+" TB";
        } else {
            return "Out of range";
        }
    }

    private String calculateTime(long millis) {
        int secs = (int)(millis/1000L);
        int days = secs/86400;
        secs = secs % 86400;
        int hours = secs/3600;
        secs = secs % 3600;
        int minutes = secs / 60;
        secs = secs % 60;
        StringBuilder buf = new StringBuilder();
        if(days > 1) {
            buf.append(' ').append(days).append(" days");
        } else if(days > 0) {
            buf.append(' ').append(days).append(" day");
        }
        if(hours > 1) {
            buf.append(' ').append(hours).append(" hours");
        } else if(hours > 0) {
            buf.append(' ').append(hours).append(" hour");
        }
        if(minutes > 1) {
            buf.append(' ').append(minutes).append(" minutes");
        } else if(minutes > 0) {
            buf.append(' ').append(minutes).append(" minute");
        }
        if(secs > 1) {
            buf.append(' ').append(secs).append(" seconds");
        } else if(secs > 0) {
            buf.append(' ').append(secs).append(" second");
        }
        buf.delete(0,1);
        return buf.toString();
    }

    @RemoteProperty
    public String getMemoryCurrent() {
        return memoryCurrent;
    }

    @RemoteProperty
    public String getMemoryMost() {
        return memoryMost;
    }

    @RemoteProperty
    public String getMemoryAllocated() {
        return memoryAllocated;
    }

    @RemoteProperty
    public String getUpTime() {
        return upTime;
    }

    @RemoteProperty
    public long getBytesCurrent() {
        return bytesCurrent;
    }

    @RemoteProperty
    public long getBytesMost() {
        return bytesMost;
    }

    @RemoteProperty
    public long getBytesAllocated() {
        return bytesAllocated;
    }
}
