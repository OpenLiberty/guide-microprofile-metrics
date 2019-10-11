// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::MetricsTest[]
package it.io.openliberty.guides.metrics;

import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetricsTest {
  private static String httpPort;
  private static String httpsPort;
  private static String baseHttpUrl;
  private static String baseHttpsUrl;

  private List<String> metrics;
  private Client client;

  private final String INVENTORY_HOSTS = "inventory/systems";
  private final String INVENTORY_HOSTNAME = "inventory/systems/localhost";
  private final String METRICS_APPLICATION = "metrics/application";

  // tag::BeforeClass[]
  @BeforeClass
  // end::BeforeClass[]
  // tag::oneTimeSetup[]
  public static void oneTimeSetup() {
    httpPort = System.getProperty("liberty.test.port");
    httpsPort = System.getProperty("liberty.https.port");
    baseHttpUrl = "http://localhost:" + httpPort + "/";
    baseHttpsUrl = "https://localhost:" + httpsPort + "/";
  }
  // end::oneTimeSetup[]

  // tag::Before[]
  @Before
  // end::Before[]
  public void setup() {
    client = ClientBuilder.newClient();
    // tag::JsrJsonpProvider[]
    client.register(JsrJsonpProvider.class);
    // end::JsrJsonpProvider[]
  }
  // tag::After[]
  @After
  // end::After[]

  // tag::teardown[]
  public void teardown() {
    client.close();
  }
  // end::teardown[]

  // tag::Test[]
  @Test
  // end::Test[]
  
  // tag::testSuite[]
  public void testSuite() {
    this.testPropertiesRequestTimeMetric();
    this.testInventoryAccessCountMetric();
    this.testInventorySizeGaugeMetric();
  }
  // end::testSuite[]

  // tag::testPropertiesRequestTimeMetricp[]
  public void testPropertiesRequestTimeMetric() {
    connectToEndpoint(baseHttpUrl + INVENTORY_HOSTNAME);
    metrics = getMetrics();
    for (String metric : metrics) {
      if (metric.startsWith(
          "application:inventory_properties_request_time_rate_per_second")) {
        float seconds = Float.parseFloat(metric.split(" ")[1]);
        assertTrue(4 > seconds);
      }
    }
  }
  // end::testPropertiesRequestTimeMetric[]

  // tag::testInventoryAccessCountMetric[]
  public void testInventoryAccessCountMetric() {
    connectToEndpoint(baseHttpUrl + INVENTORY_HOSTS);
    metrics = getMetrics();
    for (String metric : metrics) {
      if (metric.startsWith("application:inventory_access_count")) {
        assertTrue(
            1 == Character.getNumericValue(metric.charAt(metric.length() - 1)));
      }
    }
  }
  // end::testInventoryAccessCountMetric[]

  // tag::testInventorySizeGaugeMetric[]
  public void testInventorySizeGaugeMetric() {
    metrics = getMetrics();
    for (String metric : metrics) {
      if (metric.startsWith("application:inventory_size_guage")) {
        assertTrue(
            1 == Character.getNumericValue(metric.charAt(metric.length() - 1)));
      }
    }

  }
  // end::testInventorySizeGaugeMetric[]

  public void connectToEndpoint(String url) {
    Response response = this.getResponse(url);
    this.assertResponse(url, response);
    response.close();
  }

  private List<String> getMetrics() {
    String usernameAndPassword = "admin" + ":" + "adminpwd";
    String authorizationHeaderValue = "Basic "
        + java.util.Base64.getEncoder()
                          .encodeToString(usernameAndPassword.getBytes());
    Response metricsResponse = client.target(baseHttpsUrl + METRICS_APPLICATION)
                                     .request(MediaType.TEXT_PLAIN)
                                     .header("Authorization",
                                         authorizationHeaderValue)
                                     .get();

    BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) 
    metricsResponse.getEntity()));
    List<String> result = new ArrayList<String>();
    try {
      String input;
      while ((input = br.readLine()) != null) {
        result.add(input);
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    metricsResponse.close();
    return result;
  }

  private Response getResponse(String url) {
    return client.target(url).request().get();
  }

  private void assertResponse(String url, Response response) {
    assertEquals("Incorrect response code from " + url, 200, response.getStatus());
  }
}
// end::MetricsTest[]
