package pl.softwaremill.cdiweb.jaxrs;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.cdi.ControllerResolver;
import pl.softwaremill.cdiweb.controller.cdi.RequestType;
import pl.softwaremill.cdiweb.controller.ContextConstants;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Web;
import pl.softwaremill.cdiweb.controller.annotation.WebImpl;
import pl.softwaremill.cdiweb.exception.HttpErrorException;
import pl.softwaremill.cdiweb.servlet.CDIWebListener;
import pl.softwaremill.cdiweb.velocity.LayoutDirective;
import pl.softwaremill.cdiweb.velocity.TagHelper;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    public static final String CDIWEB_DEV_DIR = "CDIWEB_DEV_DIR";

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return resolveFile(req, "/static/"+path);
    }

    @POST
    @Path("/post/{controller}/{view}{sep:/?}{path:.*}")
    public void handlePost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                           @PathParam("controller") String controller, @PathParam("view") String view,
                           @PathParam("path") String extraPath,
                           MultivaluedMap<String, String> formValues) {

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.getController().doPostMagic(formValues.entrySet());

            controllerResolver.executeView(RequestType.POST, view, new CDIWebContext(req, resp,
                    controllerResolver.getController(), extraPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/json/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object handleJsonGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                @PathParam("controller") String controller,
                                @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {
        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            return controllerResolver.executeView(RequestType.JSON, view, new CDIWebContext(req, resp,
                    controllerResolver.getController(), extraPath));
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

    @GET
    @Path("/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.TEXT_HTML)
    public String handleGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                            @PathParam("controller") String controller,
                            @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {
        ControllerBean controllerBean = null;

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);
            controllerResolver.executeView(RequestType.GET, view, new CDIWebContext(req, resp,
                    controllerResolver.getController(), extraPath));
            controllerBean = controllerResolver.getController();
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }

        // get the method
        try {

            VelocityContext context = new VelocityContext();

            BeanManager beanManager = (BeanManager) req.getServletContext().getAttribute(CDIWebListener.BEAN_MANAGER);

            Set<Bean<?>> beans = beanManager.getBeans(Object.class, new WebImpl());

            System.out.println("Listing beans");
            for (Bean<?> bean : beans) {
                System.out.println("bean: " + bean);
                for (Annotation annotation : bean.getQualifiers()) {
                    System.out.println("annotation = " + annotation.annotationType());
                    if (annotation.annotationType().equals(Web.class)) {
                        System.out.println("Adding annotation " + annotation);
                        context.put(((Web) annotation).value(), D.inject(bean.getBeanClass(),
                                bean.getQualifiers().toArray(new Annotation[bean.getQualifiers().size()])));

                        break;
                    }
                }
            }

            for (Map.Entry<String, Object> param : controllerBean.getParams().entrySet()) {
                context.put(param.getKey(), param.getValue());
            }

            // put some context
            context.put("tag", new TagHelper(req.getContextPath()));
            context.put("pageTitle", controllerBean.getPageTitle());
            context.put(ContextConstants.CONTROLLER, controllerBean);
            context.put(ContextConstants.VIEW, view);

            controllerBean.clearParams();

            /* lets render a template */

            StringWriter w = new StringWriter();

            String template = resolveTemplate(req, controller, view);

            Velocity.evaluate(context, w, controller + "/" + view, template);

            String layout;
            while ((layout = (String) context.get(LayoutDirective.LAYOUT)) != null) {
                // clear the layout
                context.put(LayoutDirective.LAYOUT, null);

                w = new StringWriter();
                template = resolveTemplate(req, "layout", layout);
                Velocity.evaluate(context, w, controller + "/" + view, template);
            }

            System.out.println(" template : " + w);

            return w.toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

    }


    protected String resolveTemplate(HttpServletRequest req, String controller, String view) throws IOException {
        InputStream is = resolveFile(req, "/view/" + controller + "/" + view + ".vm");

        StringWriter templateSW = new StringWriter();

        int c;
        while ((c = is.read()) > 0) {
            templateSW.append((char) c);
        }

        return templateSW.toString();
    }

    protected InputStream resolveFile(HttpServletRequest req, String path) {
        InputStream is;

        if (System.getProperty(CDIWEB_DEV_DIR) != null) {
            // read from the disk

            String dir = System.getProperty(CDIWEB_DEV_DIR);

            try {
                is = new FileInputStream(dir + path);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            is = req.getServletContext().getResourceAsStream(path);
        }

        return is;
    }
}
