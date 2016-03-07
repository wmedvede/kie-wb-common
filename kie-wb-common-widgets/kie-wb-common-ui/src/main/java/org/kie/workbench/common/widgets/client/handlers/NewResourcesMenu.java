/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.widgets.client.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.Callback;
import org.guvnor.common.services.project.context.ProjectContext;
import org.guvnor.common.services.project.context.ProjectContextChangeEvent;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;

/**
 * A menu to create New Resources
 */
@ApplicationScoped
public class NewResourcesMenu {

    private SyncBeanManager iocBeanManager;
    private NewResourcePresenter newResourcePresenter;

    private final List<MenuItem> items = new ArrayList<MenuItem>();
    private final Map<NewResourceHandler, MenuItem> newResourceHandlers = new HashMap<NewResourceHandler, MenuItem>();

    public NewResourcesMenu() {
        //Zero argument constructor for CDI proxies
    }

    @Inject
    public NewResourcesMenu( final SyncBeanManager iocBeanManager,
                             final NewResourcePresenter newResourcePresenter ) {
        this.iocBeanManager = iocBeanManager;
        this.newResourcePresenter = newResourcePresenter;
    }
    private MenuItem projectMenuItem;

    @PostConstruct
    public void setup() {

        addNewResourceHandlers();

        sortMenuItemsByCaption();

        addProjectMenuItem();
    }

    private void addNewResourceHandlers() {
        final Collection<IOCBeanDef<NewResourceHandler>> handlerBeans = iocBeanManager.lookupBeans( NewResourceHandler.class );

        for ( final IOCBeanDef<NewResourceHandler> handlerBean : handlerBeans ) {
            addMenuItem( handlerBean.getInstance() );
        }
    }

    private void addMenuItem( final NewResourceHandler newResourceHandler ) {

        if ( newResourceHandler.canCreate( ) ) {

            final MenuItem menuItem = getMenuItem( newResourceHandler );

            newResourceHandlers.put( newResourceHandler,
                                     menuItem );

            if ( isProjectMenuItem( newResourceHandler ) ) {
                this.projectMenuItem = menuItem;
            } else {
                items.add( menuItem );
            }
        }
    }

    /*
    * We set the project menu item first if it is in.
     */
    private void addProjectMenuItem() {
        if ( projectMenuItem != null ) {
            items.add( 0,
                       projectMenuItem );
        }
    }

    private void sortMenuItemsByCaption() {
        Collections.sort( items,
                          new Comparator<MenuItem>() {
                              @Override
                              public int compare( final MenuItem o1,
                                                  final MenuItem o2 ) {
                                  return o1.getCaption().compareToIgnoreCase( o2.getCaption() );
                              }
                          } );
    }

    private MenuItem getMenuItem( final NewResourceHandler activeHandler ) {
        final String description = activeHandler.getDescription();
        return MenuFactory.newSimpleItem( description ).respondsWith( new Command() {
            @Override
            public void execute() {
                final Command command = activeHandler.getCommand( newResourcePresenter );
                command.execute();
            }
        } ).endMenu().build().getItems().get( 0 );
    }

    private boolean isProjectMenuItem( final NewResourceHandler activeHandler ) {
        return activeHandler.getClass().getName().contains( "NewProjectHandler" );
    }

    public List<MenuItem> getMenuItems() {
        return items;
    }

    public List<MenuItem> getMenuItemsWithoutProject() {
        if ( projectMenuItem != null && items.contains( projectMenuItem ) ) {
            return items.subList( 1,
                                  items.size() );
        } else {
            return items;
        }
    }

    public void onProjectContextChanged( @Observes final ProjectContextChangeEvent event ) {
        final ProjectContext context = new ProjectContext();
        context.setActiveOrganizationalUnit( event.getOrganizationalUnit() );
        context.setActiveRepository( event.getRepository() );
        context.setActiveProject( event.getProject() );
        context.setActivePackage( event.getPackage() );
        enableNewResourceHandlers( context );
    }

    private void enableNewResourceHandlers( final ProjectContext context ) {
        for ( Map.Entry<NewResourceHandler, MenuItem> entry : this.newResourceHandlers.entrySet() ) {
            final NewResourceHandler handler = entry.getKey();
            final MenuItem menuItem = entry.getValue();

            handler.acceptContext( context,
                                   new Callback<Boolean, Void>() {
                                       @Override
                                       public void onFailure( Void reason ) {
                                           // Nothing to do there right now.
                                       }

                                       @Override
                                       public void onSuccess( final Boolean result ) {
                                           if ( result != null ) {
                                               menuItem.setEnabled( result );
                                           }
                                       }
                                   } );

        }
    }

}
