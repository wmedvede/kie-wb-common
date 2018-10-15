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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.ThrowEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.PostConverterProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public abstract class AbstractCompensationEventPostConverter implements PostConverterProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCompensationEventPostConverter.class);

    protected void linkActivityRef(Process process,
                                   ThrowEvent throwEvent,
                                   String activityRef) {
        if (!isEmpty(activityRef)) {
            final CompensateEventDefinition compensateEvent = (CompensateEventDefinition) throwEvent.getEventDefinitions().get(0);
            final Activity activity = findActivity(process,
                                                   activityRef);
            if (activity != null) {
                compensateEvent.setActivityRef(activity);
            } else {
                LOG.warn("Referred activity: " + activityRef + " was not found for event: id: " + throwEvent.getId() + ", name: " + throwEvent.getName());
            }
        }
    }

    protected Activity findActivity(FlowElementsContainer container,
                                    String uuid) {
        final List<FlowElementsContainer> subContainers = new ArrayList<>();
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof Activity) {
                if (flowElement.getId().equals(uuid)) {
                    return (Activity) flowElement;
                } else if (flowElement instanceof SubProcess) {
                    subContainers.add((SubProcess) flowElement);
                }
            }
        }
        Activity result;
        for (FlowElementsContainer subContainer : subContainers) {
            result = findActivity(subContainer,
                                  uuid);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
