package groovyx.net.http

import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovy.json.JsonBuilder
import org.junit.Rule
import groovy.util.slurpersupport.GPathResult
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.head
import static com.github.tomakehurst.wiremock.client.WireMock.delete
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static groovyx.net.http.ContentType.*

/**
 * @author tnichols
 *
 */
class RESTClientTest extends Specification {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8089))

    @Shared
    RESTClient restClient

    def setupSpec() {
        restClient = new RESTClient("http://localhost:8089")
    }

    @Unroll
    def "Test constructors: #scenario" () {
        expect: "Content type is correct"
        client.contentType == expectedContentType

        where:
        scenario                            | client                                            | expectedContentType
        "Call empty constructor"            | new RESTClient()                                  | ANY
        "Call constructor with custom type" | new RESTClient( 'http://www.google.com', XML )    | XML
    }

    def "HEAD results in error" () {
        given: "Mock"
        wireMockRule.stubFor(head(urlEqualTo("/fault"))
                .willReturn(aResponse().withStatus(404))
        )

        when: "Perform HEAD"
        restClient.head(path: "/fault")

        then: "Exception is thrown"
        HttpResponseException e = thrown HttpResponseException
        e.statusCode == 404
    }

    def "HEAD is successful" () {
        given: "Mock"
        wireMockRule.stubFor(head(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                )
        )

        when: "Perform HEAD"
        def response = restClient.head(path: "/success")

        then: "Request is successful"
        verifyAll {
            response.status == 200
            !response.data
        }
    }

    def "GET is successful" () {
        given: "Mock"
        wireMockRule.stubFor(get(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(new JsonBuilder(message: "Hello World").toString())
                )
        )

        when: "Perform GET"
        def response = restClient.get(path: "/success", contentType: JSON)

        then: "Request is successful"
        verifyAll {
            response.status == 200
            response.data instanceof Map
            response.data.message == "Hello World"
        }
    }

    def "DELETE is successful" () {
        given: "Mock"
        wireMockRule.stubFor(delete(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(204)
                )
        )

        when: "Perform GET"
        def response = restClient.delete(path: "/success")

        then: "Request is successful"
        response.status == 204
    }

    def "Test unknown named parameters" () {
        when: "Provide invalid value in GET request"
        restClient.get(invalid: 2)

        then: "Exception is thrown"
        thrown IllegalArgumentException
    }

    def "Send JSON POST request" () {
        given: "Mock"
        wireMockRule.stubFor(post(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody(new JsonBuilder(result: "success").toPrettyString()))
        )

        when: "Send a POST request"
        def response = restClient.post(path: "/success", contentType: JSON, body: [input: "value"])

        then: "Request is successful"
        verifyAll {
            response.status == 201
            response.data instanceof Map
            response.data.result == "success"
        }
    }

    def "Send XML POST request" () {
        given: "Mock"
        wireMockRule.stubFor(post(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("<person name='bob' title='builder'></person>")))

        when: "Perform POST"
        def postBody = {
            person( name: 'bob', title: 'builder' )
        }
        def response = restClient.post(path: "/success", contentType: XML, body: postBody)


        then: "Request is successful"
        verifyAll {
            response.status == 201
            response.data instanceof GPathResult
            response.data.name() == 'person'
            response.data.@name == 'bob'
            response.data.@title == 'builder'
        }
    }
}
