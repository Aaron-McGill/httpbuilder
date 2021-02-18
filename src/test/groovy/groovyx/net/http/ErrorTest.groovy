import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.Rule
import org.junit.Test

class ErrorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule()

    @Test
    void firstTest() {
        assert true
    }

    @Test
    void wireMockFault() {
        wireMockRule.stubFor(post(urlEqualTo("/fault"))
            .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))
        )

        RESTClient restClient = new RESTClient("http://localhost:8080")
        def response = restClient.post(path: "/fault")

        assert true
    }

    @Test
    void wireMockSuccess() {
        wireMockRule.stubFor(get(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello World")
                )
        )

        RESTClient restClient = new RESTClient("http://localhost:8080")
        def response = restClient.get(path: "/success")

        assert response.status == 200
    }
}
