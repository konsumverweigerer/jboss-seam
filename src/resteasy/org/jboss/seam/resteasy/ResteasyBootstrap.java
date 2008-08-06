package org.jboss.seam.resteasy;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.DeploymentStrategy;
import org.jboss.seam.annotations.*;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Reflections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Scans annoated JAX-RS resources and providers, optionally registers them as Seam components.
 *
 * @author Christian Bauer
 */
@Name("org.jboss.seam.resteasy.bootstrap")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
@Install(classDependencies = "org.resteasy.Dispatcher")
public class ResteasyBootstrap
{

    @Logger
    Log log;

    @In
    protected ApplicationConfig applicationConfig;

    @Create
    public void onStartup()
    {
        log.info("deploying Resteasy providers and resources");

        Collection<Class<java.lang.Object>> annotatedProviderClasses = null;
        Collection<Class<java.lang.Object>> annotatedResourceClasses = null;
        if (applicationConfig.isScanProviders() || applicationConfig.isScanResources())
        {
            log.debug("scanning all classes for JAX-RS annotations");

            DeploymentStrategy deployment = (DeploymentStrategy) Component.getInstance("deploymentStrategy");
            AnnotationDeploymentHandler handler =
                    (AnnotationDeploymentHandler) deployment.getDeploymentHandlers().get(AnnotationDeploymentHandler.NAME);

            annotatedProviderClasses = handler.getClasses().get(javax.ws.rs.ext.Provider.class.getName());
            annotatedResourceClasses = handler.getClasses().get(javax.ws.rs.Path.class.getName());
        }

        log.debug("finding all Seam component classes");
        Map<Class, Component> seamComponents = new HashMap<Class, Component>();
        String[] applicationContextNames = Contexts.getApplicationContext().getNames();
        for (String applicationContextName : applicationContextNames)
        {
            if (applicationContextName.endsWith(".component"))
            {
                Component seamComponent =
                        (Component) Component.getInstance(applicationContextName, ScopeType.APPLICATION);
                // TODO: This should consider EJB components/annotations on interfaces somehow?
                seamComponents.put(seamComponent.getBeanClass(), seamComponent);
            }
        }

        registerProviders(seamComponents, annotatedProviderClasses);
        registerResources(seamComponents, annotatedResourceClasses);
    }

    // Load all provider classes, either scanned or through explicit configuration
    protected void registerProviders(Map<Class, Component> seamComponents, Collection annotatedProviderClasses)
    {
        Collection<Class> providerClasses = new HashSet<Class>();
        try
        {
            if (applicationConfig.isScanProviders() && annotatedProviderClasses != null)
                providerClasses.addAll(annotatedProviderClasses);

            for (String s : new HashSet<String>(applicationConfig.getProviderClassNames()))
                providerClasses.add(Reflections.classForName(s));

        }
        catch (ClassNotFoundException ex)
        {
            log.error("error loading JAX-RS provider class: " + ex.getMessage());
        }
        for (Class providerClass : providerClasses)
        {
            // Ignore built-in providers, we register them manually later
            if (providerClass.getName().startsWith("org.resteasy.plugins.providers")) continue;

            Component seamComponent = null;
            // Check if this is also a Seam component bean class
            if (seamComponents.containsKey(providerClass))
            {
                seamComponent = seamComponents.get(providerClass);
                // Needs to be APPLICATION or STATELESS
                if (!seamComponent.getScope().equals(ScopeType.APPLICATION) &&
                        !seamComponent.getScope().equals(ScopeType.STATELESS))
                {
                    log.warn("not registering as  provider Seam component, must be APPLICATION or STATELESS scoped: "
                            + seamComponent.getName());
                    seamComponent = null;
                }
            }
            if (seamComponent != null)
            {
                log.debug("registering provider Seam component: " + seamComponent.getName());
            }
            else
            {
                log.debug("registering provider class: " + providerClass.getName());
            }
            applicationConfig.addProviderClass(providerClass, seamComponent);
        }
        if (applicationConfig.getProviderClasses().size() == 0 &&
                !applicationConfig.isUseBuiltinProviders())
        {
            log.info("no RESTEasy provider classes registered");
        }
    }

    // Load all resource classes, either scanned or through explicit configuration
    protected void registerResources(Map<Class, Component> seamComponents, Collection annotatedResourceClasses)
    {
        Collection<Class> resourceClasses = new HashSet<Class>();
        try
        {
            if (applicationConfig.isScanResources() && annotatedResourceClasses != null)
                resourceClasses.addAll(annotatedResourceClasses);

            for (String s : new HashSet<String>(applicationConfig.getResourceClassNames()))
                resourceClasses.add(Reflections.classForName(s));

        }
        catch (ClassNotFoundException ex)
        {
            log.error("error loading JAX-RS resource class: " + ex.getMessage());
        }
        for (Class<Object> resourceClass : resourceClasses)
        {

            Component seamComponent = null;
            // Check if this is also a Seam component bean class
            if (seamComponents.containsKey(resourceClass))
            {
                seamComponent = seamComponents.get(resourceClass);
                log.debug("registering resource Seam component: " + seamComponent.getName());
            }
            else
            {
                log.debug("registering resource class with JAX-RS default lifecycle: " + resourceClass.getName());
            }
            applicationConfig.addResourceClass(resourceClass, seamComponent);
        }
        if (applicationConfig.getResourceClasses().size() == 0)
            log.info("no JAX-RS resource classes registered");
    }

}