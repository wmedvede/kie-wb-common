/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties;

import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.MultiInstanceLoopCharacteristics;
import org.eclipse.bpmn2.SubProcess;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;

public class MultipleInstanceSubProcessPropertyWriterTest {

    @Test
    public void nullInputOutputsShouldNotThrow() {
        SubProcess subProcess = bpmn2.createSubProcess();
        MultipleInstanceSubProcessPropertyWriter p =
                new MultipleInstanceSubProcessPropertyWriter(
                        subProcess, new FlatVariableScope());

        assertThatCode(() -> {
            p.setInput(null);
            p.setOutput(null);
            p.createDataInput(null, null);
            p.createDataOutput(null, null);
            p.setCompletionCondition(null);
        }).doesNotThrowAnyException();
    }

    @Test
    public void completionConditionMustBeWrappedInCdata() {
        String expression = "x<1";
        String expected = "<![CDATA[" + expression + "]]>";
        SubProcess subProcess = bpmn2.createSubProcess();
        MultipleInstanceSubProcessPropertyWriter p =
                new MultipleInstanceSubProcessPropertyWriter(
                        subProcess, new FlatVariableScope());
        p.setCompletionCondition(expression);
        MultiInstanceLoopCharacteristics loopCharacteristics =
                (MultiInstanceLoopCharacteristics) subProcess.getLoopCharacteristics();
        FormalExpression completionCondition =
                (FormalExpression) loopCharacteristics.getCompletionCondition();

        assertThat(expected).isEqualTo(completionCondition.getBody());
    }
}