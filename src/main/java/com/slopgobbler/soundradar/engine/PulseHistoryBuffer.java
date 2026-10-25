package com.slopgobbler.soundradar.engine;

import com.slopgobbler.soundradar.model.AcousticPulse;
import com.slopgobbler.soundradar.model.AudioCategory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe telemetry buffer that maintains acoustic history, event logs, and frequency statistics.
 */
public class PulseHistoryBuffer {

    private final int capacity;
    private final Deque<AcousticPulse> buffer;
    private final Map<AudioCategory, AtomicLong> categoryCounters;
    private final AtomicLong totalIntercepted = new AtomicLong(0);

    public PulseHistoryBuffer(int capacity) {
        this.capacity = Math.max(16, capacity);
        this.buffer = new ArrayDeque<>(this.capacity);
        this.categoryCounters = new EnumMap<>(AudioCategory.class);
        for (AudioCategory cat : AudioCategory.values()) {
            this.categoryCounters.put(cat, new AtomicLong(0));
        }
    }

    public synchronized void record(AcousticPulse pulse) {
        if (pulse == null) return;
        totalIntercepted.incrementAndGet();
        AtomicLong counter = categoryCounters.get(pulse.getCategory());
        if (counter != null) {
            counter.incrementAndGet();
        }

        while (buffer.size() >= capacity) {
            buffer.pollFirst();
        }
        buffer.addLast(pulse);
    }

    public synchronized List<AcousticPulse> getRecentPulses() {
        return new ArrayList<>(buffer);
    }

    public long getTotalIntercepted() {
        return totalIntercepted.get();
    }

    public long getCountForCategory(AudioCategory category) {
        AtomicLong counter = categoryCounters.get(category);
        return counter != null ? counter.get() : 0L;
    }

    public synchronized void clear() {
        buffer.clear();
        totalIntercepted.set(0);
        for (AtomicLong counter : categoryCounters.values()) {
            counter.set(0);
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized int size() {
        return buffer.size();
    }
}
