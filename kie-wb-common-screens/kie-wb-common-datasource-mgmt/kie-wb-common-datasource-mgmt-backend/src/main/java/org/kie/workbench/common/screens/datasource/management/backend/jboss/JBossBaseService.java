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

package org.kie.workbench.common.screens.datasource.management.backend.jboss;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.Date;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.client.helpers.ClientConstants.*;

public class JBossBaseService {

    public ModelControllerClient createControllerClient() throws Exception {
        ModelControllerClient result = ModelControllerClient.Factory.create( InetAddress.getByName( "127.0.0.1" ), 9999 );
        return result;
    }

    public void checkResponse( ModelNode response ) throws Exception {

        //TODO improve all this error checking and handling.
        if ( "failed".equals( response.get( OUTCOME ) ) ) {
            throw new Exception( getErrorDescription( response ) );
        } else if ( "canceled".equals( response.get( OUTCOME ) ) ) {
            //to nothing
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
                System.out.println(" Antes close: " + new Date() );
                closeable.close();
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

    public void log( String message ) {
        System.out.println( message );
    }
}
