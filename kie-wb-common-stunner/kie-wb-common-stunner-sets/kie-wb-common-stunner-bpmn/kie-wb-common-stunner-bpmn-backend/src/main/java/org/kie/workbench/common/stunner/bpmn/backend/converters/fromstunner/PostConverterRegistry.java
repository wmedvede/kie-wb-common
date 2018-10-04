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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.events.EndCompensationEventPostConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.events.IntermediateCompensationEventPostConverter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.EndCompensationEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateCompensationEventThrowing;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

//TODO WM terminar de ver esto, si sigo con la regitry o paso a un enfoque parecido al de Eduardo
public class PostConverterRegistry {

    private Map<Class<? extends BPMNViewDefinition>, PostConverterProcessor> postConverters = new HashMap<>();

    public PostConverterRegistry() {
        postConverters.put(IntermediateCompensationEventThrowing.class,
                           new IntermediateCompensationEventPostConverter());
        postConverters.put(EndCompensationEvent.class,
                           new EndCompensationEventPostConverter());
    }

    public Optional<PostConverterProcessor> getPostConverter(Node<View<? extends BPMNViewDefinition>, ?> node) {
        return Optional.ofNullable(postConverters.get(node.getContent().getDefinition().getClass()));
    }
}
