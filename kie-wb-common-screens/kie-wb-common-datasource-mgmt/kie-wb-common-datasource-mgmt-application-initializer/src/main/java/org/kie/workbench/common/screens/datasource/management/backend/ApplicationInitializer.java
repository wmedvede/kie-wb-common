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

package org.kie.workbench.integration;

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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ApplicationInitializer
        implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger( ApplicationInitializer.class );

    EntityManagerFactory emf;

    public void contextInitialized( ServletContextEvent sce ) {

        final ServletContext servletContext = sce.getServletContext();

        DirectoryStream<Path> configFilesStream = null;
        final List<Path> datasourcePaths = new ArrayList<Path>(  );
        final List<Path> driverPaths = new ArrayList<Path>(  );

        Path rootPath = null;
        Path persistenceFilePath = null;

        try {
            logger.info( "Starting LiveSpark application: " + servletContext.getServletContextName() + " initialization." );

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

        if ( persistenceFilePath != null ) {
            try {
                createPersistenceFile( persistenceFilePath, rootPath );
            } catch ( Exception e ) {
                logger.error( "It was not possible to create persistence file. Application may not start properly.", e );
                return;

            }
            initializePersistence( datasourcePaths, driverPaths );
            try {
                deletePersistenceFile( rootPath );
            } catch ( Exception e ) {
                logger.warn( "It was not possible to remove the created persistence file." );
            }
        } else {
            logger.error( "Persistence configuration file was not found. Peristence won't be initialized." );
        }

        logger.info( "LiveSpark application: " + servletContext.getServletContextName() + " initialization finished." );
    }

    private void initializePersistence( List<Path> driverPaths,
            List<Path> datasourcePaths ) {

        for ( Path path : driverPaths ) {
            initializeDriver( path );
        }

        for ( Path path : datasourcePaths ) {
            initializeDataSource( path );
        }
        initializeEntityManagerFactory();
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

    public boolean isDataSourceFile( final Path path ) {
        return path != null && path.getFileName().toString().endsWith( ".datasource" );
    }

    public boolean isPersistenceFile( final Path path ) {
        return path != null && path.getFileName().toString().equals( "persistence.xml" );
    }

    public boolean isDriverFile( final Path path ) {
        return path != null && path.getFileName().toString().equals( ".driver" );
    }

    //manage to get the conf.xml file from the non standard location "META-INF/conf/conf.xml
    //conf providers like eclipselink also admits a property e.g. eclipselink.persistencexml to indicate
    //the location of the conf.xml file. But usually Persistence providers will use the getResources( )
    //method for looking at all available conf.xml files on the class path.
            /*
            Thread.currentThread().setContextClassLoader( new ClassLoader() {
                @Override
                public Enumeration<URL> getResources( String name ) throws IOException {
                    if ( name != null && name.equals( "META-INF/conf.xml" ) ) {
                        return Collections.enumeration( Arrays.asList( persistenceFilePath.toUri().toURL() ) );
                    } else {
                        return super.getResources( name );
                    }
                }

                @Override
                public URL getResource( String name ) {
                    if ( name != null && name.equals( "META-INF/conf.xml" ) ) {
                        try {
                            return persistenceFilePath.toUri().toURL();
                        } catch ( Exception e ) {
                            //shouldn't fail by construction.
                            return null;
                        }
                    } else {
                        return super.getResource( name );
                    }
                }
            } );
              */

}
