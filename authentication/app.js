/**
 * This is an example of a basic node.js script that performs
 * the Authorization Code oAuth2 flow to authenticate against
 * the ContaAzul Accounts.
 *
 * For more information, read
 * http://developers.contaazul.com
 */

var express = require('express'); // Express web server framework
var request = require('request'); // "Request" library
var querystring = require('querystring');
var cookieParser = require('cookie-parser');

var client_id = '6fc7b7828c0f47fc9d50a700edbce30e'//'CLIENT_ID'; // Your client id
var client_secret = '4e28352e555a4e8f9cb27cb4f973ce74'//'CLIENT_SECRET'; // Your secret
var redirect_uri = 'https://9a1cc67c.ngrok.io/callback'//'REDIRECT_URI'; // Your redirect uri

/**
 * Generates a random string containing numbers and letters
 * @param  {number} length The length of the string
 * @return {string} The generated string
 */
var generateRandomString = function(length) {
  var text = '';
  var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

  for (var i = 0; i < length; i++) {
    text += possible.charAt(Math.floor(Math.random() * possible.length));
  }
  return text;
};

var stateKey = 'contaazul_auth_state';

var app = express();

app.use(express.static(__dirname + '/public'))
   .use(cookieParser());

app.get('/login', function(req, res) {

  var state = generateRandomString(16);
  res.cookie(stateKey, state);

  // your application requests authorization
  var scope = 'sales';
  res.redirect('https://api.contaazul.com/auth/authorize?' +
    querystring.stringify({
      response_type: 'code',
      client_id: client_id,
      scope: scope,
      redirect_uri: redirect_uri,
      state: state
    }));
});

function newHeaderBasic(client_id, client_secret){
  return 'Basic ' + (new Buffer(client_id + ':' + client_secret).toString('base64'))
}

app.get('/callback', function(req, res) {

  // your application requests refresh and access tokens
  // after checking the state parameter

  var code = req.query.code || null;
  var state = req.query.state || null;
  var storedState = req.cookies ? req.cookies[stateKey] : null;

  if (state === null || state !== storedState) {
    res.redirect('/#' +
      querystring.stringify({
        error: 'state_mismatch'
      }));
  } else {
    res.clearCookie(stateKey);
    var authOptions = {
      url: 'https://api.contaazul.com/oauth2/token',
      form: {
        code: code,
        redirect_uri: redirect_uri,
        grant_type: 'authorization_code'
      },
      headers: {
        'Authorization': newHeaderBasic(client_id, client_secret)
      },
      json: true
  };

  request.post(authOptions, function(error, response, body) {
    if (!error && response.statusCode === 200) {

      var access_token = body.access_token,
          refresh_token = body.refresh_token;
      console.log('AccessToken2: '+ access_token);

      var options = {
        url: 'https://api.contaazul.com/v1/me',
        headers: { 'Authorization': 'Bearer ' + access_token },
        json: true
      };

      // use the access token to access the contaazul Web API
      request.get(options, function(error, response, body) {
        console.log(body);
      });

      // we can also pass the token to the browser to make requests from there
      res.redirect('/#' +
        querystring.stringify({
          access_token: access_token,
          refresh_token: refresh_token
        }));
      } else {
        res.redirect('/#' +
          querystring.stringify({
            error: 'invalid_token'
          }));
      }
    });
  }
});

app.get('/refresh_token', function(req, res) {

  // requesting access token from refresh token
  var refresh_token = req.query.refresh_token;
  var authOptions = {
    url: 'https://api.contaazul.com/oauth2/token',
    headers: { 'Authorization': newHeaderBasic(client_id, client_secret) },
    form: {
      grant_type: 'refresh_token',
      refresh_token: refresh_token
    },
    json: true
  };

  request.post(authOptions, function(error, response, body) {
    if (!error && response.statusCode === 200) {
      var access_token = body.access_token;
      var refresh_token = body.refresh_token;
      res.send({
        'access_token': access_token,
        'refresh_token' : refresh_token
      });
    } else {
      res.send({
        'error' : error
      });
    }
  });
});

console.log('Listening on 8888');
app.listen(8888);
