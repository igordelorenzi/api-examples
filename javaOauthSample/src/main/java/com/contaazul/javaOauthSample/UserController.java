package com.contaazul.javaOauthSample;

import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
class UserController {

    private final String CLIENT_ID = "{YOUR CLIENT ID}";
    private final String CLIENT_SECRET = "{YOUR CLIENT SECRET}";
    private final String CONTAAZUL_STATE_KEY = "contaazul_auth_state";
    private final String CONTAAZUL_AUTH_URL = "https://api.contaazul.com/auth/authorize";
    private final String REDIRECT_URL = "{YOUR NGROCK OR SERVER URL}/user/token";
    private final String CONTAAZUL_TOKEN_URL = "https://api.contaazul.com/oauth2/token";
    private final String CONTAAZUL_PRODUCTS_URL = "https://api.contaazul.com/v1/products/";

    /*
     * List user products
     * */
    @RequestMapping(method = GET, value = "/user/products")
    public ResponseEntity<?> listProducts(@RequestParam("access_token") String accessToken,
                                          @RequestParam("page") String page) {

        ResponseEntity<Product[]> productList = null;

        try {
            RestTemplate restTemplate = new RestTemplate();

            productList = restTemplate.exchange(
                    getProductListUrl(page),
                    HttpMethod.GET,
                    getAuthorizedRequestConfig(accessToken),
                    Product[].class
            );


        } catch (Exception e) {
            System.out.println("---");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return new ResponseEntity<>(productList.getBody(), HttpStatus.OK);
    }


    /*
     *  Builds the get request for product list call
     *
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
     *
     */
    private String getProductListUrl(String page) {
        if (page == null) {
            page = "";
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_PRODUCTS_URL)
                .queryParam("page", page)
                .queryParam("size", "50");

        return builder.build().toUri().toString();
    }

    @RequestMapping(method = DELETE, value = "/user/deleteProduct")
    public void deleteProduct(
            @RequestParam("access_token") String accessToken,
            @RequestParam("id") String id) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(
                CONTAAZUL_PRODUCTS_URL + id,
                HttpMethod.DELETE,
                getAuthorizedRequestConfig(accessToken),
                String.class
        );
    }

    /**
     * Redirects users to ContaAzul Authorization URL, adding credentials and a state variable, to be used later to
     * assure that the user is coming from your application integration flow
     **/
    @RequestMapping(method = GET, value = "/user/authorize")
    public void authorize(HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        String contaazulState = generateRandomString();
        String authorizationUrl = getRedirectUrl(request, contaazulState);

        Cookie cookie = new Cookie(CONTAAZUL_STATE_KEY, contaazulState);
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect(authorizationUrl);
    }

    /*
     *
     * Builds ContaAzul OAuth 2 Authorization URL
     *
     * */
    private String getRedirectUrl(HttpServletRequest request, String contaazulState) throws UnsupportedEncodingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_AUTH_URL)
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("state", contaazulState)
                .queryParam("redirect_uri", URLEncoder.encode(REDIRECT_URL, "UTF-8"));

        return builder.build().toUri().toString();
    }

    /*
     *
     * This is the URL called after authorization. It will receive an authorization code from ContaAzul,
     * which will be used to get access token and refresh token.
     *
     * */
    @RequestMapping(method = GET, value = "/user/token")
    public ResponseEntity<?> getOauth2Tokens(HttpServletRequest request,
                                             HttpServletResponse response,
                                             @RequestParam("code") String authorizationCode,
                                             @RequestParam("state") String state,
                                             @CookieValue(CONTAAZUL_STATE_KEY) String storedState) throws IOException {


        if (!state.equals(storedState)) {
            return new ResponseEntity<String>("{error:'Invalid State'}", HttpStatus.UNAUTHORIZED);
        } else {

            OAuthData oAuthData = getContaAzulTokens(getTokenUrl(authorizationCode));
            response.sendRedirect(
                    getHomeUrlWithTokens(request, oAuthData)
            );
        }
        return null;
    }

    /*
     *
     * Get new accessToken using refreshToken
     *
     * */
    @RequestMapping(method = GET, value = "/user/refreshToken")
    public ResponseEntity<?> refreshToken(HttpSession session,
                                          HttpServletRequest request,
                                          HttpServletResponse response,
                                          @RequestParam("refresh_token") String refreshToken) throws IOException {

        OAuthData oAuthData = getContaAzulTokens(getRefreshTokenUrl(refreshToken));
        return new ResponseEntity<>(oAuthData, HttpStatus.OK);
    }

    /*
     * Request ContaAzul OAuth 2 tokens
     * */
    private OAuthData getContaAzulTokens(String url) throws IOException {
        HttpEntity<MultiValueMap<String, String>> httpEntity;
        httpEntity = getTokenRequestConfig();

        RestTemplate restTemplate = new RestTemplate();
        OAuthData oAuthData = new OAuthData();

        try {

            oAuthData = restTemplate.postForObject(url, httpEntity, OAuthData.class);
        } catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
        }

        return oAuthData;
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
     *  Builds the token request body
     */
    private String getTokenUrl(String authorizationCode) throws UnsupportedEncodingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_TOKEN_URL)
                .queryParam("code", authorizationCode)
                .queryParam("redirect_uri", REDIRECT_URL)
                .queryParam("grant_type", "authorization_code");

        return builder.build().toUri().toString();
    }

    /*
     *  Builds the token request body
     */
    private String getRefreshTokenUrl(String refreshToken) throws UnsupportedEncodingException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(CONTAAZUL_TOKEN_URL)
                .queryParam("refresh_token", refreshToken)
                .queryParam("grant_type", "refresh_token");

        return builder.build().toUri().toString();
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

    /*
     *
     * In our sample application we redirect the user to the initial page, but you don't need to to it.
     * After getting the OAuth2 tokens, you can store them and redirect the user to any of your application's page.
     *
     * */
    private String getHomeUrlWithTokens(HttpServletRequest request, OAuthData oAuthData) {
        String homeUrl = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(homeUrl)
                .queryParam("access_token", oAuthData.getAccessToken())
                .queryParam("refresh_token", oAuthData.getRefreshToken());

        return builder.build().toUri().toString();
    }


    /**
     * Generates a random string with 16 chars
     **/
    private String generateRandomString() {
        Random r = new Random();
        StringBuilder b = new StringBuilder();
        char[] stringOptions = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        for (int i = 0; i < 16; i++) {
            b.append(stringOptions[r.nextInt(stringOptions.length)]);
        }

        return b.toString();
    }


}
