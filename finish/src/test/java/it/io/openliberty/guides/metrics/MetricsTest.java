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
// tag::testClass[]
package it.io.openliberty.guides.metrics;

import static org.junit.Assert.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.io.*;
import java.util.*;

import javax.enterprise.context.Conversation;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class MetricsTest {
    private static String port;
    private static String baseUrl;
    
    private Client client;

    private final String INVENTORY_HOSTS = "inventory/hosts";
    private final String INVENTORY_LOCALHOST = "inventory/hosts/localhost";
    private final String INVENTORY_IP_ADDRESS = "inventory/hosts/127.0.0.1";
    private final String INVENTORY_HOSTNAME = "inventory/hosts/DESKTOP-8GIHP0V";
    
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

    // tag::testSuite[]
    @Test
    public void testSuite() {
        this.testListCount();
        this.testHostsNumber();
    }
    // end::testSuite[]

    // tag::testListCount[]
    public void testListCount() {
            
        Response firstResponse = this.getResponse(baseUrl + INVENTORY_HOSTS);
        Response secondResponse = this.getResponse(baseUrl + INVENTORY_HOSTS);
        Response thirdResponse = this.getResponse(baseUrl + INVENTORY_HOSTS);

        this.assertResponse(baseUrl + INVENTORY_HOSTS, firstResponse);
        this.assertResponse(baseUrl + INVENTORY_HOSTS, secondResponse);
        this.assertResponse(baseUrl + INVENTORY_HOSTS, thirdResponse);

        firstResponse.close();
        secondResponse.close();
        thirdResponse.close();

        List<String> metrics = this.getMetrics();
        
        for(String metric : metrics) {
            if(metric.startsWith("application:list_count")) {
                assertEquals(3, Character.getNumericValue(metric.charAt(metric.length()-1)));
            }
        }
    }
    // end::testListCount[]
    

    // tag::testHostsNumber[]
    public void testHostsNumber() {

        Response firstResponse = this.getResponse(baseUrl + INVENTORY_LOCALHOST);
        Response secondResponse = this.getResponse(baseUrl + INVENTORY_IP_ADDRESS);
        Response thirdResponse = this.getResponse(baseUrl + INVENTORY_HOSTNAME);
         
        this.assertResponse(baseUrl + INVENTORY_LOCALHOST, firstResponse);
        this.assertResponse(baseUrl + INVENTORY_IP_ADDRESS, secondResponse);
        this.assertResponse(baseUrl + INVENTORY_HOSTNAME, thirdResponse);

        firstResponse.close();
        secondResponse.close();
        thirdResponse.close();

        List<String> metrics = this.getMetrics();

        for(String metric : metrics) {
            if(metric.startsWith("application:io_openliberty_guides_inventory_inventory_manager_get_host_count_hosts")) {
                assertEquals(3, Character.getNumericValue(metric.charAt(metric.length()-1)));
            }
        }
    }
    // end::testHostsNumber[]
        
    // end::tests[]
    private List<String> getMetrics(){
        Response metricsResponse = client.target("https://localhost:9443/metrics/application")
                                    .request(MediaType.TEXT_PLAIN)
                                    .header("Authorization", "Basic Y29uZkFkbWluOm1pY3JvcHJvZmlsZQ==")
                                    .get();

        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)metricsResponse.getEntity()));
        List<String> result = new ArrayList<String>();
        try {
            String input;
            while ((input = br.readLine()) != null){
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

    // tag::helpers[]
    // tag::javadoc[]
    /**
     * <p>Returns response information from the specified URL.</p>
     * 
     * @param url - target URL.
     * @return Response object with the response from the specified URL.
     */
    // end::javadoc[]
    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    // tag::javadoc[]
    /**
     * <p>Asserts that the given URL has the correct response code of 200.</p>
     * 
     * @param url      - target URL.
     * @param response - response received from the target URL.
     */
    // end::javadoc[]
    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }
}
// end::testClass[]
