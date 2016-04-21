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

public class JBossDriverDef {

    /**
     *  "driver-name"
     */
    String driverName;

    /**
     *  "deployment-name"
     */
    String deploymentName;

    /**
     * "driver-module-name"
     * In cases the driver was installed as an EAP module and not just copying it into the deployments directory.
     */
    String driverModuleName;

    /**
     * "module-slot"
     * In cases the driver was installed as an EAP module and a module slot is used.
     *
     */
    String moduleSlot;

    /**
     * "major-version"
     */
    int mayorVersion;

    /**
     * "minor-version"
     */
    int minorVersion;

    /**
     * "driver-class"
     */
    String driverClass;

    /**
     * "driver-datasource-class-name"
     */
    String dataSourceClass;

    /**
     * "driver-xa-datasource-class-name"
     */
    String xaDataSourceClass;

    /**
     * "jdbc-compliant"
     */
    boolean jdbcCompliant;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName( String driverName ) {
        this.driverName = driverName;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName( String deploymentName ) {
        this.deploymentName = deploymentName;
    }

    public String getDriverModuleName() {
        return driverModuleName;
    }

    public void setDriverModuleName( String driverModuleName ) {
        this.driverModuleName = driverModuleName;
    }

    public String getModuleSlot() {
        return moduleSlot;
    }

    public void setModuleSlot( String moduleSlot ) {
        this.moduleSlot = moduleSlot;
    }

    public int getMayorVersion() {
        return mayorVersion;
    }

    public void setMayorVersion( int mayorVersion ) {
        this.mayorVersion = mayorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion( int minorVersion ) {
        this.minorVersion = minorVersion;
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

    public String getXaDataSourceClass() {
        return xaDataSourceClass;
    }

    public void setXaDataSourceClass( String xaDataSourceClass ) {
        this.xaDataSourceClass = xaDataSourceClass;
    }

    public boolean isJdbcCompliant() {
        return jdbcCompliant;
    }

    public void setJdbcCompliant( boolean jdbcCompliant ) {
        this.jdbcCompliant = jdbcCompliant;
    }

    @Override
    public String toString() {
        return "JBossDriverDef{" +
                "driverName='" + driverName + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", driverModuleName='" + driverModuleName + '\'' +
                ", moduleSlot='" + moduleSlot + '\'' +
                ", mayorVersion=" + mayorVersion +
                ", minorVersion=" + minorVersion +
                ", driverClass='" + driverClass + '\'' +
                ", dataSourceClass='" + dataSourceClass + '\'' +
                ", xaDataSourceClass='" + xaDataSourceClass + '\'' +
                ", jdbcCompliant=" + jdbcCompliant +
                '}';
    }
}
