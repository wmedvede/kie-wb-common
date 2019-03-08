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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.dc.Bounds;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GraphUtils.class)
public abstract class AbstractBasePropertyWriterTest<W extends BasePropertyWriter, E extends BaseElement> {

    protected static final String ID = "ID";
    protected static final Double X1 = 1d;
    protected static final Double Y1 = 2d;
    protected static final Double X2 = 10d;
    protected static final Double Y2 = 20d;

    protected E element;

    @Mock
    protected VariableScope variableScope;

    protected W propertyWriter;

    @Before
    public void setUp() {
        element = mockElement();
        when(element.getId()).thenReturn(ID);
        List<Documentation> documentation = new ArrayList<>();
        when(element.getDocumentation()).thenReturn(documentation);
        propertyWriter = newPropertyWriter(element, variableScope);
    }

    protected abstract W newPropertyWriter(E baseElement, VariableScope variableScope);

    protected abstract E mockElement();

    @Test
    public void testGetId() {
        assertEquals(ID, propertyWriter.getId());
    }

    @Test
    public void testSetBounds() {
        org.kie.workbench.common.stunner.core.graph.content.Bounds bounds =
                org.kie.workbench.common.stunner.core.graph.content.Bounds.create(X1, Y1, X2, Y2);
        propertyWriter.setBounds(bounds);

        BPMNShape shape = propertyWriter.getShape();
        assertNotNull(shape);
        assertEquals("shape_" + ID, shape.getId());
        assertEquals(element, shape.getBpmnElement());
        Bounds shapeBounds = shape.getBounds();
        assertEquals(X1.floatValue(), shapeBounds.getX(), 0);
        assertEquals(Y1.floatValue(), shapeBounds.getY(), 0);
        assertEquals(X2.floatValue() - X1.floatValue(), shapeBounds.getWidth(), 0);
        assertEquals(Y2.floatValue() - Y1.floatValue(), shapeBounds.getHeight(), 0);
    }

    @Test
    public void testSetAbsoluteBounds() {
        testSetAbsoluteBounds(createNode());
        assertFalse(propertyWriter.getShape().isIsExpanded());
    }

    public Node<View, ?> createNode() {
        return mockNode(new Object(), org.kie.workbench.common.stunner.core.graph.content.Bounds.create(X1, Y1, X2, Y2));
    }

    protected void testSetAbsoluteBounds(Node<View, ?> node) {
        org.kie.workbench.common.stunner.core.graph.content.Bounds relativeBounds = node.getContent().getBounds();
        double absoluteX = 200;
        double absoluteY = 400;
        Point2D computedPosition = Point2D.create(absoluteX, absoluteY);
        mockStatic(GraphUtils.class);
        PowerMockito.when(GraphUtils.getComputedPosition(node)).thenReturn(computedPosition);
        propertyWriter.setAbsoluteBounds(node);

        Bounds shapeBounds = propertyWriter.getShape().getBounds();
        assertEquals(absoluteX, shapeBounds.getX(), 0);
        assertEquals(absoluteY, shapeBounds.getY(), 0);
        assertEquals(relativeBounds.getWidth(), shapeBounds.getWidth(), 0);
        assertEquals(relativeBounds.getHeight(), shapeBounds.getHeight(), 0);
    }

    @Test
    public void testGetElement() {
        assertEquals(element, propertyWriter.getElement());
    }

    @Test
    public void testSetNullDocumentation() {
        propertyWriter.setDocumentation(null);
        assertTrue(element.getDocumentation().isEmpty());
    }

    @Test
    public void testSetEmptyDocumentation() {
        propertyWriter.setDocumentation("");
        assertTrue(element.getDocumentation().isEmpty());
    }

    @Test
    public void testSetNonEmptyDocumentation() {
        String value = "some non empty value";
        propertyWriter.setDocumentation(value);
        assertEquals(1, element.getDocumentation().size());
        assertEquals("<![CDATA[" + value + "]]>", element.getDocumentation().get(0).getText());
    }

    @Test
    public void testAddItemDefinition() {
        ItemDefinition itemDefinition = mock(ItemDefinition.class);
        propertyWriter.addItemDefinition(itemDefinition);
        assertTrue(propertyWriter.getItemDefinitions().contains(itemDefinition));
    }

    @Test
    public void testAddRootElement() {
        RootElement rootElement = mock(RootElement.class);
        propertyWriter.addRootElement(rootElement);
        assertTrue(propertyWriter.getRootElements().contains(rootElement));
    }

    @SuppressWarnings("unchecked")
    protected static Node<View, ?> mockNode(Object definition, org.kie.workbench.common.stunner.core.graph.content.Bounds bounds) {
        Node<View, ?> node = mock(Node.class);
        View view = mock(View.class);
        when(node.getContent()).thenReturn(view);
        when(view.getBounds()).thenReturn(bounds);
        when(view.getDefinition()).thenReturn(definition);
        return node;
    }
}
