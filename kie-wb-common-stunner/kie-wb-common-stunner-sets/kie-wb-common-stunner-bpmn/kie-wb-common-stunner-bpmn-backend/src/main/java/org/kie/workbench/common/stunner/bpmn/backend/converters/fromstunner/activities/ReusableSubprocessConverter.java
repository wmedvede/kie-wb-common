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
package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.activities;

import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.CallActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ReusableSubprocessTaskExecutionSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ReusableSubprocessConverter extends BaseReusableSubprocessConverter<ReusableSubprocess> {

    public ReusableSubprocessConverter(PropertyWriterFactory propertyWriterFactory) {
        super(propertyWriterFactory);
    }

    @Override
    public PropertyWriter toFlowElement(Node<View<ReusableSubprocess>, ?> n) {
        CallActivityPropertyWriter p = (CallActivityPropertyWriter) super.toFlowElement(n);
        ReusableSubprocess reusableSubprocess = n.getContent().getDefinition();
        ReusableSubprocessTaskExecutionSet executionSet = reusableSubprocess.getExecutionSet();
        if (Boolean.TRUE.equals(executionSet.getIsMultipleInstance().getValue())) {
            p.setCollectionInput(executionSet.getMultipleInstanceCollectionInput().getValue());
            p.setInput(executionSet.getMultipleInstanceDataInput().getValue());
            p.setCollectionOutput(executionSet.getMultipleInstanceCollectionOutput().getValue());
            p.setOutput(executionSet.getMultipleInstanceDataOutput().getValue());
            p.setCompletionCondition(executionSet.getMultipleInstanceCompletionCondition().getValue());
        }
        return p;
    }
}
