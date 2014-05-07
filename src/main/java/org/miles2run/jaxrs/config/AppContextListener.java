package org.miles2run.jaxrs.config;

import org.jug.JugFilterDispatcher;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 01/05/14.
 */
@WebListener
public class AppContextListener implements ServletContextListener{

    @Inject
    private Logger logger;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Context Initialized....");
        ServletContext ctx = sce.getServletContext();
        FilterRegistration.Dynamic filter = ctx.addFilter("JUGFilter", JugFilterDispatcher.class);
        filter.setInitParameter("javax.ws.rs.Application", RestConfig.class.getName());
        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Context Destroyed....");
    }
}
