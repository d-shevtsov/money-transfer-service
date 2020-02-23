package com.shevtsov.mt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationIntegrationTest {

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private static final int SERVER_PORT = 8000;
    private static final String SERVER_URI = "http://localhost:" + SERVER_PORT;
    private static final String GET_BALANCE_PATH = "/api/balance/";

    @Before
    public void setUp() {
        try {
            Application.configureHttpServerAndStart();
        } catch (IOException e) {
            System.out.println("Cannot configure HttpServer");
        }
    }

    @Test
    public void balanceIsValid() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8000/api/balance/1")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals("200000.0", response.body());
    }

    @Test
    public void getBalanceFromNonExistingAccount() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8000/api/balance/2000000")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void getBalanceIdIsNotANumber() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8000/api/balance/test")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    public void getBalanceWithPostRequest() throws IOException, InterruptedException {
        HttpRequest.BodyPublisher content = HttpRequest.BodyPublishers.noBody();
        HttpRequest httpRequest = HttpRequest.newBuilder().POST(content).uri(URI.create("http://localhost:8000/api/balance/test")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    @Test
    public void transferBetweenAccountsIsCorrect() throws IOException, InterruptedException {
        String transferBody = "{\"from\":0,\"to\":1,\"amount\":1.1}";
        HttpRequest.BodyPublisher content = HttpRequest.BodyPublishers.ofString(transferBody);
        HttpRequest httpRequest = HttpRequest.newBuilder().POST(content).uri(URI.create("http://localhost:8000/api/transfer")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertEquals("199998.9", sendGetBalanceRequest(0).body());
        assertEquals("200001.1", sendGetBalanceRequest(1).body());
    }

    @Test
    public void transferBetweenAccountsBodyIsEmpty() throws IOException, InterruptedException {
        String transferBody = "";
        HttpRequest.BodyPublisher content = HttpRequest.BodyPublishers.ofString(transferBody);
        HttpRequest httpRequest = HttpRequest.newBuilder().POST(content).uri(URI.create("http://localhost:8000/api/transfer")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        assertEquals("200000.0", sendGetBalanceRequest(0).body());
        assertEquals("200000.0", sendGetBalanceRequest(1).body());
    }

    @Test
    public void transferBetweenAccountsBodyIsMalformed() throws IOException, InterruptedException {
        String transferBody = "{\"from\":0,\"to\":1,\"amount\":amount}";
        HttpRequest.BodyPublisher content = HttpRequest.BodyPublishers.ofString(transferBody);
        HttpRequest httpRequest = HttpRequest.newBuilder().POST(content).uri(URI.create("http://localhost:8000/api/transfer")).build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());

        assertEquals("200000.0", sendGetBalanceRequest(0).body());
        assertEquals("200000.0", sendGetBalanceRequest(1).body());
    }

    private HttpResponse<String> sendGetBalanceRequest(long balanceId) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(URI.create(SERVER_URI + GET_BALANCE_PATH + balanceId)).build();
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

}