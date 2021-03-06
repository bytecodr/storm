/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.grouping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.storm.generated.GlobalStreamId;
import org.apache.storm.task.WorkerTopologyContext;


public class ShuffleGrouping implements CustomStreamGrouping, Serializable {
    private ArrayList<List<Integer>> choices;
    private AtomicInteger current;

    @Override
    public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
        choices = new ArrayList<List<Integer>>(targetTasks.size());
        for (Integer i : targetTasks) {
            choices.add(Arrays.asList(i));
        }
        current = new AtomicInteger(0);
        Collections.shuffle(choices, new Random());
    }

    @Override
    public List<Integer> chooseTasks(int taskId, List<Object> values) {
        int rightNow;
        int size = choices.size();
        while (true) {
            rightNow = current.incrementAndGet();
            if (rightNow < size) {
                return choices.get(rightNow);
            } else if (rightNow == size) {
                current.set(0);
                return choices.get(0);
            }
        } // race condition with another thread, and we lost. try again
    }
}
