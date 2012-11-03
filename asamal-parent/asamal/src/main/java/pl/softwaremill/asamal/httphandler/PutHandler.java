package pl.softwaremill.asamal.httphandler;

import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class PutHandler extends AbstractHttpHandler {

    @PUT
    @Path("/{controller}/{view}{sep:/?}{path:.*}")
    public Response handlePut(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                              @PathParam("controller") String controller,
                              @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {
        return executeView(controller, view, req, resp, extraPath, null, RequestType.PUT);
    }

}
