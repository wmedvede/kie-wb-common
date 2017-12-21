/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.datamodeller.util;

import java.util.HashMap;
import java.util.Map;

public class TimeProfiler {

    private static final Map<String, TimeProfiler> profilers = new HashMap<>();

    String name;

    long startTime;

    long endTime = -1;

    private TimeProfiler(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TimeProfiler start() {
        startTime = System.currentTimeMillis();
        return this;
    }

    public TimeProfiler stop() {
        endTime = System.currentTimeMillis();
        return this;
    }

    public long elapsedTime() {
        long currentTime = endTime;
        if (currentTime < 0) {
            currentTime = System.currentTimeMillis();
        }
        return currentTime - startTime;
    }

    public void print() {
        System.out.println("TimeProfiler " + name + " total time: " + elapsedTime());
    }

    public static TimeProfiler addTimeProfiler(String name) {
        TimeProfiler profiler = new TimeProfiler(name);
        profilers.put(profiler.getName(),
                      profiler);
        return profiler;
    }

    public static TimeProfiler addTimeProfiler(Class clazz,
                                               String name) {
        return addTimeProfiler(clazz.getSimpleName() + "-" + name);
    }

    public static TimeProfiler getTimeProfiler(Class clazz,
                                               String name) {
        return getTimeProfiler(clazz.getSimpleName() + "-" + name);
    }

    public static TimeProfiler getTimeProfiler(String name) {
        return profilers.get(name);
    }
}
