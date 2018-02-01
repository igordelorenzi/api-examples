package com.contaazul.javaOauthSample;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
class ContaAzulService {

    private final String CLIENT_ID = "{YOUR CLIENT ID}";
    private final String CLIENT_SECRET = "{YOUR CLIENT SECRET}";
    private final String CONTAAZUL_AUTH_URL = "https://api.contaazul.com/auth/authorize";
    private final String REDIRECT_URL = "{YOUR NGROK OR SERVER URL}/user/token";
    private final String CONTAAZUL_TOKEN_URL = "https://api.contaazul.com/oauth2/token";
    private final String CONTAAZUL_PRODUCTS_URL = "https://api.contaazul.com/v1/products/";

    public ResponseEntity<Product[]> listProducts(String accessToken, String page) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(
                getProductListUrl(page),
                HttpMethod.GET,
                getAuthorizedRequestConfig(accessToken),
                Product[].class
        );
    }

    /*
     * Request ContaAzul OAuth 2 tokens
     * */
    public OAuthData getContaAzulTokens(String url) throws IOException {
        HttpEntity<MultiValueMap<String, String>> httpEntity;
        httpEntity = getTokenRequestConfig();

        RestTemplate restTemplate = new RestTemplate();
        OAuthData oAuthData = restTemplate.postForObject(url, httpEntity, OAuthData.class);
        return oAuthData;
    }

    /*
     * In our sample application we redirect the user to the initial page, but you don't need to to it.
     * After getting the OAuth2 tokens, you can store them and redirect the user to any of your application's page.
     * */
    public String getHomeUrlWithTokens(HttpServletRequest request, OAuthData oAuthData) {
        String homeUrl = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(homeUrl)
                .queryParam("access_token", oAuthData.getAccessToken())
                .queryParam("refresh_token", oAuthData.getRefreshToken());
        return builder.build().toUri().toString();
    }

    /*
     * Builds ContaAzul OAuth 2 Authorization URL
     * */
    public String getRedirectUrl(HttpServletRequest request, String contaazulState) throws UnsupportedEncodingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_AUTH_URL)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("state", contaazulState)
                .queryParam("redirect_uri", URLEncoder.encode(REDIRECT_URL, "UTF-8"));
        return builder.build().toUri().toString();
    }

    /**
     * Generates a random string with 16 chars
     **/
    public String generateRandomString() {
        Random r = new Random();
        StringBuilder b = new StringBuilder();
        char[] stringOptions = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        for (int i = 0; i < 16; i++) {
            b.append(stringOptions[r.nextInt(stringOptions.length)]);
        }
        return b.toString();
    }

    /*
     *  Builds the token request body
     */
    public String getRefreshTokenUrl(String refreshToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_TOKEN_URL)
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token");
        return builder.build().toUri().toString();
    }

    /*
     *  Builds the token request body
     */
    public String getTokenUrl(String authorizationCode) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_TOKEN_URL)
                .queryParam("code", authorizationCode)
                .queryParam("redirect_uri", REDIRECT_URL)
                .queryParam("grant_type", "authorization_code");
        return builder.build().toUri().toString();
    }

    public void deleteProduct(String accessToken, String id) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(
                CONTAAZUL_PRODUCTS_URL + id,
                HttpMethod.DELETE,
                getAuthorizedRequestConfig(accessToken),
                String.class
        );
    }

    /*
     *  Builds the get request for product list call
     */
    private HttpEntity<MultiValueMap<String, String>> getAuthorizedRequestConfig(String accessToken) {
        return new HttpEntity<MultiValueMap<String, String>>(
                null,
                getAuthorizationHeader(accessToken)
        );
    }

    /*
     * Create authorization header for API calls
     * */
    private MultiValueMap<String, String> getAuthorizationHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();

        List acceptedTypes = new ArrayList();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(acceptedTypes);
        headers.add("Authorization", "Bearer " + accessToken);

        return headers;
    }

    /*
     *  Builds the product list url
     */
    private String getProductListUrl(String page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_PRODUCTS_URL)
                .queryParam("page", page)
                .queryParam("size", "50");
        return builder.build().toUri().toString();
    }

    /*
     *  Builds the request body and headers for the post request to be made, in this case it's a HttpEntity class, expected by
     *   RestTemplate
     */
    private HttpEntity<MultiValueMap<String, String>> getTokenRequestConfig() {
        return new HttpEntity<MultiValueMap<String, String>>(
                null,
                getTokenRequestHeaders()
        );
    }

    /*
     *  Builds the token request headers
     */
    private HttpHeaders getTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getBasicAuthenticationHeader());
        return headers;
    }

    /*
     * Builds the basic authentication header
     */
    private String getBasicAuthenticationHeader() {
        String credentialsToBeEncoded = CLIENT_ID + ':' + CLIENT_SECRET;
        return "Basic " + Base64.getEncoder().encodeToString(credentialsToBeEncoded.getBytes());
    }
}
