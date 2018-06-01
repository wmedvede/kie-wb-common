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

import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.forms.dynamic.model.config.SelectorData;
import org.kie.workbench.common.forms.dynamic.service.shared.BackendLiveSearchDataProviderService;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;
import org.kie.workbench.common.forms.dynamic.service.shared.LiveSearchDataProviderManager;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchResults;

@Dependent
public class LiveSearchDataProviderProxy {

    private static final String LOCAL_PREFIX = "local";

    private LiveSearchDataProviderManager clientProviderManager;

    private Caller<BackendLiveSearchDataProviderService> backendProviderService;

    private String dataProvider;

    @Inject
    public LiveSearchDataProviderProxy(LiveSearchDataProviderManager clientProviderManager,
                                       Caller<BackendLiveSearchDataProviderService> backendProviderService) {
        this.clientProviderManager = clientProviderManager;
        this.backendProviderService = backendProviderService;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void search(String pattern,
                       int maxResults,
                       FormRenderingContext context,
                       Consumer<LiveSearchResults<String>> resultsConsumer) {
        if (dataProvider != null && !dataProvider.isEmpty()) {
            if (isLocal(dataProvider)) {
                SelectorData data = clientProviderManager.searchFromProvider(pattern,
                                                                             maxResults,
                                                                             context,
                                                                             dataProvider);
                resultsConsumer.accept(buildLiveSearchResults(data));
            } else {
                backendProviderService.call((SelectorData data) -> resultsConsumer.accept(buildLiveSearchResults(data))).searchFromProvider(pattern,
                                                                                                                                            maxResults,
                                                                                                                                            context,
                                                                                                                                            dataProvider);
            }
        } else {
            resultsConsumer.accept(new LiveSearchResults<>());
        }
    }

    public void searchEntry(String key,
                            FormRenderingContext context,
                            Consumer<LiveSearchResults> resultsConsumer) {
        if (dataProvider != null && !dataProvider.isEmpty()) {
            if (isLocal(dataProvider)) {
                SelectorData data = clientProviderManager.searchEntryFromProvider(key,
                                                                                  context,
                                                                                  dataProvider);
                resultsConsumer.accept(buildLiveSearchResults(data));
            } else {
                backendProviderService.call((SelectorData data) -> resultsConsumer.accept(buildLiveSearchResults(data))).searchEntryFromProvider(key,
                                                                                                                                                 context,
                                                                                                                                                 dataProvider);
            }
        } else {
            resultsConsumer.accept(new LiveSearchResults<>());
        }
    }

    private LiveSearchResults<String> buildLiveSearchResults(SelectorData selectorData) {
        LiveSearchResults<String> results = new LiveSearchResults<>();
        if (selectorData != null && selectorData.getValues() != null) {
            selectorData.getValues().forEach((key, value) -> results.add(key.toString(),
                                                                         value.toString()));
        }
        return results;
    }

    private boolean isLocal(String dataProvider) {
        return dataProvider.startsWith(LOCAL_PREFIX);
    }
}
