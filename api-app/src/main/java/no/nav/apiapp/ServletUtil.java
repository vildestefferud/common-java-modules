package no.nav.apiapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.*;

import java.util.EnumSet;

import static org.springframework.web.context.support.WebApplicationContextUtils.findWebApplicationContext;

public class ServletUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletUtil.class);

    public static WebApplicationContext getContext(ServletContext servletContext) {
        return findWebApplicationContext(servletContext);
    }

    public static WebApplicationContext getContext(ServletContextEvent servletContextEvent) {
        return getContext(servletContextEvent.getServletContext());
    }

    public static AnnotationConfigWebApplicationContext getSpringContext(ServletContextEvent servletContextEvent) {
        return (AnnotationConfigWebApplicationContext) getContext(servletContextEvent.getServletContext());
    }

    public static ServletRegistration.Dynamic leggTilServlet(ServletContextEvent servletContextEvent, Class<? extends Servlet> servletClass, String... path) {
        return leggTilServlet(servletContextEvent.getServletContext(), servletClass, path);
    }

    public static ServletRegistration.Dynamic leggTilServlet(ServletContext servletContext, Class<? extends Servlet> servletClass, String... path) {
        ServletRegistration.Dynamic dynamic = servletContext.addServlet(servletClass.getName(), servletClass);
        dynamic.setLoadOnStartup(0); // provoke any errors early
        dynamic.addMapping(path);
        LOGGER.info("la til servlet [{}] på [{}]", servletClass.getName(), path);
        return dynamic;
    }

    public static ServletRegistration.Dynamic leggTilServlet(ServletContextEvent servletContextEvent, Servlet servlet, String path) {
        return leggTilServlet(servletContextEvent.getServletContext(), servlet, path);
    }

    public static ServletRegistration.Dynamic leggTilServlet(ServletContext servletContext, Servlet servlet, String path) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(servlet.getClass().getName(), servlet);
        servletRegistration.setLoadOnStartup(0); // provoke any errors early
        servletRegistration.addMapping(path);
        LOGGER.info("la til servlet [{}] på [{}]", servlet, path);
        return servletRegistration;
    }

    public static FilterRegistration.Dynamic leggTilFilter(ServletContextEvent servletContextEvent, Class<? extends Filter> filterClass) {
        return leggTilFilter(servletContextEvent.getServletContext(), filterClass);
    }

    public static FilterRegistration.Dynamic leggTilFilter(ServletContext servletContext, Class<? extends Filter> filterClass) {
        FilterRegistration.Dynamic dynamic = servletContext.addFilter(filterClass.getName(), filterClass);
        dynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
        LOGGER.info("la til filter [{}]", filterClass.getName());
        return dynamic;
    }

    public static FilterRegistration.Dynamic leggTilFilter(ServletContextEvent servletContextEvent, Filter filter) {
        return leggTilFilter(servletContextEvent.getServletContext(), filter);
    }

    public static FilterRegistration.Dynamic leggTilFilter(ServletContext servletContext, Filter filter) {
        Class<? extends Filter> filterClass = filter.getClass();
        FilterRegistration.Dynamic dynamic = servletContext.addFilter(filterClass.getName(), filter);
        dynamic.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
        LOGGER.info("la til filter [{}]", filterClass.getName());
        return dynamic;
    }
}
