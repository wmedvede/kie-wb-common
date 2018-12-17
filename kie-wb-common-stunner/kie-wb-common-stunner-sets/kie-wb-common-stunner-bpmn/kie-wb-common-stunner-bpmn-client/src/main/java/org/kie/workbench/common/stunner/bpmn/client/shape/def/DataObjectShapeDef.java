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

package org.kie.workbench.common.stunner.bpmn.client.shape.def;

import java.util.Optional;

import org.kie.workbench.common.stunner.bpmn.client.resources.BPMNGlyphFactory;
import org.kie.workbench.common.stunner.bpmn.client.resources.BPMNSVGViewFactory;
import org.kie.workbench.common.stunner.bpmn.definition.DataObject;
import org.kie.workbench.common.stunner.core.client.shape.view.handler.SizeHandler;
import org.kie.workbench.common.stunner.core.definition.shape.Glyph;
import org.kie.workbench.common.stunner.svg.client.shape.view.SVGShapeView;

public class DataObjectShapeDef extends BaseDimensionedShapeDef
        implements BPMNSvgShapeDef<DataObject> {

    @Override
    public SizeHandler<DataObject, SVGShapeView> newSizeHandler() {
        return newSizeHandlerBuilder()
                .width(dataObject -> dataObject.getDimensionsSet().getWidth().getValue())
                .height(dataObject -> dataObject.getDimensionsSet().getHeight().getValue())
                .minWidth(dataObject -> 50d)
                .maxWidth(dataObject -> 400d)
                .minHeight(dataObject -> 50d)
                .maxHeight(dataObject -> 400d)
                .build();
    }

    @Override
    public SVGShapeView<?> newViewInstance(final BPMNSVGViewFactory factory,
                                           final DataObject dataObject) {
        //TODO WM, Use the data object shape instead of the script
        return newViewInstance(Optional.ofNullable(dataObject.getDimensionsSet().getWidth()),
                               Optional.ofNullable(dataObject.getDimensionsSet().getHeight()),
                               factory.dataObject());
    }

    @Override
    public Glyph getGlyph(final Class<? extends DataObject> dataObject, final String defId) {
        //TODO WM, Use the data object shape instead of the script
        return BPMNGlyphFactory.TASK_SCRIPT;
    }
}