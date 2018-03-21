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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.components.palette.DefaultPaletteDefinitionProviders.DefaultItemMessageProvider;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryService;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.definition.adapter.DefinitionAdapter;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.i18n.StunnerTranslationService;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.uberfire.mvp.Command;

public abstract class AbstractPaletteDefinitionBuilder<T extends AbstractPaletteDefinitionBuilder>
        implements PaletteDefinitionBuilder<AbstractCanvasHandler, DefaultPaletteDefinition> {

    public interface ItemMessageProvider {

        String getTitle(String id);

        String getDescription(String id);
    }

    private static Logger LOGGER = Logger.getLogger(AbstractPaletteDefinitionBuilder.class.getName());

    protected final DefinitionUtils definitionUtils;
    protected final ClientFactoryService clientFactoryServices;
    protected final StunnerTranslationService translationService;

    protected Predicate<String> itemFilter;
    protected Predicate<String> categoryFilter;
    protected ItemMessageProvider itemMessageProvider;

    protected AbstractPaletteDefinitionBuilder(final DefinitionUtils definitionUtils,
                                               final ClientFactoryService clientFactoryServices,
                                               final StunnerTranslationService translationService) {
        this.definitionUtils = definitionUtils;
        this.clientFactoryServices = clientFactoryServices;
        this.translationService = translationService;
        initDefaults();
    }

    public T itemFilter(final Predicate<String> definitionItemFilter) {
        this.itemFilter = definitionItemFilter;
        return cast();
    }

    public T categoryFilter(final Predicate<String> categoryFilter) {
        this.categoryFilter = categoryFilter;
        return cast();
    }

    public T itemMessages(final ItemMessageProvider provider) {
        this.itemMessageProvider = provider;
        return cast();
    }

    @Override
    public void build(final AbstractCanvasHandler canvasHandler,
                      final Consumer<DefaultPaletteDefinition> paletteDefinition) {
        build(canvasHandler.getDiagram().getMetadata(),
              paletteDefinition);
    }

    public Predicate<String> getItemFilter() {
        return itemFilter;
    }

    public Predicate<String> getCategoryFilter() {
        return categoryFilter;
    }

    protected abstract DefaultPaletteItem createItem(Object definition,
                                                     DefinitionAdapter<Object> definitionAdapter,
                                                     Metadata metadata,
                                                     Function<String, DefaultPaletteItem> itemSupplier);

    private void build(final Metadata metadata,
                       final Consumer<DefaultPaletteDefinition> paletteDefinitionConsumer) {
        final String definitionSetId = metadata.getDefinitionSetId();
        final Object definitionSet = getDefinitionManager().definitionSets().getDefinitionSetById(definitionSetId);
        final Set<String> definitions = getDefinitionManager().adapters().forDefinitionSet().getDefinitions(definitionSet);
        if (null != definitions) {
            final Map<String, DefaultPaletteItem> items = new LinkedHashMap<>();
            final Set<String> consumed = new HashSet<>(definitions);
            // Once all item definitions consumed, build the resulting palette definition
            // and let the consumer do its job.
            final Command checkConsumedAndComplete = () -> {
                if (consumed.isEmpty()) {
                    paletteDefinitionConsumer.accept(new DefaultPaletteDefinition(new ArrayList<>(items.values()),
                                                                                  definitionSetId));
                }
            };
            for (final String defId : definitions) {
                consumed.remove(defId);
                // Check if this concrete definition is excluded from the palette model.
                if (itemFilter.test(defId)) {
                    clientFactoryServices
                            .newDefinition(defId,
                                           new ServiceCallback<Object>() {
                                               @Override
                                               public void onSuccess(final Object definition) {
                                                   buildItem(definition,
                                                             metadata,
                                                             items);
                                                   checkConsumedAndComplete.execute();
                                               }

                                               @Override
                                               public void onError(final ClientRuntimeError error) {
                                                   LOGGER.severe("Error while building the palette definition. " +
                                                                         "[Message=" + error.getMessage() + "]");
                                               }
                                           });
                } else {
                    checkConsumedAndComplete.execute();
                }
            }
        }
    }

    protected void buildItem(final Object definition,
                             final Metadata metadata,
                             final Map<String, DefaultPaletteItem> items) {
        final DefinitionAdapter<Object> definitionAdapter = getDefinitionManager().adapters().forDefinition();
        final String categoryId = definitionAdapter.getCategory(definition);
        // Check if this concrete category excluded from the palette model.
        if (categoryFilter.test(categoryId)) {
            final DefaultPaletteItem item = createItem(definition,
                                                       definitionAdapter,
                                                       metadata,
                                                       items::get);
            if (null != item) {
                items.put(item.getId(), item);
            }
        }
    }

    private void initDefaults() {
        this
                .itemFilter(id -> true)
                .categoryFilter(id -> true)
                .itemMessages(new DefaultItemMessageProvider(translationService));
    }

    protected DefinitionManager getDefinitionManager() {
        return definitionUtils.getDefinitionManager();
    }

    @SuppressWarnings("unchecked")
    private T cast() {
        return (T) this;
    }
}
