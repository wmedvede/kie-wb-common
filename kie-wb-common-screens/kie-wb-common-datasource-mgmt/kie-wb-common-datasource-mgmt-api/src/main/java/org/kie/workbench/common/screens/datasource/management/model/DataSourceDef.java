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

public class DataSourceDef {

    String name;

    String jndi;

    String connectionURL;

    String driverClass;

    String dataSourceClass;

    String driverName;

    String user;

    String password;

    String poolName;

    boolean useJTA;

    boolean useCCM;

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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName( String driverName ) {
        this.driverName = driverName;
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
                "name='" + name + '\'' +
                ", jndi='" + jndi + '\'' +
                ", connectionURL='" + connectionURL + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", dataSourceClass='" + dataSourceClass + '\'' +
                ", driverName='" + driverName + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", useJTA=" + useJTA +
                ", useCCM=" + useCCM +
                '}';
    }
}
