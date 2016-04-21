/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.screens.datasource.management.backend.integration.jboss;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import static org.jboss.as.controller.client.helpers.ClientConstants.*;

public class JBossDeploymentService extends JBossBaseService {

    public void deployContent( String deploymentName, String runtimeName, byte[] content, boolean enabled ) throws Exception {

        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            operation.get( OP ).set( ADD );
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );

            List<ModelNode> contentList = new ArrayList<ModelNode>();
            ModelNode contentNode = new ModelNode();
            contentNode.set( "bytes", content );
            contentList.add( contentNode );

            operation.get( "name" ).set( deploymentName );
            operation.get( "content" ).set( contentList );
            operation.get( "enabled" ).set( enabled );
            operation.get( "runtime-name" ).set( runtimeName );

            response = client.execute( operation );

        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

    public void enableDeployment( String deploymentName, boolean enabled ) throws Exception {

        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            if ( enabled ) {
                operation.get( OP ).set( DEPLOYMENT_DEPLOY_OPERATION );
            } else {
                operation.get( OP ).set( DEPLOYMENT_UNDEPLOY_OPERATION );
            }
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );

            response = client.execute( operation );
        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

    public void removeDeployment( String deploymentName ) throws Exception {
        ModelControllerClient client = null;
        ModelNode response = null;

        try {
            client = createControllerClient();

            ModelNode operation = new ModelNode( );
            operation.get( OP ).set( DEPLOYMENT_REMOVE_OPERATION );
            operation.get( OP_ADDR ).add( DEPLOYMENT, deploymentName );
            operation.get( "name" ).set( deploymentName );
            response = client.execute( operation );

        } finally {
            safeClose( client );
            checkResponse( response );
        }
    }

}
