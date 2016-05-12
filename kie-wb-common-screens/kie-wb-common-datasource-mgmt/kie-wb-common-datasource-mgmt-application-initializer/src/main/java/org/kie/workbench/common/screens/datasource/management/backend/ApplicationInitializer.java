/*
 * Copyright 2016 JBoss Inc
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

package org.kie.workbench.common.screens.datasource.management.backend;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceDefSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ApplicationInitializer
        implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger( ApplicationInitializer.class );

    @Inject
    DataSourceManagementService dataSourceManagementService;

    EntityManagerFactory emf;

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        if ( Boolean.parseBoolean( System.getProperty( "initializeApplication" ) ) ) {
            initializeApplication( sce );
        }
    }

    public void initializeApplication( ServletContextEvent sce ) {

        final ServletContext servletContext = sce.getServletContext();

        DirectoryStream<Path> configFilesStream = null;
        final List<Path> datasourcePaths = new ArrayList<Path>(  );
        final List<Path> driverPaths = new ArrayList<Path>(  );

        Path rootPath = null;
        Path persistenceFilePath = null;

        try {
            logger.info( "Starting application: " + servletContext.getServletContextName() + " initialization." );

            rootPath = Paths.get( servletContext.getRealPath( "/WEB-INF/classes/META-INF" ) );
            String confStrPath = servletContext.getRealPath( "/WEB-INF/classes/META-INF/conf" );
            Path confPath = Paths.get( confStrPath );
            Path currentPath;

            configFilesStream = Files.newDirectoryStream( confPath );
            Iterator<Path> it = configFilesStream.iterator();
            while ( it.hasNext( ) ) {
                currentPath = it.next();
                if ( isDataSourceFile( currentPath ) ) {
                    datasourcePaths.add( currentPath );
                    logger.debug( "DataSource configuration file was found: " + currentPath.getFileName() );
                } else if ( isDriverFile( currentPath ) ) {
                    driverPaths.add( currentPath );
                    logger.debug( "Driver configuration file was found: " + currentPath.getFileName() );
                } else if ( isPersistenceFile( currentPath ) ) {
                    persistenceFilePath = currentPath;
                    logger.debug( "Persistence configuration file was found: " + currentPath.getFileName() );
                }
            }
        } catch ( IOException e ) {
            logger.error( "Configuration files detection failed. Application may not start properly.", e );
        } finally {
            try {
                if ( configFilesStream != null ) {
                    configFilesStream.close();
                }
            } catch ( Exception e ) {
                //nothing to do here.
            }
        }

        initializeDataSources( datasourcePaths );

        initializePersistence( persistenceFilePath, rootPath );

        logger.info( "Application: " + servletContext.getServletContextName() + " initialization finished." );
    }


    private void initializeDataSources( List<Path> datasourcePaths ) {
        for ( Path path : datasourcePaths ) {
            initializeDataSource( path );
        }
    }

    private void createPersistenceFile( Path persistenceFilePath, Path targetDir ) throws IOException {
        Path targetFile = targetDir.resolve( "persistence.xml" );
        Files.copy( persistenceFilePath, targetFile, StandardCopyOption.REPLACE_EXISTING );
    }

    private void deletePersistenceFile( Path rootPath ) throws IOException {
        Path persistencePath = rootPath.resolve( "persistence.xml" );
        if ( Files.exists( persistencePath ) ) {
            Files.delete( persistencePath );
        }
    }

    private void initializePersistence( Path persistenceFilePath, Path rootPath ) {
        logger.debug( "Initializing application persistence." );
        if ( persistenceFilePath != null ) {
            try {
                createPersistenceFile( persistenceFilePath, rootPath );
            } catch ( Exception e ) {
                logger.error( "It was not possible to create persistence file. Application may not start properly.", e );
                return;

            }
            initializeEntityManagerFactory( );
            try {
                deletePersistenceFile( rootPath );
            } catch ( Exception e ) {
                logger.warn( "It was not possible to remove the created persistence file." );
            }
        } else {
            logger.error( "Persistence configuration file was not found. Peristence won't be initialized." );
        }
    }

    private EntityManagerFactory initializeEntityManagerFactory( ) {
        logger.debug( "Starting EnityManagerFactory initialization" );

        try {
            emf = Persistence.createEntityManagerFactory( "LiveSparkPersistence", new Properties() );
            logger.debug( "EntityManagerFactory was successfully initialized. " );
        } catch ( Exception e ) {
            logger.error( "An error was produced during EntityManagerFactory initialization." +
                    " Application won't work properly.", e );
        }
        return emf;
    }

    private void initializeDataSource( Path path ) {
        logger.debug( "Starting DataSource initialization for: " + path.getFileName() );
        FileReader reader = null;
        DataSourceDef dataSourceDef;
        boolean isBound = false;

        try {
            reader = new FileReader( path.toFile() );
            dataSourceDef = DataSourceDefSerializer.deserialize( reader );
        } catch ( Exception e ) {
            logger.error( "An error was produced during DataSource parsing.", e );
            return;
        } finally {
            safeClose( reader );
        }

        if ( dataSourceDef == null ) {
            logger.warn( "No DataSource definition was found in file: " + path );
            return;
        }

        try {
            isBound = isBound( dataSourceDef.getJndi() );
        } catch ( Exception e ) {
            logger.error( "It was not possible to establish if DataSource: " + dataSourceDef.getName() +
            " is already bound as: " + dataSourceDef.getJndi() );
        }

        if ( !isBound ) {
            try {
                dataSourceManagementService.deploy( dataSourceDef );
                logger.debug( "DataSource: " + dataSourceDef.getName() + " was properly initialized." );
            } catch ( Exception e ) {
                logger.error( "An error was produced during DataSource: " + dataSourceDef.getName() + " initialization", e );
            }
        } else {
            logger.warn( "An object was already bounded as: " + dataSourceDef.getJndi() +
                    " DataSource: " + dataSourceDef.getName() + " won't be created." );
        }

    }

    private void initializeDriver( Path path ) {
        logger.debug( "Starting Driver for: " + path.getFileName() );
    }

    public void contextDestroyed( ServletContextEvent sce ) {
        logger.debug( "Destroying EntityManagerFactory." );
        if ( emf != null && emf.isOpen() ) {
            try {
                emf.close();
                logger.debug( "EntityManagerFactory was properly destroyed." );
            } catch ( Exception e ) {
                logger.error( "An error was produced during EntitiyManagerFactory destruction. ", e );

            }
        } else {
            logger.error( "EntityManagerFactory was already destroyed or wasn't created." );
        }
    }

    private boolean isDataSourceFile( final Path path ) {
        return path != null && path.getFileName().toString().endsWith( ".datasource" );
    }

    private boolean isPersistenceFile( final Path path ) {
        return path != null && path.getFileName().toString().equals( "persistence.xml" );
    }

    private boolean isDriverFile( final Path path ) {
        return path != null && path.getFileName().toString().equals( ".driver" );
    }

    private void safeClose( Closeable closeable ) {
        if ( closeable != null ) {
            try {
                closeable.close();
            } catch ( Exception e ) {
                logger.warn( "An error was produced during close operation on: " + closeable );
            }
        }
    }

    private boolean isBound( String jndi ) throws Exception {
        if ( jndi == null ) return false;
        InitialContext initialContext = new InitialContext(  );
        try {
            return initialContext.lookup( jndi ) != null;
        } catch ( NameNotFoundException e ) {
            return false;
        }
    }
}
