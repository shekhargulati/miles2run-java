package org.miles2run.jaxrs.views;

import org.jug.view.View;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 * Created by shekhargulati on 09/05/14.
 */
@Path("/logout")
public class LogoutView {

    @Context
    private HttpServletRequest request;

    @GET
    @Produces("text/html")
    public View logout() {
        request.getSession().invalidate();
        return View.of("/", true);
    }
}
