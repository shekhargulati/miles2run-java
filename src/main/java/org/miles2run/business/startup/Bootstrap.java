package org.miles2run.business.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.TimeZone;

/**
 * Created by shekhargulati on 19/08/14.
 */
@Singleton
@Startup
public class Bootstrap {

    private Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    @PostConstruct
    public void init() {
        String defaultTimezone = TimeZone.getDefault().getDisplayName();
        logger.info("Default timezone {} ", defaultTimezone);
    }
}
