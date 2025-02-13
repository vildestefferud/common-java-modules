package no.nav.common.cxf.jetty;

import lombok.SneakyThrows;
import no.nav.common.cxf.CXFEndpoint;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.ServerSocket;
import java.net.URI;

import static java.lang.String.format;
import static java.lang.System.setProperty;
import static org.apache.cxf.staxutils.StaxUtils.ALLOW_INSECURE_PARSER;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class JettyTestServer {

    private static final Logger LOG = getLogger(JettyTestServer.class);

    private static final String SERVICE_PATH = "/test";
    private static final String CONTEXT_PATH = "/" + JettyTestServer.class.getSimpleName();

    private Jetty jetty;

    protected <T> String startCxfServer(Class<T> serviceClass) {
        return startCxfServer(serviceClass, mock(serviceClass));
    }

    @SneakyThrows
    protected <T> String startCxfServer(Class<T> serviceClass, T service) {
        // Hvis andre tester har opprettet en bus allerede, må denne stoppes først
        BusFactory.getThreadDefaultBus().shutdown(false);

        setProperty(ALLOW_INSECURE_PARSER, Boolean.TRUE.toString());

        int port = findFreePort();
        jetty = Jetty.usingWar()
                .at(CONTEXT_PATH)
                .port(port)
                .disableAnnotationScanning()
                .buildJetty();
        jetty.context.addServlet(new ServletHolder(new CxfServlet(serviceClass, service)), "/*");
        jetty.start();

        String uri = URI.create(format("http://localhost:%d", port) + CONTEXT_PATH + SERVICE_PATH).toString();

        LOG.info("{} is running at {}", serviceClass, uri);
        return uri;
    }

    @SneakyThrows
    public static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    @After
    public void cleanup() throws Exception {
        if (jetty != null) {
            jetty.server.stop();
        }
    }

    private static class CxfServlet extends CXFNonSpringServlet {

        private final Class<?> serviceClass;
        private final Object service;

        private <T> CxfServlet(Class<T> serviceClass, T service) {
            this.serviceClass = serviceClass;
            this.service = service;
        }

        @Override
        public void init(ServletConfig sc) throws ServletException {
            super.init(sc);
            new CXFEndpoint()
                    .serviceBean(service)
                    .disableSAMLIn()
                    .address(SERVICE_PATH)
                    .create();
        }

    }

}
