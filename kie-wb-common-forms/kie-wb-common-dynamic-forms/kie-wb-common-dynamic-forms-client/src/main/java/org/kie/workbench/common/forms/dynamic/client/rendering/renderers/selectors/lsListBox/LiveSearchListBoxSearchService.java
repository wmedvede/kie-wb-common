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

package org.kie.workbench.common.forms.dynamic.client.rendering.renderers.selectors.lsListBox;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.uberfire.ext.widgets.common.client.dropdown.EntryCreationLiveSearchService;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchCallback;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchResults;

@Dependent
public class LiveSearchListBoxSearchService implements EntryCreationLiveSearchService<String, LiveSearchListBoxEntryCreationEditor> {

    private LiveSearchListBoxEntryCreationEditor editor;

    @Inject
    public LiveSearchListBoxSearchService(LiveSearchListBoxEntryCreationEditor editor) {
        this.editor = editor;
    }

    @Override
    public LiveSearchListBoxEntryCreationEditor getEditor() {
        return editor;
    }

    @Override
    public void search(String pattern,
                       int maxResults,
                       LiveSearchCallback<String> callback) {

        GWT.log("search with pattern: " + pattern + ", maxResults: " + maxResults);

        LiveSearchResults<String> results = new LiveSearchResults<>(maxResults);

        results.add("uno", "UNO");
        results.add("dos", "DOS");
        results.add("tres", "TRES");
        callback.afterSearch(results);

    }

    @Override
    public void searchEntry(String key,
                            LiveSearchCallback<String> callback) {
        GWT.log("searchEntry with key: " + key);
        LiveSearchResults<String> results = new LiveSearchResults<>();

        if ("uno".equals(key)) {
            results.add("uno",
                        "UNO");
        }
        if ("dos".equals(key)) {
            results.add("dos",
                        "DOS");
        }
        if ("tres".equals(key)) {
            results.add("tres",
                        "TRES");
        }
        callback.afterSearch(results);
    }
}
