/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import java.util.List;

import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;

public class ProcessPostConverter {

    public void postConvert(BpmnNode rootNode) {
        rootNode.getChildren().stream()
                .filter(ProcessPostConverter::isSubProcess)
                .forEach(this::postConvertSubProcess);
    }

    private void postConvertSubProcess(BpmnNode subProcess) {
        subProcess.getChildren().stream()
                .filter(ProcessPostConverter::isSubProcess)
                .forEach(this::postConvertSubProcess);
        if (subProcess.getPropertyReader().isCollapsed() || hasResizedChild(subProcess)) {
            //bueno acá habria q aplicar el algoritmo
            //seguir aca mañana....
        }

    }

    private static boolean isSubProcess(BpmnNode node) {
        return node.value().getContent().getDefinition() instanceof EmbeddedSubprocess;
    }

    private static boolean hasResizedChild(BpmnNode node) {
        return node.getChildren().stream().anyMatch(BpmnNode::isResized);
    }

}
