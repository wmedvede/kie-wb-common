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

package org.kie.workbench.common.stunner.bpmn.client.dataproviders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.kie.workbench.common.stunner.bpmn.definition.BPMNDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.EndCompensationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateCompensationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateCompensationEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.compensation.ActivityRef;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;
import org.uberfire.commons.data.Pair;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class ProcessCompensationRefProvider extends AbstractProcessFilteredNodeProvider {

    @Inject
    public ProcessCompensationRefProvider(final SessionManager sessionManager) {
        super(sessionManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate<Node> getFilter() {
        final Map<String, Node> referredNodes = new HashMap<>();
        final Diagram diagram = sessionManager.getCurrentSession().getCanvasHandler().getDiagram();
        final Iterable<Node> it = diagram.getGraph().nodes();
        //collect the nodes that:
        // 1) a compensation event referring to.
        // 2) has a boundary compensation event on
        // 3) or is an event sub-processes
        it.forEach(node -> {
            ActivityRef activityRef = null;
            Node nodeTarget = null;
            if (((View) node.getContent()).getDefinition() instanceof EndCompensationEvent) {
                activityRef = ((EndCompensationEvent) ((View) node.getContent()).getDefinition()).getExecutionSet().getActivityRef();
            } else if (((View) node.getContent()).getDefinition() instanceof IntermediateCompensationEventThrowing) {
                activityRef = ((IntermediateCompensationEventThrowing) ((View) node.getContent()).getDefinition()).getExecutionSet().getActivityRef();
            } else if ((((View) node.getContent()).getDefinition() instanceof IntermediateCompensationEvent) && GraphUtils.isDockedNode(node)) {
                nodeTarget = (Node) GraphUtils.getDockParent(node).orElse(null);
            } else if (((View) node.getContent()).getDefinition() instanceof EventSubprocess) {
                nodeTarget = node;
            }

            if (activityRef != null && !isEmpty(activityRef.getValue())) {
                referredNodes.put(activityRef.getValue(),
                                  node);
            } else if (nodeTarget != null) {
                referredNodes.put(nodeTarget.getUUID(),
                                  node);
            }
        });

        return node -> referredNodes.containsKey(node.getUUID());
    }

    @Override
    public Function<Node, Pair<Object, String>> getMapper() {
        return node -> {
            if (((View) node.getContent()).getDefinition() instanceof BPMNDefinition) {
                return buildPair(node.getUUID(),
                                 (BPMNDefinition) ((View) node.getContent()).getDefinition());
            }
            return null;
        };
    }

    private Pair<Object, String> buildPair(String uuid,
                                           BPMNDefinition definition) {
        String name = definition.getGeneral().getName().getValue();
        return new Pair<>(uuid,
                          isEmpty(name) ? uuid : name);
    }
}