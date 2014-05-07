package org.miles2run.jaxrs.config;

import org.jug.filters.AfterLogin;
import org.jug.filters.AfterLoginFilter;
import org.jug.filters.AuthenticationFilter;
import org.jug.filters.LoggedIn;
import org.jug.view.NotFoundExceptionMapper;
import org.jug.view.ViewExceptionMapper;
import org.jug.view.ViewResourceNotFoundExceptionMapper;
import org.jug.view.ViewWriter;
import org.miles2run.jaxrs.views.IndexView;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RestConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(NotFoundExceptionMapper.class);
        classes.add(IndexView.class);
        classes.add(ViewWriter.class);
        classes.add(ViewExceptionMapper.class);
        classes.add(ViewResourceNotFoundExceptionMapper.class);
        classes.add(AuthenticationFilter.class);
        classes.add(LoggedIn.class);
        classes.add(AfterLogin.class);
        classes.add(AfterLoginFilter.class);
        return classes;
    }
}
