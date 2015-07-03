/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client.handlers.advanceddomain;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.IOC;
import org.kie.workbench.common.screens.datamodeller.client.command.DataModelCommand;
import org.kie.workbench.common.screens.datamodeller.client.handlers.DomainHandler;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.AdvancedDomainEditor;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.DomainEditor;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.ResourceOptions;

@ApplicationScoped
public class AdvancedDomain implements DomainHandler {

    public AdvancedDomain() {
    }

    @Override
    public String getName() {
        return "ADVANCED";
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public DomainEditor getDomainEditor( boolean newInstance ) {
        //TODO, currently a new instance is always created. Likely the singleton option will be added when
        //we add the UF tool windows.
        AdvancedDomainEditor domainEditor = IOC.getBeanManager().lookupBean( AdvancedDomainEditor.class ).newInstance();
        domainEditor.setHandler( this );
        return domainEditor;
    }

    @Override
    public ResourceOptions getResourceOptions( boolean newInstance ) {
        //this domain has no special options at resource creation time.
        return null;
    }

    @Override
    public boolean validateCommand( DataModelCommand command ) {
        //cross domain validation not yet implemented
        return true;
    }

    @Override
    public void postCommandProcessing( DataModelCommand command ) {
        //no post command processing for this domain.
    }
}
