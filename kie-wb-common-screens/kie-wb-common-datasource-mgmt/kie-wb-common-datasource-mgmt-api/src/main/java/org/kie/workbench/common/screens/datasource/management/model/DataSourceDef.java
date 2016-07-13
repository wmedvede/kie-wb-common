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

@Portable
public class DataSourceDef {

    DataSourceDefType type;

    String uuid;

    String name;

    String jndi;

    String connectionURL;

    String driverClass;

    String dataSourceClass;

    String driverUuid;

    String user;

    String password;

    boolean useJTA;

    boolean useCCM;

    public DataSourceDef() {
    }

    public DataSourceDefType getType() {
        return type;
    }

    public void setType( DataSourceDefType type ) {
        this.type = type;
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

    public String getJndi() {
        return jndi;
    }

    public void setJndi( String jndi ) {
        this.jndi = jndi;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL( String connectionURL ) {
        this.connectionURL = connectionURL;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass( String driverClass ) {
        this.driverClass = driverClass;
    }

    public String getDataSourceClass() {
        return dataSourceClass;
    }

    public void setDataSourceClass( String dataSourceClass ) {
        this.dataSourceClass = dataSourceClass;
    }

    public String getDriverUuid() {
        return driverUuid;
    }

    public void setDriverUuid( String driverUuid ) {
        this.driverUuid = driverUuid;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public boolean isUseJTA() {
        return useJTA;
    }

    public void setUseJTA( boolean useJTA ) {
        this.useJTA = useJTA;
    }

    public boolean isUseCCM() {
        return useCCM;
    }

    public void setUseCCM( boolean useCCM ) {
        this.useCCM = useCCM;
    }

    @Override
    public String toString() {
        return "DataSourceDef{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", jndi='" + jndi + '\'' +
                ", connectionURL='" + connectionURL + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", dataSourceClass='" + dataSourceClass + '\'' +
                ", driverUuid='" + driverUuid + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", useJTA=" + useJTA +
                ", useCCM=" + useCCM +
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

        DataSourceDef that = ( DataSourceDef ) o;

        if ( useJTA != that.useJTA ) {
            return false;
        }
        if ( useCCM != that.useCCM ) {
            return false;
        }
        if ( uuid != null ? !uuid.equals( that.uuid ) : that.uuid != null ) {
            return false;
        }
        if ( name != null ? !name.equals( that.name ) : that.name != null ) {
            return false;
        }
        if ( jndi != null ? !jndi.equals( that.jndi ) : that.jndi != null ) {
            return false;
        }
        if ( connectionURL != null ? !connectionURL.equals( that.connectionURL ) : that.connectionURL != null ) {
            return false;
        }
        if ( driverClass != null ? !driverClass.equals( that.driverClass ) : that.driverClass != null ) {
            return false;
        }
        if ( dataSourceClass != null ? !dataSourceClass.equals( that.dataSourceClass ) : that.dataSourceClass != null ) {
            return false;
        }
        if ( driverUuid != null ? !driverUuid.equals( that.driverUuid ) : that.driverUuid != null ) {
            return false;
        }
        if ( user != null ? !user.equals( that.user ) : that.user != null ) {
            return false;
        }
        return !( password != null ? !password.equals( that.password ) : that.password != null );

    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = ~~result;
        result = 31 * result + ( name != null ? name.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( jndi != null ? jndi.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( connectionURL != null ? connectionURL.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( driverClass != null ? driverClass.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( dataSourceClass != null ? dataSourceClass.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( driverUuid != null ? driverUuid.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( user != null ? user.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( password != null ? password.hashCode() : 0 );
        result = ~~result;
        result = 31 * result + ( useJTA ? 1 : 0 );
        result = ~~result;
        result = 31 * result + ( useCCM ? 1 : 0 );
        result = ~~result;
        return result;
    }
}
