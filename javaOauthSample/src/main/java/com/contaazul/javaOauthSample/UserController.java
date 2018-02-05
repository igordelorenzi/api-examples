package com.contaazul.javaOauthSample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequestMapping(value = "/user")
@RestController
class UserController {

    private final String CONTAAZUL_STATE_KEY = "contaazul_auth_state";

    @Autowired
    ContaAzulService contaAzulService;

    /**
     * Redirects users to ContaAzul Authorization URL, adding credentials and a state variable, to be used later to
     * assure that the user is coming from your application integration flow
     **/
    @RequestMapping(method = GET, value = "/authorize")
    public void authorize(HttpServletResponse response, HttpServletRequest request) throws IOException{

        String contaAzulState = contaAzulService.generateRandomString();
        String authorizationUrl = contaAzulService.getRedirectUrl(request, contaAzulState );

        Cookie cookie = new Cookie(CONTAAZUL_STATE_KEY, contaAzulState );
        cookie.setPath("/");

        response.addCookie(cookie);
        response.sendRedirect(authorizationUrl);
    }

    /*
     * This is the URL called after authorization. It will receive an authorization code from ContaAzul,
     * which will be used to get access token and refresh token.
     * */
    @RequestMapping(method = GET, value = "/token")
    public ResponseEntity<?> getOauth2Tokens(HttpServletRequest request,
                                             HttpServletResponse response,
                                             @RequestParam("code") String authorizationCode,
                                             @RequestParam("state") String state,
                                             @CookieValue(CONTAAZUL_STATE_KEY) String storedState) throws IOException {

        if (!state.equals(storedState)) {
            return new ResponseEntity<String>("{error:'Invalid State'}", HttpStatus.UNAUTHORIZED);
        }

        Token token = contaAzulService.getContaAzulTokens(
                contaAzulService.getTokenUrl(authorizationCode)
        );

        response.sendRedirect(
                contaAzulService.getHomeUrlWithTokens(request, token)
        );
        return null;
    }

    /*
     * Get new accessToken using refreshToken
     * */
    @RequestMapping(method = GET, value = "/refreshToken")
    public ResponseEntity<Token> refreshToken(HttpSession session,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              @RequestParam("refresh_token") String refreshToken) throws Exception {

        Token token = contaAzulService.getContaAzulTokens(
                contaAzulService.getRefreshTokenUrl(refreshToken)
        );
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    /*
     * List user products
     * */
    @RequestMapping(method = GET, value = "/products")
    public ResponseEntity<Product[]> listProducts(@RequestParam("access_token") String accessToken,
                                                  @RequestParam("page") String page) {

        ResponseEntity<Product[]> productListResponse = contaAzulService.listProducts(accessToken, page);

        if (productListResponse.getStatusCode() != HttpStatus.OK) {
            return new ResponseEntity(productListResponse.getBody().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Product[]>(productListResponse.getBody(), HttpStatus.OK);
    }

    @RequestMapping(method = DELETE, value = "/deleteProduct")
    public void deleteProduct(
            @RequestParam("access_token") String accessToken,
            @RequestParam("id") String id) {

        contaAzulService.deleteProduct(accessToken, id);
    }
}
