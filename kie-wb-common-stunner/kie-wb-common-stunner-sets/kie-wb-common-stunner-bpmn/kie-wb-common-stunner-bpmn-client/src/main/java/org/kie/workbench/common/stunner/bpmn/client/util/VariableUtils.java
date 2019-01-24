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

package org.kie.workbench.common.stunner.bpmn.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.kie.workbench.common.stunner.bpmn.definition.BPMNDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BusinessRuleTask;
import org.kie.workbench.common.stunner.bpmn.definition.EndErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndEscalationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateErrorEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateEscalationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateEscalationEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateMessageEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateMessageEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateSignalEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateSignalEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.MultipleInstanceSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.StartErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartEscalationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.workitem.ServiceTask;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.util.StringUtils;
import org.uberfire.commons.data.Pair;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class VariableUtils {

    private static final String PROPERTY_IN_PREFIX = "[din]";
    private static final String PROPERTY_OUT_PREFIX = "[dout]";
    private final static BiFunction<String, Pair<BPMNDefinition, Node<View<BPMNDefinition>, Edge>>, Collection<VariableUsage>> NO_USAGES = (s, pair) -> null;
    private final static Map<Class<?>, BiFunction<String, Pair<BPMNDefinition, Node<View<BPMNDefinition>, Edge>>, Collection<VariableUsage>>> findFunctions = buildFindFunctions();

    @SuppressWarnings("unchecked")
    public static Collection<VariableUsage> findVariableUsages(Graph graph, String variableName) {
        if (StringUtils.isEmpty(variableName)) {
            return Collections.EMPTY_LIST;
        }
        Iterable<Node> nodes = graph.nodes();
        return StreamSupport.stream(nodes.spliterator(), false)
                .filter(VariableUtils::isBPMNDefinition)
                .map(node -> (Node<View<BPMNDefinition>, Edge>) node)
                .map(node -> lookupFindFunction(node.getContent().getDefinition()).orElse(NO_USAGES).apply(variableName, Pair.newPair(node.getContent().getDefinition(), node)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static Collection<VariableUsage> findVariableUsages(String variableName, AssignmentsInfo assignmentsInfo, String displayName, Node<View<BPMNDefinition>, Edge> node) {
        Collection<VariableUsage> result = new ArrayList<>();
        if (assignmentsInfo != null) {
            Map<String, VariableUsage> decodedVariableUsages = decodeVariableUsages(assignmentsInfo.getValue(), node, displayName);
            if (decodedVariableUsages.containsKey(variableName)) {
                result.add(decodedVariableUsages.get(variableName));
            }
        }
        return result;
    }

    private static Collection<VariableUsage> findVariableUsages(String variableName, MultipleInstanceSubprocess subprocess, Node<View<BPMNDefinition>, Edge> node) {
        final Collection<VariableUsage> result = new ArrayList<>();
        addVariableUsages(result, variableName, subprocess.getExecutionSet().getMultipleInstanceCollectionInput().getValue(),
                          subprocess.getExecutionSet().getMultipleInstanceCollectionOutput().getValue(), getDisplayName(subprocess), node);
        return result;
    }

    private static Collection<VariableUsage> findVariableUsages(String variableName, UserTask userTask, Node<View<BPMNDefinition>, Edge> node) {
        final String displayName = getDisplayName(userTask);
        final Collection<VariableUsage> result = findVariableUsages(variableName, userTask.getExecutionSet().getAssignmentsinfo(), displayName, node);
        addVariableUsages(result, variableName, userTask.getExecutionSet().getMultipleInstanceCollectionInput().getValue(),
                          userTask.getExecutionSet().getMultipleInstanceCollectionOutput().getValue(), displayName, node);
        return result;
    }

    private static Collection<VariableUsage> findVariableUsages(String variableName, ReusableSubprocess subprocess, Node<View<BPMNDefinition>, Edge> node) {
        final String displayName = getDisplayName(subprocess);
        final Collection<VariableUsage> result = findVariableUsages(variableName, subprocess.getDataIOSet().getAssignmentsinfo(), displayName, node);
        addVariableUsages(result, variableName, subprocess.getExecutionSet().getMultipleInstanceCollectionInput().getValue(),
                          subprocess.getExecutionSet().getMultipleInstanceCollectionOutput().getValue(), displayName, node);
        return result;
    }

    private static void addVariableUsages(Collection<VariableUsage> variableUsages, String variableName,
                                          String inputCollection, String outputCollection,
                                          String displayName, Node<View<BPMNDefinition>, Edge> node) {
        if (variableName.equals(inputCollection)) {
            variableUsages.add(new VariableUsage(variableName, VariableUsage.USAGE_TYPE.INPUT_COLLECTION, node, displayName));
        }
        if (variableName.equals(outputCollection)) {
            variableUsages.add(new VariableUsage(variableName, VariableUsage.USAGE_TYPE.OUTPUT_COLLECTION, node, displayName));
        }
    }

    private static boolean isBPMNDefinition(Node node) {
        return node.getContent() instanceof View &&
                ((View) node.getContent()).getDefinition() instanceof BPMNDefinition;
    }

    private static String getDisplayName(BPMNDefinition definition) {
        return definition.getGeneral() != null && definition.getGeneral().getName() != null ? definition.getGeneral().getName().getValue() : null;
    }

    private static Map<String, VariableUsage> decodeVariableUsages(String encodedAssignments, Node node, String displayName) {
        Map<String, VariableUsage> variableUsages = new HashMap<>();
        if (isEmpty(encodedAssignments)) {
            return variableUsages;
        }
        String[] encodedParts = encodedAssignments.split("\\|");
        if (encodedParts.length != 5) {
            return variableUsages;
        }
        String encodedVariablesList = encodedParts[4];
        if (!isEmpty(encodedVariablesList)) {
            String[] variablesList = encodedVariablesList.split(",");
            Arrays.stream(variablesList)
                    .filter(variableDef -> !isEmpty(variableDef))
                    .forEach(variableDef -> {
                        String variableName = null;
                        VariableUsage.USAGE_TYPE usageType = null;
                        String unPrefixedVariableDef;
                        String[] variableDefParts;
                        if (variableDef.startsWith(PROPERTY_IN_PREFIX)) {
                            unPrefixedVariableDef = variableDef.substring(PROPERTY_IN_PREFIX.length());
                            if (!isEmpty(unPrefixedVariableDef)) {
                                variableDefParts = unPrefixedVariableDef.split("->");
                                variableName = variableDefParts[0];
                                usageType = VariableUsage.USAGE_TYPE.INPUT_VARIABLE;
                            }
                        } else if (variableDef.startsWith(PROPERTY_OUT_PREFIX)) {
                            unPrefixedVariableDef = variableDef.substring(PROPERTY_OUT_PREFIX.length());
                            if (!isEmpty(unPrefixedVariableDef)) {
                                variableDefParts = unPrefixedVariableDef.split("->");
                                variableName = variableDefParts[1];
                                usageType = VariableUsage.USAGE_TYPE.OUTPUT_VARIABLE;
                            }
                        }
                        if (!isEmpty(variableName)) {
                            VariableUsage variableUsage = variableUsages.get(variableName);
                            if (variableUsage == null) {
                                variableUsage = new VariableUsage(variableName, usageType, node, displayName);
                                variableUsages.put(variableUsage.getVariableName(), variableUsage);
                            }
                            if (variableUsage.getUsageType() != usageType) {
                                variableUsage.setUsageType(VariableUsage.USAGE_TYPE.INPUT_OUTPUT_VARIABLE);
                            }
                        }
                    });
        }
        return variableUsages;
    }

    private static Optional<BiFunction<String, Pair<BPMNDefinition, Node<View<BPMNDefinition>, Edge>>, Collection<VariableUsage>>> lookupFindFunction(BPMNDefinition definition) {
        //This code should ideally be based on an iteration plus the invocation of Class.isAssignableFrom method, but unfortunately not available in GWT client classes
        if (definition instanceof BusinessRuleTask) {
            return Optional.of(findFunctions.getOrDefault(BusinessRuleTask.class, NO_USAGES));
        } else if (definition instanceof UserTask) {
            return Optional.of(findFunctions.getOrDefault(UserTask.class, NO_USAGES));
        } else if (definition instanceof ServiceTask) {
            return Optional.of(findFunctions.getOrDefault(ServiceTask.class, NO_USAGES));
        } else if (definition instanceof EndErrorEvent) {
            return Optional.of(findFunctions.getOrDefault(EndErrorEvent.class, NO_USAGES));
        } else if (definition instanceof EndEscalationEvent) {
            return Optional.of(findFunctions.getOrDefault(EndEscalationEvent.class, NO_USAGES));
        } else if (definition instanceof EndMessageEvent) {
            return Optional.of(findFunctions.getOrDefault(EndMessageEvent.class, NO_USAGES));
        } else if (definition instanceof EndSignalEvent) {
            return Optional.of(findFunctions.getOrDefault(EndSignalEvent.class, NO_USAGES));
        } else if (definition instanceof IntermediateErrorEventCatching) {
            return Optional.of(findFunctions.getOrDefault(IntermediateErrorEventCatching.class, NO_USAGES));
        } else if (definition instanceof IntermediateMessageEventCatching) {
            return Optional.of(findFunctions.getOrDefault(IntermediateMessageEventCatching.class, NO_USAGES));
        } else if (definition instanceof IntermediateSignalEventCatching) {
            return Optional.of(findFunctions.getOrDefault(IntermediateSignalEventCatching.class, NO_USAGES));
        } else if (definition instanceof IntermediateEscalationEvent) {
            return Optional.of(findFunctions.getOrDefault(IntermediateEscalationEvent.class, NO_USAGES));
        } else if (definition instanceof IntermediateEscalationEventThrowing) {
            return Optional.of(findFunctions.getOrDefault(IntermediateEscalationEventThrowing.class, NO_USAGES));
        } else if (definition instanceof IntermediateMessageEventThrowing) {
            return Optional.of(findFunctions.getOrDefault(IntermediateMessageEventThrowing.class, NO_USAGES));
        } else if (definition instanceof IntermediateSignalEventThrowing) {
            return Optional.of(findFunctions.getOrDefault(IntermediateSignalEventThrowing.class, NO_USAGES));
        } else if (definition instanceof StartErrorEvent) {
            return Optional.of(findFunctions.getOrDefault(StartErrorEvent.class, NO_USAGES));
        } else if (definition instanceof StartEscalationEvent) {
            return Optional.of(findFunctions.getOrDefault(StartEscalationEvent.class, NO_USAGES));
        } else if (definition instanceof StartMessageEvent) {
            return Optional.of(findFunctions.getOrDefault(StartMessageEvent.class, NO_USAGES));
        } else if (definition instanceof StartSignalEvent) {
            return Optional.of(findFunctions.getOrDefault(StartSignalEvent.class, NO_USAGES));
        } else if (definition instanceof ReusableSubprocess) {
            return Optional.of(findFunctions.getOrDefault(ReusableSubprocess.class, NO_USAGES));
        } else if (definition instanceof MultipleInstanceSubprocess) {
            return Optional.of(findFunctions.getOrDefault(MultipleInstanceSubprocess.class, NO_USAGES));
        }
        return Optional.of(NO_USAGES);
    }

    private static Map<Class<?>, BiFunction<String, Pair<BPMNDefinition, Node<View<BPMNDefinition>, Edge>>, Collection<VariableUsage>>> buildFindFunctions() {
        Map<Class<?>, BiFunction<String, Pair<BPMNDefinition, Node<View<BPMNDefinition>, Edge>>, Collection<VariableUsage>>> findFunctions = new HashMap<>();
        findFunctions.put(BusinessRuleTask.class, (s, pair) -> findVariableUsages(s, ((BusinessRuleTask) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(UserTask.class, (s, pair) -> findVariableUsages(s, ((UserTask) pair.getK1()), pair.getK2()));
        findFunctions.put(ServiceTask.class, (s, pair) -> findVariableUsages(s, ((ServiceTask) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(EndErrorEvent.class, (s, pair) -> findVariableUsages(s, ((EndErrorEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(EndEscalationEvent.class, (s, pair) -> findVariableUsages(s, ((EndEscalationEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(EndMessageEvent.class, (s, pair) -> findVariableUsages(s, ((EndMessageEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(EndSignalEvent.class, (s, pair) -> findVariableUsages(s, ((EndSignalEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateErrorEventCatching.class, (s, pair) -> findVariableUsages(s, ((IntermediateErrorEventCatching) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateMessageEventCatching.class, (s, pair) -> findVariableUsages(s, ((IntermediateMessageEventCatching) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateSignalEventCatching.class, (s, pair) -> findVariableUsages(s, ((IntermediateSignalEventCatching) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateEscalationEvent.class, (s, pair) -> findVariableUsages(s, ((IntermediateEscalationEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateEscalationEventThrowing.class, (s, pair) -> findVariableUsages(s, ((IntermediateEscalationEventThrowing) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateMessageEventThrowing.class, (s, pair) -> findVariableUsages(s, ((IntermediateMessageEventThrowing) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(IntermediateSignalEventThrowing.class, (s, pair) -> findVariableUsages(s, ((IntermediateSignalEventThrowing) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(StartErrorEvent.class, (s, pair) -> findVariableUsages(s, ((StartErrorEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(StartEscalationEvent.class, (s, pair) -> findVariableUsages(s, ((StartEscalationEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(StartMessageEvent.class, (s, pair) -> findVariableUsages(s, ((StartMessageEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(StartSignalEvent.class, (s, pair) -> findVariableUsages(s, ((StartSignalEvent) pair.getK1()).getDataIOSet().getAssignmentsinfo(), getDisplayName(pair.getK1()), pair.getK2()));
        findFunctions.put(ReusableSubprocess.class, (s, pair) -> findVariableUsages(s, (ReusableSubprocess) pair.getK1(), pair.getK2()));
        findFunctions.put(MultipleInstanceSubprocess.class, (s, pair) -> findVariableUsages(s, (MultipleInstanceSubprocess) pair.getK1(), pair.getK2()));
        return findFunctions;
    }
}