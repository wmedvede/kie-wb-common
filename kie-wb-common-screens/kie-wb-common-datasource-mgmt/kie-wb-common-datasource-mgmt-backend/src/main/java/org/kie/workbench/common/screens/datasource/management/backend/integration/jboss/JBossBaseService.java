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

import java.io.Closeable;
import java.net.InetAddress;
import java.util.Date;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

public abstract class JBossBaseService {

    private static final Logger logger = LoggerFactory.getLogger( JBossBaseService.class );

    public ModelControllerClient createControllerClient( ) throws Exception {
        return createControllerClient( true );
    }

    public ModelControllerClient createControllerClient( boolean checkConnection ) throws Exception {

        ModelControllerClient client = ModelControllerClient.Factory.create( InetAddress.getByName( "127.0.0.1" ), 9999 );
        if ( checkConnection ) {
            try {
                //dummy operation to check if the connection was properly established, since the create operation
                //don't warranty the connection has been established.
                ModelNode op = new ModelNode();
                op.get( ClientConstants.OP ).set("read-resource");

                ModelNode returnVal = client.execute( new OperationBuilder( op ).build() );
                String releaseVersion = returnVal.get("result").get("release-version").asString();
                String releaseCodeName = returnVal.get("result").get("release-codename").asString();
            } catch ( Exception e ) {
                logger.error( "It was not possible to open connection to Wildfly/EAP server.", e );
                throw new Exception( "It was not possible to open connection to server. " + e.getMessage() );
            }
        }
        return client;
    }

    /**
     * Checks the outcome returned by server when an operation was executed.
     *
     * @param response ModelNode returned by server as response.
     *
     * @throws Exception
     */
    public void checkResponse( ModelNode response ) throws Exception {

        if ( "failed".equals( response.get( OUTCOME ) ) ) {
            throw new Exception( "operation execution failed. :" + getErrorDescription( response ) );
        } else if ( "canceled".equals( response.get( OUTCOME ) ) ) {
            throw new Exception( "operation excecution was canceled by server: " + getErrorDescription( response ) );
        } else if ( SUCCESS.equals( response.get( OUTCOME ) ) ) {
            //great!!!
        }
    }

    public boolean isFailure( ModelNode response ) {
        return "failed".equals( response.get( OUTCOME ) );
    }

    public void safeClose( final Closeable closeable ) {
        if ( closeable != null ) {
            try {
                System.out.println( " Antes close: " + new Date() );
                boolean disableClose = Boolean.valueOf( System.getProperty( "disableClose" ) );
                if ( disableClose ) {
                    System.out.println( " XXXXXXXX close disabled: " + new Date() );
                } else {
                    closeable.close();
                }
                System.out.println( " Despues close" );
            } catch ( Exception e ) {
                System.out.println(" error when closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getErrorDescription( ModelNode response ) {

        if ( response.hasDefined( FAILURE_DESCRIPTION ) ) {
            return response.get( FAILURE_DESCRIPTION ).asString();
        } else {
            return response.asString();
        }
    }
}
