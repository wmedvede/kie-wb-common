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

package org.kie.workbench.common.forms.common.rendering.client.widgets.lsListBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;
import org.uberfire.ext.widgets.common.client.dropdown.EntryCreationLiveSearchService;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchCallback;

@Dependent
public class LiveSearchListBoxSearchService implements EntryCreationLiveSearchService<String, LiveSearchEntryCreationEditor> {

    private LiveSearchDataProviderProxy dataProviderProxy;

    private LiveSearchEntryCreationEditor editor;

    private FormRenderingContext context;

    private List<String> customEntries = new ArrayList<>();

    @Inject
    public LiveSearchListBoxSearchService(LiveSearchEntryCreationEditor editor,
                                          LiveSearchDataProviderProxy dataProviderProxy) {
        this.editor = editor;
        this.dataProviderProxy = dataProviderProxy;
        editor.setCustomEntryCommand(this::addCustomEntry);
    }

    public void init(String dataProvider,
                     FormRenderingContext context) {
        this.dataProviderProxy.setDataProvider(dataProvider);
        this.context = context;
    }

    @Override
    public LiveSearchEntryCreationEditor getEditor() {
        return editor;
    }

    @Override
    public void search(String pattern,
                       int maxResults,
                       LiveSearchCallback<String> callback) {
        final List<String> filteredCustomEntries;
        if (pattern == null || pattern.isEmpty()) {
            filteredCustomEntries = customEntries;
        } else {
            filteredCustomEntries = customEntries.stream()
                    .filter(entry -> entry.contains(pattern))
                    .collect(Collectors.toList());
        }
        dataProviderProxy.search(pattern,
                                 maxResults,
                                 context,
                                 searchResults -> {
                                     filteredCustomEntries.forEach(customEntry -> searchResults.add(customEntry,
                                                                                                    customEntry));
                                     callback.afterSearch(searchResults);
                                 });
    }

    @Override
    public void searchEntry(String key,
                            LiveSearchCallback<String> callback) {
        dataProviderProxy.searchEntry(key,
                                      context,
                                      searchResults -> {
                                          if (searchResults.isEmpty() && key != null) {
                                              if (!customEntries.contains(key)) {
                                                  addCustomEntry(key);
                                              }
                                              searchResults.add(key,
                                                                key);
                                          }
                                          callback.afterSearch(searchResults);
                                      });
    }

    private void addCustomEntry(String customEntry) {
        customEntries.add(customEntry);
    }
}
