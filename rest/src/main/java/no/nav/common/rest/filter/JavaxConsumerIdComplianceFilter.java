package no.nav.common.rest.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static no.nav.common.rest.filter.LogRequestFilter.NAV_CONSUMER_ID_HEADER_NAME;

@Deprecated
@Slf4j
public class JavaxConsumerIdComplianceFilter implements Filter {

    private final boolean enforceCompliance;

    public JavaxConsumerIdComplianceFilter(boolean enforceCompliance) {
        this.enforceCompliance = enforceCompliance;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        boolean isMissingConsumerId = getHeader(request, NAV_CONSUMER_ID_HEADER_NAME).isEmpty();

        if (isMissingConsumerId) {
            log.warn("Request is missing consumer id, enforcingCompliance={}", enforceCompliance);

            if (enforceCompliance) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                response.setStatus(400);
                response.getWriter().write(
                        "Bad request: Consumer id is missing from header: " + NAV_CONSUMER_ID_HEADER_NAME +
                                ". Make sure to set the header with the name of the requesting application."
                );
                response.getWriter().flush();
                return;
            }
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

    private Optional<String> getHeader(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName));
    }

}
