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

package org.kie.workbench.common.forms.dynamic.client.config;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.kie.workbench.common.forms.dynamic.model.config.LiveSearchDataProvider;
import org.kie.workbench.common.forms.dynamic.service.shared.AbstractLiveSearchDataProviderManager;

@ApplicationScoped
public class ClientLiveSearchDataProviderManager extends AbstractLiveSearchDataProviderManager {

    public static final String PREFIX = "local";

    @PostConstruct
    public void init() {
        Collection<SyncBeanDef<LiveSearchDataProvider>> providers = IOC.getBeanManager().lookupBeans(LiveSearchDataProvider.class);

        for (SyncBeanDef<LiveSearchDataProvider> provider : providers) {
            registerProvider(provider.newInstance());
        }
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
