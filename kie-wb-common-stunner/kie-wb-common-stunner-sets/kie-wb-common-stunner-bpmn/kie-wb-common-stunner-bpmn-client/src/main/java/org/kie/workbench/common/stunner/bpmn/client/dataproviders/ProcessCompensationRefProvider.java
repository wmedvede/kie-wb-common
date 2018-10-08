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
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
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
        final Map<String, Node> candidates = new HashMap<>();
        final Diagram diagram = sessionManager.getCurrentSession().getCanvasHandler().getDiagram();
        final String rootUUID = diagram.getMetadata().getCanvasRootUUID();
        final Graph graph = diagram.getGraph();
        final Iterable<Node> it = graph.nodes();

        it.forEach(node -> {
            if (((View) node.getContent()).getDefinition() instanceof EndCompensationEvent) {
                ActivityRef activityRef = ((EndCompensationEvent) ((View) node.getContent()).getDefinition()).getExecutionSet().getActivityRef();
                if (isValid(activityRef)) {
                    Node targetNode = graph.getNode(activityRef.getValue());
                    candidates.put(activityRef.getValue(),
                                   targetNode);
                }
            } else if (((View) node.getContent()).getDefinition() instanceof IntermediateCompensationEventThrowing) {
                ActivityRef activityRef = ((IntermediateCompensationEventThrowing) ((View) node.getContent()).getDefinition()).getExecutionSet().getActivityRef();
                if (isValid(activityRef)) {
                    Node targetNode = graph.getNode(activityRef.getValue());
                    candidates.put(activityRef.getValue(),
                                   targetNode);
                }
            } else if (isDescendantFrom(rootUUID,
                                        node)) {
                Node nodeTarget = null;
                if ((((View) node.getContent()).getDefinition() instanceof IntermediateCompensationEvent) && GraphUtils.isDockedNode(node)) {
                    nodeTarget = (Node) GraphUtils.getDockParent(node).orElse(null);
                } else if (((View) node.getContent()).getDefinition() instanceof EventSubprocess) {
                    nodeTarget = node;
                }
                if (nodeTarget != null) {
                    candidates.put(nodeTarget.getUUID(),
                                   nodeTarget);
                }
            }
        });

        return node -> candidates.containsKey(node.getUUID());
    }

    private boolean isDescendantFrom(final String parent,
                                     final Node<?, ? extends Edge> node) {
        return node.getInEdges().stream()
                .filter(edge -> edge.getSourceNode().getUUID().equals(parent))
                .findFirst()
                .isPresent();
    }

    private boolean isValid(ActivityRef activityRef) {
        return activityRef != null && !isEmpty(activityRef.getValue());
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