package no.nav.fo.apiapp.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import no.nav.fo.apiapp.JettyTest;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.ETAG;
import static no.nav.apiapp.rest.SwaggerResource.IKKE_BERIK;
import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerTest extends JettyTest {

    private ObjectMapper swaggerObjectMapper = Json.mapper()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Test
    public void getUI() {
        assertRedirect("/internal/swagger", "/api-app/internal/swagger/");
        assertRedirect("/internal/swagger/", "/api-app/internal/swagger/index.html");

        Response response = get("/internal/swagger/index.html");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("<title>Swagger UI</title>");
        assertThat(response.getHeaderString(ETAG)).isNotEmpty();
        assertThat(response.getHeaderString(CONTENT_TYPE)).isEqualTo("text/html;charset=utf-8");
    }

    @Test
    public void getSwaggerJson() throws Exception {
        sammenlign(getSwaggerConfig(uri("/api/swagger.json")), read("/SwaggerTest.json"));
    }

    @Test
    public void getSwaggerDefaultJson() throws Exception {
        sammenlign(getSwaggerConfig(buildUri("/api/swagger.json").queryParam(IKKE_BERIK, "true").build()), read("/SwaggerTest.default.json"));
    }

    private void assertRedirect(String path, String expectedRedirectPath) {
        Response redirectResponse = get(path);
        assertThat(redirectResponse.getLocation().getPath()).isEqualTo(expectedRedirectPath);
    }

    private Swagger read(String name) throws IOException {
        return swaggerObjectMapper.readValue(SwaggerTest.class.getResourceAsStream(name), Swagger.class);
    }

    private void sammenlign(Swagger swagger, Swagger forventet) throws JsonProcessingException {
        assertThat(swagger)
            .describedAs("\n\nfaktisk swagger.json:\n%s\n\nforventet swagger.json:\n%s\n\n",
                swaggerObjectMapper.writeValueAsString(swagger),
                swaggerObjectMapper.writeValueAsString(forventet)
            )
            .isEqualTo(forventet);
    }

    private Swagger getSwaggerConfig(URI uri) throws IOException {
        String content = get(uri).readEntity(String.class);
        return swaggerObjectMapper.readValue(content, Swagger.class);
    }

}
