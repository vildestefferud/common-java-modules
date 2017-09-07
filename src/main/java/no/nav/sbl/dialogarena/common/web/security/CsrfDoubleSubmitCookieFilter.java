package no.nav.sbl.dialogarena.common.web.security;

import no.nav.sbl.dialogarena.common.web.selftest.SelfTestBaseServlet;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static java.util.Arrays.stream;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Laget etter https://www.owasp.org/index.php/Cross-Site_Request_Forgery_%28CSRF%29_Prevention_Cheat_Sheet#Double_Submit_Cookie
 **/
public class CsrfDoubleSubmitCookieFilter implements Filter {
    private static final Logger LOG = getLogger(SelfTestBaseServlet.class);
    private static final String CSRF_COOKIE_NAVN = "NAV_CSRF_PROTECTION";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if ("GET".equals(request.getMethod())) {
            if (request.getCookies() == null || stream(request.getCookies()).noneMatch(cookie -> cookie.getName().equals(CSRF_COOKIE_NAVN))) {
                response.addCookie(createCsrfProtectionCookie(request));
            }
        } else {
            if (!navCsrfCookieVerdi(request).equals(request.getHeader(CSRF_COOKIE_NAVN))) {
                LOG.error("Feil i CSRF-sjekk. Bruker du dette filteren må du i frontend sørge for å sende med NAV_CSRF_PROTECTION-cookien som en header med navn NAV_CSRF_PROTECTION og verdien til cookien");
                response.sendError(SC_UNAUTHORIZED, "Mangler NAV_CSRF_PROTECTION-cookie!! Du må inkludere cookien-verdien i en header med navn NAV_CSRF_PROTECTION");
            }
        }
        filterChain.doFilter(request, response);
    }

    private String navCsrfCookieVerdi(HttpServletRequest request) {
        return stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(CSRF_COOKIE_NAVN))
                .findFirst()
                .orElseThrow(this::manglerCsrfCookie)
                .getValue();
    }

    private RuntimeException manglerCsrfCookie() {
        return new RuntimeException("Mangler NAV_CSRF_PROTECTION-cookie. " +
                "Det betyr at brukeren ikke har gjort et GET-request til en applikasjon med dette filteret før POST/PUT/DELETE, " +
                "eller at brukeren har slettet tokenet fra browseren."
        );
    }

    private Cookie createCsrfProtectionCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(CSRF_COOKIE_NAVN, UUID.randomUUID().toString());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600 * 24 * 7);
        cookie.setDomain(request.getServerName());
        return cookie;
    }

    @Override
    public void destroy() {

    }
}
