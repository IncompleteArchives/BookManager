package org.example.bookapp;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import org.example.bookapp.config.AppConfig;
import org.example.bookapp.config.JpaConfig;
import org.example.bookapp.config.RequestIdFilter;
import org.example.bookapp.config.WebConfig;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
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
                JpaConfig.class
        );

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        RequestIdFilter requestIdFilter = new RequestIdFilter();

        String webappDir = System.getProperty("java.io.tmpdir");

        Context tomcatContext = tomcat.addContext(
                "/bookapp",
                new File(webappDir).getAbsolutePath()
        );

        Tomcat.addServlet(
                tomcatContext,
                "dispatcher",
                dispatcherServlet
        );

        tomcatContext.addServletMappingDecoded(
                "/",
                "dispatcher"
        );

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("requestIdFilter");
        filterDef.setFilter(requestIdFilter);

        tomcatContext.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("requestIdFilter");
        filterMap.addURLPattern("/*");

        tomcatContext.addFilterMap(filterMap);

        tomcat.start();

        tomcat.getServer().await();
    }

}