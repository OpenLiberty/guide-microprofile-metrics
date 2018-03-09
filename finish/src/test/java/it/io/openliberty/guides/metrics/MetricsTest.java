// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
  private static String port;
  private static String baseUrl;

  private List<String> metrics;
  private Client client;

  private final String INVENTORY_HOSTS = "inventory/systems";
  private final String INVENTORY_HOSTNAME = "inventory/systems/localhost";

  @BeforeClass
  public static void oneTimeSetup() {
    port = System.getProperty("liberty.test.port");
    baseUrl = "http://localhost:" + port + "/";
  }

  @Before
  public void setup() {
    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @After
  public void teardown() {
    client.close();
  }

  @Test
  public void testSuite() {
    this.testGetPropertiesTime();
    this.testListCount();
    this.testInventorySize();
  }

  public void testGetPropertiesTime() {
    connectToEndpoint(baseUrl + INVENTORY_HOSTNAME);
    validateMetric("@Timed",
                   "application:io_openliberty_guides_inventory_inventory_manager_get_properties_time_rate_per_second");
  }

  public void testListCount() {
    connectToEndpoint(baseUrl + INVENTORY_HOSTS);
    validateMetric("@Counted", "application:list");
  }

  public void testInventorySize() {
    validateMetric("@Gauge",
                   "application:io_openliberty_guides_inventory_inventory_manager_inventory_size");
  }

  public void validateMetric(String metricType, String givenMetric) {
    metrics = getMetrics();
    for (String metric : metrics) {
      if (metric.startsWith(givenMetric)) {
        if (metricType.equals("@Gauge") || metricType.equals("@Counted")) {
          assertTrue(1 == Character.getNumericValue(metric.charAt(metric.length()
              - 1)));
        } else if (metricType.equals("@Timed")) {
          float seconds = Float.parseFloat(metric.split(" ")[1]);
          assertTrue(4 > seconds);
        }
      }
    }
  }

  public void connectToEndpoint(String url) {
    Response response = this.getResponse(url);
    this.assertResponse(url, response);
    response.close();
  }

  private List<String> getMetrics() {
    Response metricsResponse = client.target("https://localhost:9443/metrics/application")
                                     .request(MediaType.TEXT_PLAIN)
                                     .header("Authorization",
                                             "Basic Y29uZkFkbWluOm1pY3JvcHJvZmlsZQ==")
                                     .get();

    BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) metricsResponse.getEntity()));
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
    assertEquals("Incorrect response code from " + url, 200,
                 response.getStatus());
  }
}
// end::MetricsTest[]
