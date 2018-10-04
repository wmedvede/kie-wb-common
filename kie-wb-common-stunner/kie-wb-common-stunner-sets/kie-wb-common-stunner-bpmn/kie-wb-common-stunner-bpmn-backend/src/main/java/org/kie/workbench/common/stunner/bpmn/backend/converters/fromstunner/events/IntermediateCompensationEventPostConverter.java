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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.events;

import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ThrowEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateCompensationEventThrowing;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class IntermediateCompensationEventPostConverter
        extends AbstractCompensationEventPostConverter {

    @Override
    public void postProcessNode(ProcessPropertyWriter processWriter,
                                BasePropertyWriter nodeWriter,
                                Node<View<? extends BPMNViewDefinition>, ?> node) {
        //TODO WM sacar estos prints
        System.out.println("IntermediateCompensationEventPostConverter Post process node: " + node);

        final ThrowEvent throwEvent = (ThrowEvent) nodeWriter.getElement();
        final CompensateEventDefinition compensateEvent = (CompensateEventDefinition) throwEvent.getEventDefinitions().get(0);
        final String activityRef = ((IntermediateCompensationEventThrowing) node.getContent().getDefinition()).getExecutionSet().getActivityRef().getValue();
        linkActivityRef(processWriter.getProcess(),
                        compensateEvent,
                        activityRef);
    }
}
