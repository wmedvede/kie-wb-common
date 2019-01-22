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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.activities;

import org.eclipse.bpmn2.CallActivity;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.CallActivityPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.CalledElement;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.Independent;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.IsAsync;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.IsMultipleInstance;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.MultipleInstanceCollectionInput;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.MultipleInstanceCollectionOutput;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.MultipleInstanceCompletionCondition;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.MultipleInstanceDataInput;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.MultipleInstanceDataOutput;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ReusableSubprocessTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.WaitForCompletion;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class CallActivityConverter extends BaseCallActivityConverter<ReusableSubprocess> {

    private CallActivityPropertyReader p;

    public CallActivityConverter(TypedFactoryManager factoryManager,
                                 PropertyReaderFactory propertyReaderFactory) {
        super(factoryManager, propertyReaderFactory);
    }

    @Override
    protected Node<View<ReusableSubprocess>, Edge> createNode(CallActivity activity, CallActivityPropertyReader p) {
        this.p = p;
        return factoryManager.newNode(activity.getId(), ReusableSubprocess.class);
    }

    @Override
    public BpmnNode convert(CallActivity activity) {
        BpmnNode result = super.convert(activity);
        ReusableSubprocess subprocess = (ReusableSubprocess) result.value().getContent().getDefinition();
        ReusableSubprocessTaskExecutionSet executionSet = subprocess.getExecutionSet();

        executionSet.getMultipleInstanceCollectionInput().setValue(p.getCollectionInput());
        executionSet.getMultipleInstanceDataInput().setValue(p.getDataInput());
        executionSet.getMultipleInstanceCollectionOutput().setValue(p.getCollectionOutput());
        executionSet.getMultipleInstanceDataOutput().setValue(p.getDataOutput());
        executionSet.getMultipleInstanceCompletionCondition().setValue(p.getCompletionCondition());

        boolean multipleInstance = !isEmpty(executionSet.getMultipleInstanceCollectionInput().getValue()) ||
                !isEmpty(executionSet.getMultipleInstanceDataInput().getValue()) ||
                !isEmpty(executionSet.getMultipleInstanceCollectionOutput().getValue()) ||
                !isEmpty(executionSet.getMultipleInstanceDataOutput().getValue()) ||
                !isEmpty(executionSet.getMultipleInstanceCompletionCondition().getValue());
        executionSet.setIsMultipleInstance(new IsMultipleInstance(multipleInstance));
        return result;
    }

    @Override
    protected ReusableSubprocessTaskExecutionSet createReusableSubprocessTaskExecutionSet(CalledElement calledElement,
                                                                                          Independent independent,
                                                                                          WaitForCompletion waitForCompletion,
                                                                                          IsAsync isAsync,
                                                                                          OnEntryAction onEntryAction,
                                                                                          OnExitAction onExitAction,
                                                                                          CallActivityPropertyReader p) {
        return new ReusableSubprocessTaskExecutionSet(calledElement, independent, waitForCompletion, isAsync,
                                                      new IsMultipleInstance(),
                                                      new MultipleInstanceCollectionInput(),
                                                      new MultipleInstanceDataInput(),
                                                      new MultipleInstanceCollectionOutput(),
                                                      new MultipleInstanceDataOutput(),
                                                      new MultipleInstanceCompletionCondition(),
                                                      onEntryAction, onExitAction);
    }
}
