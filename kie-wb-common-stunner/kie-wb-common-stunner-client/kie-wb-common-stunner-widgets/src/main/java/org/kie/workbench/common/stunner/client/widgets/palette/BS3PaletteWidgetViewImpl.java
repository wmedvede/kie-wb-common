/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.client.widgets.palette;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Node;
import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.soup.commons.validation.PortablePreconditions;
import org.kie.workbench.common.stunner.core.client.components.drag.DragProxy;
import org.kie.workbench.common.stunner.core.client.components.drag.DragProxyCallback;
import org.kie.workbench.common.stunner.core.client.components.glyph.ShapeGlyphDragHandler;
import org.kie.workbench.common.stunner.core.definition.shape.Glyph;

@Templated
@Dependent
public class BS3PaletteWidgetViewImpl implements BS3PaletteWidgetView,
                                                 IsElement {

    private ShapeGlyphDragHandler shapeGlyphDragHandler;

    @Inject
    @DataField("kie-palette")
    private Div palette;

    @Inject
    @DataField("list-group")
    private UnorderedList ul;

    private BS3PaletteWidget presenter;

    private DragProxy itemDragProxy;

    @Override
    public void init(BS3PaletteWidget presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setShapeGlyphDragHandler(ShapeGlyphDragHandler shapeGlyphDragHandler) {
        this.shapeGlyphDragHandler = shapeGlyphDragHandler;
    }

    @Override
    public void showDragProxy(String itemId,
                              double x,
                              double y,
                              double width,
                              double height) {
        //TODO remove, ok ya sabemos que hay q comenzar a hacer drag de un item
        //recuperamos el icono q habria q presentar
        final Glyph glyph = presenter.getShapeGlyph(itemId);
        //TODO remove, ya tenemos un componente de base ncargado de hacer el drag el icono.!!
        itemDragProxy = shapeGlyphDragHandler.show(new ShapeGlyphDragHandler.Item() {
                                                       @Override
                                                       public Glyph getShape() {
                                                           return glyph;
                                                       }

                                                       @Override
                                                       public int getWidth() {
                                                           return (int) width;
                                                       }

                                                       @Override
                                                       public int getHeight() {
                                                           return (int) height;
                                                       }
                                                   },
                                                   (int) x,
                                                   (int) y,
                                                   new DragProxyCallback() {
                                                       @Override
                                                       public void onStart(int x,
                                                                           int y) {
                                                           //TODO remove, guay, ese componente ya informa a la paleta
                                                           //sobre el ciclo del dragg q se esta ejecutando.
                                                           //informa a la paleta q arranca el drag
                                                           presenter.onDragStart(itemId,
                                                                                 x,
                                                                                 y);
                                                       }

                                                       @Override
                                                       public void onMove(int x,
                                                                          int y) {
                                                           //TODO remove, informa a la paleta que el elemento se esta moviendo
                                                           presenter.onDragProxyMove(itemId,
                                                                                     (double) x,
                                                                                     (double) y);
                                                       }

                                                       @Override
                                                       public void onComplete(int x,
                                                                              int y) {
                                                           //TODO remove, informa a la paleta que el drag del elemento ha terminado
                                                           presenter.onDragProxyComplete(itemId,
                                                                                         (double) x,
                                                                                         (double) y);
                                                       }
                                                   });
    }

    @Override
    public void add(BS3PaletteWidgetPresenter widget) {
        PortablePreconditions.checkNotNull("widget",
                                           widget);
        addElement(widget.getElement());
    }

    public final void addElement(Node widget) {
        ul.appendChild(widget);
    }

    @Override
    public void clear() {
        DOMUtil.removeAllChildren(ul);
        if (Objects.nonNull(itemDragProxy)) {
            itemDragProxy.clear();
        }
    }

    @Override
    public void destroy() {
        clear();
        if (Objects.nonNull(itemDragProxy)) {
            itemDragProxy.destroy();
        }
    }

    @Override
    public void setBackgroundColor(String backgroundColor) {
        palette.getStyle().setProperty("background-color",
                                       backgroundColor);
    }

    @Override
    public void showEmptyView(boolean showEmptyView) {
        palette.setHidden(showEmptyView);
        ul.setHidden(showEmptyView);
    }
}