//package com.polarbookshop.catalogservice.config;
//
//import com.github.tomakehurst.wiremock.WireMockServer;
//import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest(properties = {
//    "spring.cloud.config.enabled=true",
//    "spring.config.import=optional:configserver:"
//})
//public class CatalogServiceIntegrationTest {
//
//  private static WireMockServer wireMockServer;
//
//  @Autowired
//  private Environment environment; // For accessing application properties
//
//  @BeforeAll
//  static void startWireMockServer() {
//    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8889));
//    wireMockServer.start();
//
//    // Stub the configuration response from the mocked Config Server
//    // Add more debug logging to see what requests are being made
//    wireMockServer.addMockServiceRequestListener((request, response) -> {
//        System.out.println("[DEBUG_LOG] WireMock received request: " + request.getMethod() + " " + request.getUrl());
//    });
//
//    // Stub for the default Spring Cloud Config pattern
//    wireMockServer.stubFor(get(urlEqualTo("/catalog-service/default"))
//            .willReturn(okJson("""
//                        {
//                          "name": "catalog-service",
//                          "propertySources": [{
//                            "name": "mockConfig",
//                            "source": {
//                              "polar.greeting": "Hello from mocked config"
//                            }
//                          }]
//                        }
//                        """)));
//
//    // Stub for alternative Spring Cloud Config patterns
//    wireMockServer.stubFor(get(urlPathMatching("/catalog-service/.+"))
//            .willReturn(okJson("""
//                        {
//                          "name": "catalog-service",
//                          "propertySources": [{
//                            "name": "mockConfig",
//                            "source": {
//                              "polar.greeting": "Hello from mocked config"
//                            }
//                          }]
//                        }
//                        """)));
//  }
//
//  @AfterAll
//  static void stopWireMockServer() {
//    if (wireMockServer != null) {
//      wireMockServer.stop();
//    }
//  }
//
//  // Dynamically set the Spring Cloud Config URL to the WireMock server
//  @DynamicPropertySource
//  static void configureProperties(DynamicPropertyRegistry registry) {
//    registry.add("spring.cloud.config.uri", () -> "http://localhost:8889");
//  }
//
//  @Test
//  void shouldLoadConfigFromWireMock() {
//    // Add debug logging
//    System.out.println("[DEBUG_LOG] WireMock server is running: " + wireMockServer.isRunning());
//    System.out.println("[DEBUG_LOG] WireMock server port: " + wireMockServer.port());
//    System.out.println("[DEBUG_LOG] Config URI: " + environment.getProperty("spring.cloud.config.uri"));
//
//    // Assert that the mocked `polar.greeting` value is loaded into the application context
//    String polarGreeting = environment.getProperty("polar.greeting");
//    System.out.println("[DEBUG_LOG] Polar greeting: " + polarGreeting);
//
//    assertEquals("Hello from mocked config", polarGreeting, "Configuration should come from WireMock");
//  }
//}
