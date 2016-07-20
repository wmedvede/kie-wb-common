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

package org.kie.workbench.common.screens.datasource.management.backend.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.screens.datasource.management.backend.DataSourceManagementConfig;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceDefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManagerRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceProvider;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefRegistryEntry;
import org.kie.workbench.common.screens.datasource.management.backend.core.RegistrationMode;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceStatus;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@ApplicationScoped
public class DataSourceManagerRegistryImpl
        implements DataSourceManagerRegistry {

    private DataSourceManagementConfig dataSourceManagementConfig;

    private DriverDefRegistry driverDefRegistry;

    private DataSourceDefRegistry dataSourceDefRegistry;

    public DataSourceManagerRegistryImpl() {
    }

    @Inject
    public DataSourceManagerRegistryImpl( DriverDefRegistry driverDefRegistry,
            DataSourceDefRegistry dataSourceDefRegistry,
            DataSourceManagementConfig dataSourceManagementConfig ) {
        this.driverDefRegistry = driverDefRegistry;
        this.dataSourceDefRegistry = dataSourceDefRegistry;
        this.dataSourceManagementConfig = dataSourceManagementConfig;
    }

    @Override
    public synchronized void registerDriverDef( DriverDef driverDef ) throws Exception {
        registerDriverDef( driverDef, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void registerDriverDef( DriverDef driverDef, RegistrationMode registrationMode ) throws Exception {
        DriverDefRegistryEntry entry = driverDefRegistry.getDriverDefEntry( driverDef.getUuid() );
        if ( entry == null ) {
            driverDefRegistry.registerDriverDef( driverDef );
            return;
        } else if ( driverDef.equals( entry.getDriverDef() ) ) {
            return;
        }

        if ( RegistrationMode.SOFT.equals( registrationMode ) ) {
            Collection<DataSourceDef> runningDependants = filterDependants( entry, DataSourceStatus.RUNNING );
            if ( !runningDependants.isEmpty() ) {
                throw new Exception( "Dependent data sources on driver: " + driverDef + " are running. " +
                        "Driver can not be registered/re-registered with SOFT registration" );
            }
        }

        DataSourceProvider provider;
        Collection<DataSourceDef> dependants = entry.getDependants();
        for ( DataSourceDef dependant : dependants ) {
            provider = getProvider();
            provider.release( dependant );
            dataSourceDefRegistry.deRegisterDataSourceDef( dependant.getUuid() );
        }

        driverDefRegistry.deRegisterDriverDef( driverDef.getUuid() );
        DriverDefRegistryEntry newEntry = driverDefRegistry.registerDriverDef( driverDef );

        for ( DataSourceDef dependant : dependants ) {
            provider = getProvider();
            provider.initialize( dependant );
            dataSourceDefRegistry.registerDataSourceDef( dependant );
            newEntry.addDependant( dependant );
        }
    }

    @Override
    public synchronized void deRegisterDriverDef( String uuid ) throws Exception {
        deRegisterDriverDef( uuid, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void deRegisterDriverDef( String uuid, RegistrationMode registrationMode ) throws Exception {
        DriverDefRegistryEntry entry = driverDefRegistry.getDriverDefEntry( uuid );
        if ( entry == null ) {
            throw new Exception( "No driver has been registered with uuid: " + uuid );
        }

        if ( !entry.hasDependants() ) {
            driverDefRegistry.deRegisterDriverDef( uuid );
            return;
        }

        if ( RegistrationMode.SOFT.equals( registrationMode ) ) {
            Collection<DataSourceDef> runningDependants = filterDependants( entry, DataSourceStatus.RUNNING );
            if ( !runningDependants.isEmpty() ) {
                throw new Exception( "Dependant data sources on driver: " + uuid + " are running. " +
                        "Driver can not be de-registered with SOFT de-registration" );
            }
        }

        DataSourceProvider provider;
        for ( DataSourceDef dependant : entry.getDependants() ) {
            provider = getProvider( );
            provider.release( dependant );
        }
        dataSourceDefRegistry.deRegisterDataSourceDef( uuid );
        driverDefRegistry.deRegisterDriverDef( uuid );
    }

    @Override
    public synchronized DriverDef getDriverDef( String uuid ) {
        return driverDefRegistry.getDriverDef( uuid );
    }

    @Override
    public synchronized Collection<DataSourceDef> getDependants( String uuid ) {
        List<DataSourceDef> result = new ArrayList<>( );
        DriverDefRegistryEntry entry = driverDefRegistry.getDriverDefEntry( uuid );
        if ( entry != null ) {
            result.addAll( entry.getDependants() );
        }
        return result;
    }

    @Override
    public synchronized void registerDataSourceDef( DataSourceDef dataSourceDef ) throws Exception {
        registerDataSourceDef( dataSourceDef, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void registerDataSourceDef( DataSourceDef dataSourceDef, RegistrationMode registrationMode ) throws Exception {
        DataSourceDef currentDef = dataSourceDefRegistry.getDataSourceDef( dataSourceDef.getUuid() );
        if ( currentDef != null && currentDef.equals( dataSourceDef ) ) {
            return;
        }

        DriverDefRegistryEntry driverDefEntry = driverDefRegistry.getDriverDefEntry( dataSourceDef.getDriverUuid() );
        if ( driverDefEntry == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getDriverUuid() + " is not registered" );
        }

        DataSourceProvider provider = getProvider();
        if ( provider == null ) {
            throw new Exception( "DataSourceProvider was not found for data source: " + dataSourceDef );
        }

        if ( currentDef == null ) {
            provider.initialize( dataSourceDef );
            dataSourceDefRegistry.registerDataSourceDef( dataSourceDef );
            deReferFromDrivers( dataSourceDef.getUuid() );
            driverDefEntry.addDependant( dataSourceDef );
        } else {
            DataSourceStatus currentStatus = provider.getStatus( dataSourceDef.getUuid() );
            if ( RegistrationMode.SOFT.equals( registrationMode ) && DataSourceStatus.RUNNING.equals( currentStatus ) ) {
                throw new Exception( "Data source : " + dataSourceDef + " is currently running. Try a FORCED registration or release it." );
            } else {
                provider.release( dataSourceDef );
                provider.initialize( dataSourceDef );
                dataSourceDefRegistry.registerDataSourceDef( dataSourceDef );
                deReferFromDrivers( dataSourceDef.getUuid() );
                driverDefEntry.addDependant( dataSourceDef );
            }
        }
    }

    @Override
    public synchronized void deRegisterDataSourceDef( String uuid ) throws Exception {
        deRegisterDataSourceDef( uuid, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void deRegisterDataSourceDef( String uuid, RegistrationMode registrationMode ) throws Exception {
        DataSourceDef dataSourceDef = dataSourceDefRegistry.getDataSourceDef( uuid );
        if ( dataSourceDef ==  null ) {
            throw new Exception( "No data source has been registered with uuid: " + uuid );
        }

        DataSourceProvider provider = getProvider();
        if ( provider == null ) {
            throw new Exception( "DataSourceProvider was not found for data source: " + dataSourceDef );
        }

        DataSourceStatus status = provider.getStatus( uuid );
        if ( RegistrationMode.SOFT.equals( registrationMode ) && DataSourceStatus.RUNNING.equals( status ) ) {
            throw new Exception( "Running data source can not be de-registered with SOFT registration." );
        }

        provider.release( dataSourceDef );
        dataSourceDefRegistry.deRegisterDataSourceDef( dataSourceDef.getUuid() );
        deReferFromDrivers( dataSourceDef.getUuid() );
    }

    @Override
    public synchronized DataSourceDef getDataSourceDef( String uuid ) {
        return dataSourceDefRegistry.getDataSourceDef( uuid );
    }

    private DataSourceProvider getProvider( ) {
        return dataSourceManagementConfig.getDataSourceProvider();
    }

    private Collection<DataSourceDef> filterDependants( DriverDefRegistryEntry entry, DataSourceStatus status ) {
        List<DataSourceDef> result = new ArrayList<>(  );
        DataSourceProvider provider;
        DataSourceStatus currentStatus;

        if ( ( provider = getProvider() ) != null ) {
            for ( DataSourceDef dependant : entry.getDependants() ) {
                currentStatus = provider.getStatus( dependant.getUuid() );
                if ( status.equals( currentStatus ) ) {
                    result.add( dependant );
                }
            }
        }
        return result;
    }

    private void deReferFromDrivers( String dataSourceUuid ) {
        for ( DriverDefRegistryEntry entry : driverDefRegistry.findReferencedEntries( dataSourceUuid ) ) {
            entry.removeDependant( dataSourceUuid );
        }
    }
}