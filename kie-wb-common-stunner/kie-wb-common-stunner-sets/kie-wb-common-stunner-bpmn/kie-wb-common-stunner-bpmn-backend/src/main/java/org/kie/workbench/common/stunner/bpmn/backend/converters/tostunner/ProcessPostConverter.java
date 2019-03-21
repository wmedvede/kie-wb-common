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
import java.util.stream.Stream;

import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Height;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.RectangleDimensionsSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Width;
import org.kie.workbench.common.stunner.core.graph.content.Bound;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;

public class ProcessPostConverter {

    private static double PRECISION = 0.5;

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
        if ((subProcess.isSubprocess() && subProcess.isCollapsed() && !subProcess.getChildren().isEmpty()) || !resizedChildren.isEmpty()) {
            resizeSubProcess(subProcess);
        }
        if (subProcess.isSubprocess() && subProcess.isCollapsed()) {
            Bound subProcessUl = subProcess.value().getContent().getBounds().getUpperLeft();
            subProcess.getChildren().forEach(child -> translate(child, subProcessUl.getX(), subProcessUl.getY()));
            translate(subProcess.getEdges(), subProcessUl.getX(), subProcessUl.getY());
            subProcess.setCollapsed(false);
        }
    }

    private static List<BpmnNode> getResizedChildren(BpmnNode node) {
        return node.getChildren().stream()
                .filter(BpmnNode::isResized)
                .collect(Collectors.toList());
    }

    private static void resizeSubProcess(BpmnNode subProcess) {
        if (!subProcess.getChildren().isEmpty()) {
            List<Bound> ulBounds = subProcess.getChildren().stream()
                    .map(child -> child.value().getContent().getBounds().getUpperLeft())
                    .collect(Collectors.toList());
            List<Bound> lrBounds = subProcess.getChildren().stream()
                    .map(child -> child.value().getContent().getBounds().getLowerRight())
                    .collect(Collectors.toList());
            List<Point2D> controlPoints = toSimpleEdgesStream(subProcess.getEdges())
                    .map(BpmnEdge.Simple::getControlPoints)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            double leftPadding;
            double topPadding;
            double width;
            double height;

            if (controlPoints.isEmpty()) {
                leftPadding = min(ulBounds, Bound::getX);
                topPadding = min(ulBounds, Bound::getY);
                width = max(lrBounds, Bound::getX) + leftPadding;
                height = max(lrBounds, Bound::getY) + topPadding;
            } else {
                leftPadding = Math.min(min(ulBounds, Bound::getX), min(controlPoints, Point2D::getX));
                topPadding = Math.min(min(ulBounds, Bound::getY), min(controlPoints, Point2D::getY));
                width = Math.max(max(lrBounds, Bound::getX), max(controlPoints, Point2D::getX)) + leftPadding;
                height = Math.max(max(lrBounds, Bound::getY), max(controlPoints, Point2D::getY)) + topPadding;
            }

            Bounds subProcessBounds = subProcess.value().getContent().getBounds();
            double originalWidth = subProcessBounds.getWidth();
            double originalHeight = subProcessBounds.getHeight();
            Bound subProcessUl = subProcessBounds.getUpperLeft();
            Bound subProcessLr = subProcessBounds.getLowerRight();
            subProcessLr.setX(subProcessUl.getX() + width);
            subProcessLr.setY(subProcessUl.getY() + height);

            RectangleDimensionsSet subProcessRectangle;
            if (subProcess.value().getContent().getDefinition() instanceof BaseSubprocess) {
                subProcessRectangle = ((BaseSubprocess) subProcess.value().getContent().getDefinition()).getDimensionsSet();
            } else {
                subProcessRectangle = ((Lane) subProcess.value().getContent().getDefinition()).getDimensionsSet();
            }
            subProcessRectangle.setWidth(new Width(width));
            subProcessRectangle.setHeight(new Height(height));
            subProcess.setResized(true);

            double widthFactor = width / originalWidth;
            double heightFactor = height / originalHeight;
            inEdges(subProcess.getParent(), subProcess).forEach(edge -> scale(edge.getTargetConnection().getLocation(), widthFactor, heightFactor));
            outEdges(subProcess.getParent(), subProcess).forEach(edge -> scale(edge.getSourceConnection().getLocation(), widthFactor, heightFactor));
        }
    }

    private static void adjustEdgeConnection(BpmnEdge.Simple edge, boolean targetConnection) {
        Point2D siblingPoint = null;
        Point2D connnectionPoint;
        BpmnNode connectionPointNode;
        List<Point2D> controlPoints = edge.getControlPoints();
        if (targetConnection) {
            connnectionPoint = edge.getTargetConnection().getLocation();
            connectionPointNode = edge.getTarget();
            if (controlPoints.size() >= 1) {
                siblingPoint = controlPoints.get(controlPoints.size() - 1);
            }
        } else {
            connnectionPoint = edge.getSourceConnection().getLocation();
            connectionPointNode = edge.getSource();
            if (controlPoints.size() >= 1) {
                siblingPoint = controlPoints.get(0);
            }
        }
        if (siblingPoint != null) {
            Bounds bounds = connectionPointNode.value().getContent().getBounds();
            Bound nodeUl = bounds.getUpperLeft();
            if (equals(connnectionPoint.getY(), 0, PRECISION) || equals(connnectionPoint.getY(), bounds.getHeight(), PRECISION)) {
                //scaled point is on top or bottom
                if (siblingPoint.getY() != (connnectionPoint.getY() + nodeUl.getY())) {
                    siblingPoint.setX(nodeUl.getX() + (bounds.getWidth() / 2));
                }
            } else {
                //scaled point left or right
                if (siblingPoint.getX() != (connnectionPoint.getX() + nodeUl.getX())) {
                    siblingPoint.setY(nodeUl.getY() + (bounds.getHeight() / 2));
                }
            }
        }
    }

    private static void applyNodeResize(BpmnNode container, BpmnNode resizedChild) {
        Bounds originalBounds = resizedChild.getPropertyReader().getBounds();
        Bounds currentBounds = resizedChild.value().getContent().getBounds();
        double deltaX = currentBounds.getWidth() - originalBounds.getWidth();
        double deltaY = currentBounds.getHeight() - originalBounds.getHeight();
        container.getChildren().stream()
                .filter(child -> child != resizedChild)
                .forEach(child -> applyTranslationIfRequired(currentBounds.getX(), currentBounds.getY(), deltaX, deltaY, child));

        toSimpleEdgesStream(container.getEdges()).forEach(edge -> applyTranslationIfRequired(currentBounds.getX(), currentBounds.getY(), deltaX, deltaY, edge));

        /*
        inEdges(container, resizedChild).forEach(edge -> adjustEdgeConnection(edge, true));
        inEdges(container, resizedChild).forEach(edge -> adjustEdgeConnection(edge, false));
        outEdges(container, resizedChild).forEach(edge -> adjustEdgeConnection(edge, false));
        outEdges(container, resizedChild).forEach(edge -> adjustEdgeConnection(edge, true));
        */

        toSimpleEdgesStream(container.getEdges()).forEach(edge -> adjustEdgeConnection(edge, true));
        toSimpleEdgesStream(container.getEdges()).forEach(edge -> adjustEdgeConnection(edge, false));
    }

    private static void translate(BpmnNode node, double deltaX, double deltaY) {
        //TODO WM, ver los nodos q son circulos...
        Bounds childBounds = node.value().getContent().getBounds();
        translate(childBounds.getUpperLeft(), deltaX, deltaY);
        translate(childBounds.getLowerRight(), deltaX, deltaY);
        if (!node.isCollapsed()) {
            node.getChildren().forEach(child -> translate(child, deltaX, deltaY));
            translate(node.getEdges(), deltaX, deltaY);
        }
    }

    private static void translate(List<BpmnEdge> edges, double deltaX, double deltaY) {
        toSimpleEdgesStream(edges).forEach(edge -> translate(edge, deltaX, deltaY));
    }

    private static void translate(BpmnEdge.Simple edge, double deltaX, double deltaY) {
        //source and target connections points are relative to the respective source and target node, so don't need
        // translation. Only the control points are translated.
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

    private static void applyTranslationIfRequired(double x, double y, double deltaX, double deltaY, BpmnNode node) {
        Bounds bounds = node.value().getContent().getBounds();
        Bound ul = bounds.getUpperLeft();
        if (ul.getX() >= x && ul.getY() >= y) {
            translate(node, deltaX, deltaY);
        } else if (ul.getX() >= x && ul.getY() < y) {
            translate(node, deltaX, 0);
        } else if (ul.getX() < x && ul.getY() >= y) {
            translate(node, 0, deltaY);
        }
    }

    private static void applyTranslationIfRequired(double x, double y, double deltaX, double deltaY, BpmnEdge.Simple edge) {
        edge.getControlPoints().forEach(point -> applyTranslationIfRequired(x, y, deltaX, deltaY, point));
    }

    private static void applyTranslationIfRequired(double x, double y, double deltaX, double deltaY, Point2D point) {
        if (point.getX() >= x && point.getY() >= y) {
            translate(point, deltaX, deltaY);
        } else if (point.getX() >= x && point.getY() < y) {
            translate(point, deltaX, 0);
        } else if (point.getX() < x && point.getY() >= y) {
            translate(point, 0, deltaY);
        }
    }

    private static boolean isSubProcess(BpmnNode node) {
        return node.value().getContent().getDefinition() instanceof EmbeddedSubprocess || node.value().getContent().getDefinition() instanceof Lane;
    }

    private static List<BpmnEdge.Simple> inEdges(BpmnNode container, BpmnNode targetNode) {
        return toSimpleEdgesStream(container.getEdges())
                .filter(edge -> edge.getTarget() == targetNode)
                .collect(Collectors.toList());
    }

    private static List<BpmnEdge.Simple> outEdges(BpmnNode container, BpmnNode sourceNode) {
        return toSimpleEdgesStream(container.getEdges())
                .filter(edge -> edge.getSource() == sourceNode)
                .collect(Collectors.toList());
    }

    private static Stream<BpmnEdge.Simple> toSimpleEdgesStream(List<BpmnEdge> edges) {
        return edges.stream()
                .filter(edge -> edge instanceof BpmnEdge.Simple)
                .map(edge -> (BpmnEdge.Simple) edge);
    }

    private static <X, T extends Object & Comparable<? super T>> T min(List<X> values, Function<X, T> mapper) {
        return Collections.min(values.stream().map(mapper).collect(Collectors.toList()));
    }

    private static <X, T extends Object & Comparable<? super T>> T max(List<X> values, Function<X, T> mapper) {
        return Collections.max(values.stream().map(mapper).collect(Collectors.toList()));
    }

    private static boolean equals(double a, double b, double delta) {
        if (Double.compare(a, b) == 0) {
            return true;
        } else {
            return Math.abs(a - b) < delta;
        }
    }
}
