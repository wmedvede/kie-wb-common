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

import org.kie.workbench.common.screens.datasource.management.backend.DataSourceManagementBootstrap;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceDefCache;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManagerRegistry;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceProviderOLD;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCache;
import org.kie.workbench.common.screens.datasource.management.backend.core.DriverDefCacheEntry;
import org.kie.workbench.common.screens.datasource.management.backend.core.RegistrationMode;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceStatus;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;

@ApplicationScoped
public class DataSourceManagerRegistryImpl
        implements DataSourceManagerRegistry {

    private DataSourceManagementBootstrap dataSourceManagementConfig;

    private DriverDefCache driverDefCache;

    private DataSourceDefCache dataSourceDefCache;

    public DataSourceManagerRegistryImpl() {
    }

    @Inject
    public DataSourceManagerRegistryImpl( DriverDefCache driverDefCache,
            DataSourceDefCache dataSourceDefCache,
            DataSourceManagementBootstrap dataSourceManagementConfig ) {
        this.driverDefCache = driverDefCache;
        this.dataSourceDefCache = dataSourceDefCache;
        this.dataSourceManagementConfig = dataSourceManagementConfig;
    }

    @Override
    public synchronized void registerDriverDef( DriverDef driverDef ) throws Exception {
        registerDriverDef( driverDef, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void registerDriverDef( DriverDef driverDef, RegistrationMode registrationMode ) throws Exception {
        DriverDefCacheEntry entry = driverDefCache.get( driverDef.getUuid() );
        if ( entry == null ) {
            driverDefCache.putDriverDef( driverDef );
            return;
        } else if ( driverDef.equals( entry.getDriverDef() ) ) {
            return;
        }

        if ( RegistrationMode.SOFT.equals( registrationMode ) ) {
            Collection<DataSourceDef> runningDependants = filterDependants( entry, DataSourceStatus.REFERENCED );
            if ( !runningDependants.isEmpty() ) {
                throw new Exception( "Dependent data sources on driver: " + driverDef + " are running. " +
                        "Driver can not be registered/re-registered with SOFT registration" );
            }
        }

        DataSourceProviderOLD provider;
        Collection<DataSourceDef> dependants = entry.getDependants();
        for ( DataSourceDef dependant : dependants ) {
            provider = getProvider();
            provider.release( dependant );
            dataSourceDefCache.remove( dependant.getUuid() );
        }

        driverDefCache.remove( driverDef.getUuid() );
        DriverDefCacheEntry newEntry = driverDefCache.putDriverDef( driverDef );

        for ( DataSourceDef dependant : dependants ) {
            provider = getProvider();
            provider.initialize( dependant );
            dataSourceDefCache.put( dependant );
            newEntry.addDependant( dependant );
        }
    }

    @Override
    public synchronized void deRegisterDriverDef( String uuid ) throws Exception {
        deRegisterDriverDef( uuid, RegistrationMode.SOFT );
    }

    @Override
    public synchronized void deRegisterDriverDef( String uuid, RegistrationMode registrationMode ) throws Exception {
        DriverDefCacheEntry entry = driverDefCache.get( uuid );
        if ( entry == null ) {
            throw new Exception( "No driver has been registered with uuid: " + uuid );
        }

        if ( !entry.hasDependants() ) {
            driverDefCache.remove( uuid );
            return;
        }

        if ( RegistrationMode.SOFT.equals( registrationMode ) ) {
            Collection<DataSourceDef> runningDependants = filterDependants( entry, DataSourceStatus.REFERENCED );
            if ( !runningDependants.isEmpty() ) {
                throw new Exception( "Dependant data sources on driver: " + uuid + " are running. " +
                        "Driver can not be de-registered with SOFT de-registration" );
            }
        }

        DataSourceProviderOLD provider;
        for ( DataSourceDef dependant : entry.getDependants() ) {
            provider = getProvider( );
            provider.release( dependant );
        }
        dataSourceDefCache.remove( uuid );
        driverDefCache.remove( uuid );
    }

    @Override
    public synchronized DriverDef getDriverDef( String uuid ) {
        return driverDefCache.getDriverDef( uuid );
    }

    @Override
    public synchronized Collection<DataSourceDef> getDependants( String uuid ) {
        List<DataSourceDef> result = new ArrayList<>( );
        DriverDefCacheEntry entry = driverDefCache.get( uuid );
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
        DataSourceDef currentDef = dataSourceDefCache.get( dataSourceDef.getUuid() );
        if ( currentDef != null && currentDef.equals( dataSourceDef ) ) {
            return;
        }

        DriverDefCacheEntry driverDefEntry = driverDefCache.get( dataSourceDef.getDriverUuid() );
        if ( driverDefEntry == null ) {
            throw new Exception( "Required driver: " + dataSourceDef.getDriverUuid() + " is not registered" );
        }

        DataSourceProviderOLD provider = getProvider();
        if ( provider == null ) {
            throw new Exception( "DataSourceProviderOLD was not found for data source: " + dataSourceDef );
        }

        if ( currentDef == null ) {
            provider.initialize( dataSourceDef );
            dataSourceDefCache.put( dataSourceDef );
            deReferFromDrivers( dataSourceDef.getUuid() );
            driverDefEntry.addDependant( dataSourceDef );
        } else {
            DataSourceStatus currentStatus = provider.getStatus( dataSourceDef.getUuid() );
            if ( RegistrationMode.SOFT.equals( registrationMode ) && DataSourceStatus.REFERENCED.equals( currentStatus ) ) {
                throw new Exception( "Data source : " + dataSourceDef + " is currently running. Try a FORCED registration or release it." );
            } else {
                provider.release( dataSourceDef );
                provider.initialize( dataSourceDef );
                dataSourceDefCache.put( dataSourceDef );
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
        DataSourceDef dataSourceDef = dataSourceDefCache.get( uuid );
        if ( dataSourceDef ==  null ) {
            throw new Exception( "No data source has been registered with uuid: " + uuid );
        }

        DataSourceProviderOLD provider = getProvider();
        if ( provider == null ) {
            throw new Exception( "DataSourceProviderOLD was not found for data source: " + dataSourceDef );
        }

        DataSourceStatus status = provider.getStatus( uuid );
        if ( RegistrationMode.SOFT.equals( registrationMode ) && DataSourceStatus.REFERENCED.equals( status ) ) {
            throw new Exception( "Running data source can not be de-registered with SOFT registration." );
        }

        provider.release( dataSourceDef );
        dataSourceDefCache.remove( dataSourceDef.getUuid() );
        deReferFromDrivers( dataSourceDef.getUuid() );
    }

    @Override
    public synchronized DataSourceDef getDataSourceDef( String uuid ) {
        return dataSourceDefCache.get( uuid );
    }

    private DataSourceProviderOLD getProvider( ) {
        return dataSourceManagementConfig.getDataSourceProviderOLD();
    }

    private Collection<DataSourceDef> filterDependants( DriverDefCacheEntry entry, DataSourceStatus status ) {
        List<DataSourceDef> result = new ArrayList<>(  );
        DataSourceProviderOLD provider;
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
        for ( DriverDefCacheEntry entry : driverDefCache.findReferencedEntries( dataSourceUuid ) ) {
            entry.removeDependant( dataSourceUuid );
        }
    }
}