package no.nav.apiapp.config;

import no.nav.apiapp.ApiAppServletContextListener;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.selftest.impl.OpenAMHelsesjekk;
import no.nav.brukerdialog.security.jaspic.OidcAuthModule;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CProvider;
import no.nav.brukerdialog.security.oidc.provider.IssoOidcProvider;
import no.nav.brukerdialog.security.oidc.provider.OidcProvider;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.common.auth.LoginFilter;
import no.nav.common.auth.LoginProvider;
import no.nav.common.auth.openam.sbs.OpenAMLoginFilter;
import no.nav.common.auth.openam.sbs.OpenAmConfig;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;

import static no.nav.apiapp.ServletUtil.getContext;
import static no.nav.apiapp.ServletUtil.getSpringContext;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.*;

public class Konfigurator implements ApiAppConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Konfigurator.class);

    private final Jetty.JettyBuilder jettyBuilder;
    private final ApiApplication apiApplication;
    private final List<OidcProvider> oidcProviders = new ArrayList<>();
    private final List<LoginProvider> loginProviders = new ArrayList<>();
    private List<Object> springBonner = new ArrayList<>();
    private boolean issoLogin;

    public Konfigurator(Jetty.JettyBuilder jettyBuilder, ApiApplication apiApplication) {
        this.jettyBuilder = jettyBuilder;
        this.apiApplication = apiApplication;
    }

    @Override
    public ApiAppConfigurator sts() {
        return sts(defaultStsConfig());
    }

    StsConfig defaultStsConfig() {
        return StsConfig.builder()
                .url(getConfigProperty(StsSecurityConstants.STS_URL_KEY, "SECURITYTOKENSERVICE_URL"))
                .username(getConfigProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, getSystemUserUsernamePropertyName()))
                .password(getConfigProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, getSystemUserPasswordPropertyName()))
                .build();
    }

    @Override
    public ApiAppConfigurator sts(StsConfig stsConfig) {
        setProperty(StsSecurityConstants.STS_URL_KEY, stsConfig.url, PUBLIC);
        setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, stsConfig.username, PUBLIC);
        setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, stsConfig.password, SECRET);
        return this;
    }

    @Override
    public ApiAppConfigurator openAmLogin() {
        return openAmLogin(OpenAmConfig.fromSystemProperties());
    }

    private String getSystemUserUsernamePropertyName() {
        return "SRV" + getAppName() + "_USERNAME";
    }

    private String getSystemUserPasswordPropertyName() {
        return "SRV" + getAppName() + "_PASSWORD";
    }

    @Override
    public ApiAppConfigurator openAmLogin(OpenAmConfig openAmConfig) {
        loginProviders.add(new OpenAMLoginFilter(openAmConfig));
        springBonner.add(new OpenAMHelsesjekk(openAmConfig));
        return this;
    }

    @Override
    public ApiAppConfigurator issoLogin() {
        return issoLogin(IssoConfig.builder()
                .username(getConfigProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, getSystemUserUsernamePropertyName()))
                .password(getConfigProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, getSystemUserPasswordPropertyName()))
                .build());
    }

    @Override
    public ApiAppConfigurator issoLogin(IssoConfig issoConfig) {
        setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, issoConfig.username, PUBLIC);
        setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, issoConfig.password, SECRET);
        oidcProviders.add(new IssoOidcProvider());
        issoLogin = true;
        return this;
    }

    @Override
    public ApiAppConfigurator azureADB2CLogin() {
        return azureADB2CLogin(AzureADB2CConfig.readFromSystemProperties());
    }

    @Override
    public ApiAppConfigurator azureADB2CLogin(AzureADB2CConfig azureADB2CConfig) {
        oidcProviders.add(new AzureADB2CProvider(azureADB2CConfig));
        return this;
    }

    private String getConfigProperty(String primaryProperty, String secondaryProperty) {
        LOGGER.info("reading config-property {} / {}", primaryProperty, secondaryProperty);
        return getOptionalProperty(primaryProperty)
                .orElseGet(() -> getRequiredProperty(secondaryProperty));
    }

    private String getAppName() {
        return apiApplication.getApplicationName().toUpperCase();
    }

    public Jetty buildJetty() {
        if (!oidcProviders.isEmpty()) {
            loginProviders.add(new OidcAuthModule(oidcProviders));
        }
        if(!loginProviders.isEmpty()){
            jettyBuilder.addFilter(new LoginFilter(loginProviders, ApiAppServletContextListener.DEFAULT_PUBLIC_PATHS));
        }
        return jettyBuilder.buildJetty();
    }

    public boolean harIssoLogin() {
        return issoLogin;
    }

    public List<Object> getSpringBonner() {
        return springBonner;
    }
}
