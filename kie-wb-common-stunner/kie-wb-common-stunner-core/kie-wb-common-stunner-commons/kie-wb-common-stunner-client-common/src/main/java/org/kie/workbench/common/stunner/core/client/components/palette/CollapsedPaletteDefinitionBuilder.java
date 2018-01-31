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

package org.kie.workbench.common.stunner.core.client.components.palette;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionBuilders.ItemBuilder;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryService;
import org.kie.workbench.common.stunner.core.definition.adapter.DefinitionAdapter;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.i18n.StunnerTranslationService;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;

@Dependent
@Typed(CollapsedPaletteDefinitionBuilder.class)
public class CollapsedPaletteDefinitionBuilder
        extends AbstractPaletteDefinitionBuilder<CollapsedPaletteDefinitionBuilder> {

    private static final int ICON_SIZE = 35;

    @Inject
    public CollapsedPaletteDefinitionBuilder(final DefinitionUtils definitionUtils,
                                             final ClientFactoryService clientFactoryServices,
                                             final StunnerTranslationService translationService) {
        super(definitionUtils, clientFactoryServices, translationService);
    }

    @Override
    protected void buildItem(final Object definition,
                             final Metadata metadata,
                             final Map<String, DefaultPaletteItem> items) {
        final DefinitionAdapter<Object> definitionAdapter = getDefinitionManager().adapters().forDefinition();
        final String id = definitionAdapter.getId(definition);
        final String categoryId = definitionAdapter.getCategory(definition);
        // Check if this concrete category excluded from the palette model.
        if (categoryFilter.test(categoryId)) {
            final String title = definitionAdapter.getTitle(definition);
            final String description = definitionAdapter.getDescription(definition);
            // Notice it creates the item by using the title as for the item's tooltip property,
            // setting this an empty item title, in order to not display text once the rendered
            // item is displaed, just the icon with the given tooltip.
            final DefaultPaletteItem item =
                    new ItemBuilder()
                            .setItemId(id)
                            .setDefinitionId(id)
                            .setDescription(description)
                            .setTooltip(title)
                            .setIconSize(ICON_SIZE)
                            .build();
            items.put(id, item);
        }
    }
}
