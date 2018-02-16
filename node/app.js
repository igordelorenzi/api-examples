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

var auth = require('./app/auth.js');
var sales = require('./app/sales.js');
var invoices = require('./app/invoices.js');
var product = require('./app/product.js');

var stateKey = 'contaazul_auth_state';

var app = express();

app.use(express.static(__dirname + '/public'))
   .use(cookieParser());

app.get('/login', auth.authorize);
app.get('/callback', auth.callback);
app.get('/refresh_token', auth.refreshToken);

app.get('/list_sales', sales.list);

app.get('/list_invoices', invoices.list);

app.get('/list_products', product.list);
app.get('/delete_product', product.delete);

console.log('Listening on 8888');
app.listen(8888);
