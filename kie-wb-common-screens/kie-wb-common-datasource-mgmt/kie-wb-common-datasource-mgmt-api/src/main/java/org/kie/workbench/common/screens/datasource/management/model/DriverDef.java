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

package org.kie.workbench.common.screens.datasource.management.model;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.uberfire.backend.vfs.Path;

@Portable
public class DriverDef {

    String uuid;

    String name;

    String driverClass;

    Path driverLib;

    public DriverDef() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid( String uuid ) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass( String driverClass ) {
        this.driverClass = driverClass;
    }

    public Path getDriverLib() {
        return driverLib;
    }

    public void setDriverLib( Path driverLib ) {
        this.driverLib = driverLib;
    }

    @Override
    public String toString() {
        return "DriverDef{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", driverLib=" + driverLib +
                '}';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        DriverDef driverDef = ( DriverDef ) o;

        if ( uuid != null ? !uuid.equals( driverDef.uuid ) : driverDef.uuid != null ) {
            return false;
        }
        if ( name != null ? !name.equals( driverDef.name ) : driverDef.name != null ) {
            return false;
        }
        if ( driverClass != null ? !driverClass.equals( driverDef.driverClass ) : driverDef.driverClass != null ) {
            return false;
        }
        return !( driverLib != null ? !driverLib.equals( driverDef.driverLib ) : driverDef.driverLib != null );

    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = ~~result;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( driverClass != null ? driverClass.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( driverLib != null ? driverLib.hashCode() : 0 );
        result = ~~result;
        return result;
    }
}
