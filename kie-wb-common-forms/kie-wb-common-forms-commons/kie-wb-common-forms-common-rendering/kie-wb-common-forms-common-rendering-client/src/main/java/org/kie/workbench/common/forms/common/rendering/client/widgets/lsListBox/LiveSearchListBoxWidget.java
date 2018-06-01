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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import org.kie.workbench.common.forms.dynamic.service.shared.FormRenderingContext;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchDropDown;
import org.uberfire.ext.widgets.common.client.dropdown.SingleLiveSearchSelectionHandler;

public class LiveSearchListBoxWidget
        extends Composite implements HasValue<String> {

    private LiveSearchDropDown<String> liveSearchDropDown;
    private LiveSearchListBoxSearchService searchService;
    private SingleLiveSearchSelectionHandler<String> selectionHandler = new SingleLiveSearchSelectionHandler<>();
    private String lastValue = null;

    @Inject
    public LiveSearchListBoxWidget(LiveSearchDropDown<String> liveSearchDropDown,
                                   LiveSearchListBoxSearchService searchService) {
        this.liveSearchDropDown = liveSearchDropDown;
        this.searchService = searchService;
    }

    @PostConstruct
    void init() {
        initWidget(liveSearchDropDown.asWidget());
        liveSearchDropDown.setEnabled(true);
        liveSearchDropDown.setSearchEnabled(true);
        liveSearchDropDown.init(searchService,
                                selectionHandler);
        liveSearchDropDown.setOnChange(this::onSelectionChange);
    }

    public void init(String dataProvider,
                     FormRenderingContext context) {
        searchService.init(dataProvider,
                           context);
    }

    @Override
    public String getValue() {
        return selectionHandler.getSelectedKey();
    }

    @Override
    public void setValue(String value) {
        setValue(value,
                 false);
    }

    @Override
    public void setValue(String value,
                         boolean fireEvents) {
        liveSearchDropDown.setSelectedItem(value);
        if (fireEvents) {
            notifyChange(lastValue,
                         value);
        }
        lastValue = value;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler,
                          ValueChangeEvent.getType());
    }

    public void setReadOnly(boolean readOnly) {
        this.liveSearchDropDown.setEnabled(!readOnly);
    }

    private void onSelectionChange() {
        String selectedValue = selectionHandler.getSelectedKey();
        notifyChange(lastValue,
                     selectedValue);
        lastValue = selectedValue;
    }

    protected void notifyChange(String oldValue,
                                String newValue) {
        ValueChangeEvent.fireIfNotEqual(this,
                                        oldValue,
                                        newValue);
    }
}
