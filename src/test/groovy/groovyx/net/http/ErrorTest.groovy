package groovyx.net.http

import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.junit.WireMockRule
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import org.junit.Rule

class ErrorTest extends Specification {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8089))

    @Shared
    RESTClient restClient

    def setupSpec() {
        restClient = new RESTClient("http://localhost:8089")
    }

    def "Fault" () {
        given: "Mock"
        wireMockRule.stubFor(post(urlEqualTo("/fault"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))
        )

        when: "GET"
        restClient.post(path: "/fault")

        then: "Exception thrown"
        thrown Exception
    }

    def "Success" () {
        given: "Mock"
        wireMockRule.stubFor(get(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello World")
                )
        )

        when: "GET"
        def response = restClient.get(path: "/success")

        then: "Success"
        response.status == 200
    }
}
