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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Height;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Width;
import org.kie.workbench.common.stunner.core.graph.content.Bound;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;

public class ProcessPostConverter {

    public void postConvert(BpmnNode rootNode) {
        //TODO WM, ver los lanes.
        rootNode.getChildren().stream()
                .filter(ProcessPostConverter::isSubProcess)
                .forEach(this::postConvertSubProcess);

        List<BpmnNode> resizedChildren = getResizedChildren(rootNode);
        resizedChildren.forEach(resizedChild -> applyNodeResize(rootNode, resizedChild));
    }

    private void postConvertSubProcess(BpmnNode subProcess) {
        subProcess.getChildren().stream()
                .filter(ProcessPostConverter::isSubProcess)
                .forEach(this::postConvertSubProcess);

        /*
        Alternativas
        1) subproceso estaba colapsado y no hay hijos resized
            1.1) calcular en nuevo tamanño q debe tener el suproceso
                    esta parte es "facil" porque se puede hacer utilzando la posicion actual del los hijos
                    ya que en el subproceso nada ha cambiado
        2) subproceso estaba colapsado pero ademas hay hijos resized
                    acá habria que iterar primero los hijos que han sido resized uno a uno
                    y reubicar los nodos restantes empujandolos etc
                    y luego al final podemos calcular el tamaño del subproceso una vez todos los hijos han sido reubicados

        3) subproceso no estaba colapsado pero hay hijos resized.
                    lo mismo tenemos que iterar primero los hijos resized uno a uno
                    y reubicar los nodos restantes empujandolos, etc, y luego al final
                    podemos calcular el tamaño del subproceso aunque este en realidad no estaba colapsado.

        */

        List<BpmnNode> resizedChildren = getResizedChildren(subProcess);
        resizedChildren.forEach(resizedChild -> applyNodeResize(subProcess, resizedChild));
        if (subProcess.getPropertyReader().isCollapsed() || !resizedChildren.isEmpty()) {
            resizeSubProcess(subProcess);
        }
        if (subProcess.getPropertyReader().isCollapsed()) {
            Bound subProcessUl = subProcess.value().getContent().getBounds().getUpperLeft();
            subProcess.getChildren().forEach(child -> translate(child, subProcessUl.getX(), subProcessUl.getY()));
        }
    }

    private static void resizeSubProcess(BpmnNode subProcess) {
        double padding = 10;
        List<Bound> ulBounds = subProcess.getChildren().stream()
                .map(child -> child.value().getContent().getBounds().getUpperLeft())
                .collect(Collectors.toList());
        List<Bound> lrBounds = subProcess.getChildren().stream()
                .map(child -> child.value().getContent().getBounds().getLowerRight())
                .collect(Collectors.toList());
        double xUl = minX(ulBounds);
        double yUl = minY(ulBounds);
        double xLr = maxX(lrBounds);
        double yLr = maxY(lrBounds);
        double width = xLr - xUl;
        double height = yLr - yUl;

        Bounds subProcessBounds = subProcess.value().getContent().getBounds();
        Bound subProcessUl = subProcessBounds.getUpperLeft();
        Bound subProcessLr = subProcessBounds.getLowerRight();
        subProcessLr.setX(subProcessUl.getX() + width + padding);
        subProcessLr.setY(subProcessUl.getY() + height + padding);
        RectangleDimensionsSet subProcessRectangle = ((BaseSubprocess) subProcess.value().getContent().getDefinition()).getDimensionsSet();
        subProcessRectangle.setWidth(new Width(width));
        subProcessRectangle.setHeight(new Height(height));

        //TODO ver sino tengo q resizar tambien el rectangle...

        subProcess.setResized(true);
    }

    private static double minX(List<Bound> bounds) {
        return Collections.min(bounds.stream().map(Bound::getX).collect(Collectors.toList()));
    }

    private static double maxX(List<Bound> bounds) {
        return Collections.max(bounds.stream().map(Bound::getX).collect(Collectors.toList()));
    }

    private static double minY(List<Bound> bounds) {
        return Collections.min(bounds.stream().map(Bound::getY).collect(Collectors.toList()));
    }

    private static double maxY(List<Bound> bounds) {
        return Collections.max(bounds.stream().map(Bound::getY).collect(Collectors.toList()));
    }

    private static void applyNodeResize(BpmnNode container, BpmnNode resizedChild) {
        Bounds originalBounds = resizedChild.getPropertyReader().getBounds();
        Bounds currentBounds = resizedChild.value().getContent().getBounds();
        double deltaX = currentBounds.getWidth() - originalBounds.getWidth();
        double deltaY = currentBounds.getHeight() - originalBounds.getHeight();
        container.getChildren().stream()
                .filter(child -> needsTranslation(originalBounds.getUpperLeft(), child.value().getContent().getBounds()))
                .forEach(child -> translate(child, deltaX, deltaY));
    }

    private static void translate(BpmnNode child, double deltaX, double deltaY) {
        //TODO WM, ver los nodos q son circulos...
        Bounds childBounds = child.value().getContent().getBounds();
        translate(childBounds.getUpperLeft(), deltaX, deltaY);
        translate(childBounds.getLowerRight(), deltaX, deltaY);
    }

    private static void translate(Bound bound, double deltaX, double deltaY) {
        bound.setX(bound.getX() + deltaX);
        bound.setY(bound.getY() + deltaY);
    }

    private static boolean needsTranslation(Bound ul, Bounds bounds) {
        return bounds.getUpperLeft().getX() >= ul.getX() && bounds.getUpperLeft().getY() >= ul.getY() ||
                bounds.getLowerRight().getX() >= ul.getX() && bounds.getLowerRight().getY() >= ul.getY();
    }

    private static boolean isSubProcess(BpmnNode node) {
        return node.value().getContent().getDefinition() instanceof EmbeddedSubprocess;
    }

    private static List<BpmnNode> getResizedChildren(BpmnNode node) {
        return node.getChildren().stream()
                .filter(BpmnNode::isResized)
                .collect(Collectors.toList());
    }
}
