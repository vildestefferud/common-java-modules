package no.nav.common.abac;

import no.nav.common.abac.domain.AbacPersonId;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.abac.domain.request.XacmlRequest;
import no.nav.common.abac.domain.response.XacmlResponse;
import no.nav.common.abac.exception.PepException;
import no.nav.common.test.junit.SystemPropertiesRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static no.nav.common.abac.TestUtils.assertJsonEquals;
import static no.nav.common.abac.TestUtils.getContentFromJsonFile;
import static no.nav.common.utils.EnvironmentUtils.NAIS_APP_NAME_PROPERTY_NAME;
import static org.mockito.Mockito.*;

public class VeilarbPepTest {

    private final static AbacPersonId TEST_FNR = AbacPersonId.fnr("12345678900");

    private final static String TEST_SRV_USERNAME = "test";

    private final static String TEST_VEILEDER_IDENT = "Z1234";

    private final static String TEST_ENHET_ID = "1234";

    private static final String TEST_OIDC_TOKEN_BODY = "eyJpc3MiOiJuYXYubm8iLCJleHAiOjE0ODQ2NTI2NzIsImp0aSI6IkZHdXJVYWdleFRwTUVZTjdMRHlsQ1EiLCJpYXQiOjE0ODQ2NTIwNzIsIm5iZiI6MTQ4NDY1MTk1Miwic3ViIjoiYTExMTExMSJ9";

    private final static RequestInfo REQUEST_INFO = new RequestInfo("", "", "", "");

    @Rule
    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Before
    public void setup() {
        systemPropertiesRule.setProperty(NAIS_APP_NAME_PROPERTY_NAME, "testapp");
    }

    private AbacClient genericPermitClient = spy(new AbacClient() {
        @Override
        public String sendRawRequest(String xacmlRequestJson) {
            return getContentFromJsonFile("xacmlresponse-generic-permit.json");
        }

        @Override
        public XacmlResponse sendRequest(XacmlRequest xacmlRequest) {
            return XacmlMapper.mapRawResponse(sendRawRequest(XacmlMapper.mapRequestToEntity(xacmlRequest)));
        }
    });

    private AbacClient genericDenyClient = spy(new AbacClient() {
        @Override
        public String sendRawRequest(String xacmlRequestJson) {
            return getContentFromJsonFile("xacmlresponse-generic-deny.json");
        }

        @Override
        public XacmlResponse sendRequest(XacmlRequest xacmlRequest) {
            return XacmlMapper.mapRawResponse(sendRawRequest(XacmlMapper.mapRequestToEntity(xacmlRequest)));
        }
    });

    @Test
    public void sjekkVeilederTilgangTilEnhet__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkVeilederTilgangTilEnhet.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkVeilederTilgangTilEnhet(TEST_VEILEDER_IDENT, TEST_ENHET_ID, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkVeilederTilgangTilEnhet__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkVeilederTilgangTilEnhet(TEST_VEILEDER_IDENT, TEST_ENHET_ID, REQUEST_INFO);
    }


    @Test
    public void sjekkVeilederTilgangTilBruker__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkVeilederTilgangTilBruker.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkVeilederTilgangTilBruker(TEST_VEILEDER_IDENT, ActionId.READ, TEST_FNR, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkVeilederTilgangTilBruker__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkVeilederTilgangTilBruker(TEST_VEILEDER_IDENT, ActionId.READ, TEST_FNR, REQUEST_INFO);
    }


    @Test
    public void sjekkTilgangTilPerson__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkTilgangTilPerson.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkTilgangTilPerson(TEST_OIDC_TOKEN_BODY, ActionId.READ, TEST_FNR, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkTilgangTilPerson__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkTilgangTilPerson(TEST_OIDC_TOKEN_BODY, ActionId.READ, TEST_FNR, REQUEST_INFO);
    }


    @Test
    public void sjekkVeilederTilgangTilKode6__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkVeilederTilgangTilKode6.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkVeilederTilgangTilKode6(TEST_VEILEDER_IDENT, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkVeilederTilgangTilKode6__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkVeilederTilgangTilKode6(TEST_VEILEDER_IDENT, REQUEST_INFO);
    }

    @Test
    public void sjekkVeilederTilgangTilKode7__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkVeilederTilgangTilKode7.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkVeilederTilgangTilKode7(TEST_VEILEDER_IDENT, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkVeilederTilgangTilKode7__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkVeilederTilgangTilKode7(TEST_VEILEDER_IDENT, REQUEST_INFO);
    }

    @Test
    public void sjekkVeilederTilgangTilEgenAnsatt__skal_lage_riktig_request() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericPermitClient, mock(AuditLogger.class));
        String expectedRequest = getContentFromJsonFile("xacmlrequest-sjekkVeilederTilgangTilEgenAnsatt.json");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        veilarbPep.sjekkVeilederTilgangTilEgenAnsatt(TEST_VEILEDER_IDENT, REQUEST_INFO);

        verify(genericPermitClient, times(1)).sendRawRequest(captor.capture());
        assertJsonEquals(expectedRequest, captor.getValue());
    }

    @Test(expected = PepException.class)
    public void sjekkVeilederTilgangTilEgenAnsatt__skal_kaste_exception_hvis_ikke_tilgang() {
        VeilarbPep veilarbPep = new VeilarbPep(TEST_SRV_USERNAME, genericDenyClient, mock(AuditLogger.class));
        veilarbPep.sjekkVeilederTilgangTilEgenAnsatt(TEST_VEILEDER_IDENT, REQUEST_INFO);
    }

}
