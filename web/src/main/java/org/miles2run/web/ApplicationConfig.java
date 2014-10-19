package org.miles2run.web;

import org.jug.filters.AfterLoginFilter;
import org.jug.filters.AuthenticationFilter;
import org.jug.filters.EnableSessionFilter;
import org.jug.filters.InjectPrincipalFilter;
import org.jug.view.NotFoundExceptionMapper;
import org.jug.view.ViewExceptionMapper;
import org.jug.view.ViewResourceNotFoundExceptionMapper;
import org.jug.view.ViewWriter;
import org.miles2run.views.filters.InjectProfileFilter;
import org.miles2run.views.views.*;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Make it auto detect classes http://stackoverflow.com/a/21430849/247038
 */
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(NotFoundExceptionMapper.class);
        classes.add(ViewWriter.class);
        classes.add(ViewExceptionMapper.class);
        classes.add(ViewResourceNotFoundExceptionMapper.class);
        classes.add(EnableSessionFilter.class);
        classes.add(AuthenticationFilter.class);
        classes.add(AfterLoginFilter.class);
        classes.add(IndexView.class);
        classes.add(TwitterSigninView.class);
        classes.add(TwitterCallbackView.class);
        classes.add(UserView.class);
        classes.add(HomeView.class);
        classes.add(FacebookSigninView.class);
        classes.add(FacebookCallbackView.class);
        classes.add(SigninView.class);
        classes.add(InjectProfileFilter.class);
        classes.add(LogoutView.class);
        classes.add(InjectPrincipalFilter.class);
        classes.add(GoogleSigninView.class);
        classes.add(GoogleCallbackView.class);
        classes.add(ActivityView.class);
        classes.add(GoalView.class);
        classes.add(CommunityRunView.class);
        classes.add(AboutView.class);
        classes.add(ContactView.class);
        return classes;
    }
}
