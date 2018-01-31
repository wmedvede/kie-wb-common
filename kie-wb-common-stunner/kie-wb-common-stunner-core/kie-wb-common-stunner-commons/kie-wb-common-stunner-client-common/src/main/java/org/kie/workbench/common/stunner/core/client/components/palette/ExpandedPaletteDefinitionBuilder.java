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
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionBuilders.CategoryBuilder;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionBuilders.GroupBuilder;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionBuilders.ItemBuilder;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionProviders.DefaultMessageProvider;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionProviders.DefaultMorphGroupMessageProvider;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryService;
import org.kie.workbench.common.stunner.core.definition.adapter.DefinitionAdapter;
import org.kie.workbench.common.stunner.core.definition.morph.MorphDefinition;
import org.kie.workbench.common.stunner.core.definition.shape.Glyph;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.i18n.StunnerTranslationService;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;

@Dependent
@Default
public class ExpandedPaletteDefinitionBuilder
        extends AbstractPaletteDefinitionBuilder<ExpandedPaletteDefinitionBuilder> {

    private Function<String, String> categoryDefinitionIdProvider;
    private Function<String, Glyph> categoryGlyphProvider;
    private ItemMessageProvider groupMessageProvider;
    private ItemMessageProvider categoryMessageProvider;

    public ExpandedPaletteDefinitionBuilder categoryDefinitionIdProvider(final Function<String, String> categoryDefinitionIdProvider) {
        this.categoryDefinitionIdProvider = categoryDefinitionIdProvider;
        return this;
    }

    public ExpandedPaletteDefinitionBuilder categoryGlyphProvider(final Function<String, Glyph> categoryGlyphProvider) {
        this.categoryGlyphProvider = categoryGlyphProvider;
        return this;
    }

    public ExpandedPaletteDefinitionBuilder groupMessages(final ItemMessageProvider provider) {
        this.groupMessageProvider = provider;
        return this;
    }

    public ExpandedPaletteDefinitionBuilder categoryMessages(final ItemMessageProvider provider) {
        this.categoryMessageProvider = provider;
        return this;
    }

    @Inject
    public ExpandedPaletteDefinitionBuilder(final DefinitionUtils definitionUtils,
                                            final ClientFactoryService clientFactoryServices,
                                            final StunnerTranslationService translationService) {
        super(definitionUtils, clientFactoryServices, translationService);
    }

    @Override
    protected void initDefaults() {
        super.initDefaults();
        this
                .categoryDefinitionIdProvider(id -> null)
                .categoryGlyphProvider(DefaultPaletteDefinitionProviders.DEFAULT_CATEGORY_GLYPH_PROVIDER)
                .groupMessages(new DefaultMorphGroupMessageProvider(translationService))
                .categoryMessages(new DefaultMessageProvider());
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
            DefaultPaletteCategory category = (DefaultPaletteCategory) items.get(categoryId);
            if (null == category) {
                final String catDefId = categoryDefinitionIdProvider.apply(categoryId);
                final String catTitle = categoryMessageProvider.getTitle(categoryId);
                final String catDesc = categoryMessageProvider.getDescription(categoryId);
                final Glyph categoryGlyph = categoryGlyphProvider.apply(categoryId);
                category = new CategoryBuilder()
                        .setItemId(categoryId)
                        .setDefinitionId(catDefId)
                        .setTitle(catTitle)
                        .setDescription(catDesc)
                        .setTooltip(catTitle)
                        .setGlyph(categoryGlyph)
                        .build();
                items.put(categoryId, category);
            }
            final MorphDefinition morphDefinition = definitionUtils.getMorphDefinition(definition);
            final boolean hasMorphBase = null != morphDefinition;
            String morphDefault = null;
            DefaultPaletteGroup group = null;
            if (hasMorphBase) {
                final String morphBaseId = morphDefinition.getBase();
                if (groupFilter.test(morphBaseId)) {
                    morphDefault = morphDefinition.getDefault();
                    final Optional<DefaultPaletteItem> groupOp = category.getItems().stream()
                            .filter(g -> g.getId().equals(morphBaseId))
                            .findFirst();
                    if (!groupOp.isPresent()) {
                        final String groupTitle = groupMessageProvider.getTitle(morphBaseId);
                        final String groupDesc = groupMessageProvider.getDescription(morphBaseId);
                        group = new GroupBuilder()
                                .setItemId(morphBaseId)
                                .setDefinitionId(morphDefault)
                                .setTitle(groupTitle)
                                .setDescription(groupDesc)
                                .build();
                        category.getItems().add(group);
                    } else {
                        group = (DefaultPaletteGroup) groupOp.get();
                    }
                }
            }

            final String title = definitionAdapter.getTitle(definition);
            final String description = definitionAdapter.getDescription(definition);
            final DefaultPaletteItem item =
                    new ItemBuilder()
                            .setItemId(id)
                            .setDefinitionId(id)
                            .setTitle(title)
                            .setDescription(description)
                            .build();

            if (null != group) {
                if (null != morphDefault && morphDefault.equals(id)) {
                    group.getItems().add(0, item);
                } else {
                    group.getItems().add(item);
                }
            } else {
                category.getItems().add(item);
            }
        }
    }

    public ItemMessageProvider getCategoryMessageProvider() {
        return categoryMessageProvider;
    }

    public ItemMessageProvider getGroupMessageProvider() {
        return groupMessageProvider;
    }

    public Function<String, Glyph> getCategoryGlyphProvider() {
        return categoryGlyphProvider;
    }

    public Function<String, String> getCategoryDefinitionIdProvider() {
        return categoryDefinitionIdProvider;
    }
}
