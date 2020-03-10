package no.nav.fo.apiapp.rest;

import no.nav.apiapp.ApiApp;
import no.nav.apiapp.ApiApplication;
import no.nav.fo.apiapp.JettyTestConfig;
import no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants;
import no.nav.sbl.dialogarena.common.abac.pep.service.AbacServiceConfig;
import no.nav.sbl.dialogarena.common.jetty.Jetty;
import no.nav.testconfig.ApiAppTest;
import org.eclipse.jetty.server.ServerConnector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import static no.nav.testconfig.util.Util.setProperty;

public class JettyTestUtils {

    public static final String APPLICATION_NAME = "api-app";
    public static final String DEFAULT_ENVIRONMENT = "q0";

    public static void setupContext() {
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder()
                .applicationName(APPLICATION_NAME)
                .environment(DEFAULT_ENVIRONMENT)
                .build()
        );
        setProperties();
    }

    public static void setupContext(JettyTestConfig testConfig) {
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder()
                .applicationName(APPLICATION_NAME)
                .environment(DEFAULT_ENVIRONMENT)
                .allowClientStorage(testConfig.isAllowClientStorage())
                .disablePragmaHeader(testConfig.isDisablePragmaHeader())
                .build()
        );
        setProperties();
    }

    private static void setProperties() {
//        String securityTokenService = FasitUtils.getBaseUrl("securityTokenService", FSS);
//        ServiceUser srvveilarbdemo = FasitUtils.getServiceUser("srvveilarbdemo", "veilarbdemo");
//
//        RestService abacEndpoint = FasitUtils.getRestServices("abac.pdp.endpoint").stream()
//                .filter(rs -> srvveilarbdemo.getEnvironment().equals(rs.getEnvironment()) && rs.getApplication() == null)
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("fant ikke abac.pdp.endpoint i Fasit"));
//
//        setProperty(StsSecurityConstants.STS_URL_KEY, securityTokenService);
//        setProperty(StsSecurityConstants.SYSTEMUSER_USERNAME, srvveilarbdemo.getUsername());
//        setProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD, srvveilarbdemo.getPassword());
//
//        ServiceUser azureADClientId = FasitUtils.getServiceUser("aad_b2c_clientid", "veilarbdemo");
//        setProperty(EXTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URL, FasitUtils.getBaseUrl("aad_b2c_discovery"));
//        setProperty(EXTERNAL_USERS_AZUREAD_B2C_EXPECTED_AUDIENCE, azureADClientId.username);
//
//        setProperty(CredentialConstants.SYSTEMUSER_USERNAME, srvveilarbdemo.getUsername());
//        setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, srvveilarbdemo.getPassword());
//        setProperty(AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME, abacEndpoint.getUrl());

        setProperty(AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME, "http://test.local");
        setProperty(CredentialConstants.SYSTEMUSER_USERNAME, "srvveilarbdemo");
        setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, "ikke-riktig-passord");
    }

    public static Jetty nyJettyForTest(Class<? extends ApiApplication> apiAppClass) {
        ApiApp apiApp = ApiApp.startApiApp(apiAppClass, new String[]{Integer.toString(tilfeldigPort()), Integer.toString(tilfeldigPort())});
        return apiApp.getJetty();
    }

    public static int tilfeldigPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getSslPort(Jetty jetty) {
        return ((ServerConnector) jetty.server.getConnectors()[1]).getPort();
    }
}
