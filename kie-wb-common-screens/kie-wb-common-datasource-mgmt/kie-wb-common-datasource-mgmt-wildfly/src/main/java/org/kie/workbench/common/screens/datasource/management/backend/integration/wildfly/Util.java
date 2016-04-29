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

package org.kie.workbench.common.screens.datasource.management.backend.integration.wildfly;

import java.util.UUID;

public class Util {

    public static String normalizeDriverName( final String driverName ) {
        //Names created by kie-wb typically starts with an 36 characters long java UUID in the form
        // 8d568f4b-723a-4708-b0d6-8d76a6b500f4

        if ( driverName == null || driverName.length() < 36 ) {
            return driverName;
        } else {
            String uuid = driverName.substring( 0, 36 );
            try {
                UUID.fromString( uuid );
                return uuid;
            } catch ( Exception e ) {
                //the prefix is not a uuid
                return driverName;
            }
        }
    }

}
