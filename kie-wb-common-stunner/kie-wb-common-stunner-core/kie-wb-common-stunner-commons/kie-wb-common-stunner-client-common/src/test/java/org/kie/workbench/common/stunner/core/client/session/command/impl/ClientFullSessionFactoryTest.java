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

package org.kie.workbench.common.stunner.core.client.session.command.impl;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSessionFactory;
import org.kie.workbench.common.stunner.core.client.session.impl.ClientFullSessionFactory;
import org.kie.workbench.common.stunner.core.client.session.impl.ClientFullSessionImpl;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class ClientFullSessionFactoryTest extends AbstractClientSessionFactoryTest {

    @Mock
    private ManagedInstance<ClientFullSessionImpl> fullSessionInstances;

    @Mock
    private ClientFullSessionImpl clientFullSession;

    @Override
    public void setUp() {
        super.setUp();
        when(fullSessionInstances.get()).thenReturn(clientFullSession);
        clientSession = clientFullSession;
    }

    @Override
    public AbstractClientSessionFactory createSessionFactory() {
        return new ClientFullSessionFactory(fullSessionInstances,
                                            stunnerPreferences,
                                            stunnerPreferencesRegistry);
    }
}
