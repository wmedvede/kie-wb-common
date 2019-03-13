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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Height;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Width;
import org.kie.workbench.common.stunner.core.graph.content.Bound;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;

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
            translate(subProcess.getEdges(), subProcessUl.getX(), subProcessUl.getY());
        }
    }

    private static void resizeSubProcess(BpmnNode subProcess) {
        List<Bound> ulBounds = subProcess.getChildren().stream()
                .map(child -> child.value().getContent().getBounds().getUpperLeft())
                .collect(Collectors.toList());
        List<Bound> lrBounds = subProcess.getChildren().stream()
                .map(child -> child.value().getContent().getBounds().getLowerRight())
                .collect(Collectors.toList());
        List<Point2D> controlPoints = subProcess.getEdges().stream()
                .filter(edge -> edge instanceof BpmnEdge.Simple)
                .map(edge -> (BpmnEdge.Simple) edge)
                .map(BpmnEdge.Simple::getControlPoints)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        double leftPadding = Math.min(min(ulBounds, Bound::getX), min(controlPoints, Point2D::getX));
        double topPadding = Math.min(min(ulBounds, Bound::getY), min(controlPoints, Point2D::getY));
        double width = Math.max(max(lrBounds, Bound::getX), max(controlPoints, Point2D::getX)) + leftPadding;
        double height = Math.max(max(lrBounds, Bound::getY), max(controlPoints, Point2D::getY)) + topPadding;

        Bounds subProcessBounds = subProcess.value().getContent().getBounds();
        double originalWidth = subProcessBounds.getWidth();
        double originalHeight = subProcessBounds.getHeight();
        Bound subProcessUl = subProcessBounds.getUpperLeft();
        Bound subProcessLr = subProcessBounds.getLowerRight();
        subProcessLr.setX(subProcessUl.getX() + width);
        subProcessLr.setY(subProcessUl.getY() + height);
        RectangleDimensionsSet subProcessRectangle = ((BaseSubprocess) subProcess.value().getContent().getDefinition()).getDimensionsSet();
        subProcessRectangle.setWidth(new Width(width));
        subProcessRectangle.setHeight(new Height(height));
        subProcess.setResized(true);

        //necesito todas las edges entrantes o salientes y acomodarles al menos el sourceConnection y targetConnection
        List<BpmnEdge.Simple> inEdges = subProcess.getParent().getEdges().stream()
                .filter(edge -> edge instanceof BpmnEdge.Simple)
                .filter(edge -> edge.getTarget() == subProcess)
                .map(edge -> (BpmnEdge.Simple) edge)
                .collect(Collectors.toList());
        List<BpmnEdge.Simple> outEdges = subProcess.getParent().getEdges().stream()
                .filter(edge -> edge instanceof BpmnEdge.Simple)
                .filter(edge -> edge.getSource() == subProcess)
                .map(edge -> (BpmnEdge.Simple) edge)
                .collect(Collectors.toList());

        double widthFactor = width / originalWidth;
        double heightFactor = height / originalHeight;
        inEdges.forEach(edge -> scale(edge.getTargetConnection().getLocation(), widthFactor, heightFactor));
        outEdges.forEach(edge -> scale(edge.getSourceConnection().getLocation(), widthFactor, heightFactor));

    }

    private static <X, T extends Object & Comparable<? super T>> T min(List<X> values, Function<X, T> mapper) {
        return Collections.min(values.stream().map(mapper).collect(Collectors.toList()));
    }

    private static <X, T extends Object & Comparable<? super T>> T max(List<X> values, Function<X, T> mapper) {
        return Collections.max(values.stream().map(mapper).collect(Collectors.toList()));
    }

    private static void applyNodeResize(BpmnNode container, BpmnNode resizedChild) {
        Bounds originalBounds = resizedChild.getPropertyReader().getBounds();
        Bounds currentBounds = resizedChild.value().getContent().getBounds();
        double deltaX = currentBounds.getWidth() - originalBounds.getWidth();
        double deltaY = currentBounds.getHeight() - originalBounds.getHeight();
        container.getChildren().stream()
                .filter(child -> child != resizedChild)
                .filter(child -> needsTranslation(originalBounds.getUpperLeft(), child.value().getContent().getBounds()))
                .forEach(child -> translate(child, deltaX, deltaY));
        translate(container.getEdges(), deltaX, deltaY);
    }

    private static void translate(BpmnNode node, double deltaX, double deltaY) {
        //TODO WM, ver los nodos q son circulos...
        Bounds childBounds = node.value().getContent().getBounds();
        translate(childBounds.getUpperLeft(), deltaX, deltaY);
        translate(childBounds.getLowerRight(), deltaX, deltaY);
        node.getChildren().forEach(child -> translate(child, deltaX, deltaY));
        translate(node.getEdges(), deltaX, deltaY);
    }

    private static void translate(List<BpmnEdge> edges, double deltaX, double deltaY) {
        edges.stream()
                .filter(edge -> edge instanceof BpmnEdge.Simple)
                .map(edge -> (BpmnEdge.Simple) edge)
                .forEach(edge -> translate(edge, deltaX, deltaY));
    }

    private static void translate(BpmnEdge.Simple edge, double deltaX, double deltaY) {
        //source and target connections points are relative to the respective source and target node, so don't need translation.
        //only the control points are translated.
        edge.getControlPoints().forEach(controlPoint -> translate(controlPoint, deltaX, deltaY));
    }

    private static void translate(Point2D point, double deltaX, double deltaY) {
        point.setX(point.getX() + deltaX);
        point.setY(point.getY() + deltaY);
    }

    private static void translate(Bound bound, double deltaX, double deltaY) {
        bound.setX(bound.getX() + deltaX);
        bound.setY(bound.getY() + deltaY);
    }

    private static void scale(Point2D point2D, double widthFactor, double heightFactor) {
        point2D.setX(point2D.getX() * widthFactor);
        point2D.setY(point2D.getY() * heightFactor);
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
