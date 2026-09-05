package org.example.bookapp;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import org.example.bookapp.config.*;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class BookApplication {

    public static void main(String[] args) throws LifecycleException {

        Tomcat tomcat = new Tomcat();

        tomcat.setPort(8080);
        tomcat.getConnector();

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();

        context.register(
                AppConfig.class,
                WebConfig.class,
                JpaConfig.class,
                SecurityConfig.class
        );

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        String webappDir = System.getProperty("java.io.tmpdir");

        Context tomcatContext = tomcat.addContext(
                "/bookapp",
                new File(webappDir).getAbsolutePath()
        );

        context.setServletContext(tomcatContext.getServletContext());
        context.refresh();


        RequestIdFilter requestIdFilter = new RequestIdFilter();

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("requestIdFilter");
        filterDef.setFilter(requestIdFilter);

        tomcatContext.addFilterDef(filterDef);

        FilterMap requestIdFilterMap = new FilterMap();
        requestIdFilterMap.setFilterName("requestIdFilter");
        requestIdFilterMap.addURLPattern("/*");

        tomcatContext.addFilterMap(requestIdFilterMap);


        DelegatingFilterProxy securityFilter =
                new DelegatingFilterProxy("springSecurityFilterChain", context);

        FilterDef securityFilterDef = new FilterDef();
        securityFilterDef.setFilterName("springSecurityFilterChain");
        securityFilterDef.setFilter(securityFilter);

        tomcatContext.addFilterDef(securityFilterDef);

        FilterMap securityFilterMap = new FilterMap();
        securityFilterMap.setFilterName("springSecurityFilterChain");
        securityFilterMap.addURLPattern("/*");

        tomcatContext.addFilterMap(securityFilterMap);


        Tomcat.addServlet(
                tomcatContext,
                "dispatcher",
                dispatcherServlet
        );

        tomcatContext.addServletMappingDecoded(
                "/",
                "dispatcher"
        );


        tomcat.start();

        tomcat.getServer().await();
    }

}