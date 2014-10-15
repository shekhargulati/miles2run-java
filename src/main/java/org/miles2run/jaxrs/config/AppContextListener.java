package org.miles2run.jaxrs.config;

import org.jug.JugFilterDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;

@WebListener
public class AppContextListener implements ServletContextListener {

    private Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("inside contextInitialized()");
        ServletContext ctx = sce.getServletContext();
        FilterRegistration.Dynamic filter = ctx.addFilter("JUGFilter", JugFilterDispatcher.class);
        filter.setInitParameter("javax.ws.rs.Application", RestConfig.class.getName());
        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        logger.info("Context Initialized....");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Context Destroyed....");
    }
}
