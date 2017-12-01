# ContaAzul authorization example - Node.js

## Getting started

In order to run this example, you will need to install Node.js. On the [official Node.js website](https://nodejs.org/en/) you can learn how to install it.
With Node.js and npm installed, clone this repository, navigate to the _node_ directory and run :

`$ npm install`

This command will download all the dependencies required by this example.

## Configuring

Before running the example you must change your credentials in the `app/app.js` file.
The variables that must be filled with your information are :

 - client_id
 - client_secret
 - redirect_uri

To get this information you'll need to have an API Credential.
For more information about how to get an API Credential go to [http://developers.contaazul.com](http://developers.contaazul.com)

## Running the example

To run the example just execute this command in the _node_ directory :

`$ node app.js`

Then access the example in the browser in `http://localhost:/8888`

## Dependencies

This example uses some dependencies to assist some steps :

  - [cookie-parser](https://github.com/expressjs/cookie-parser) : stores and retrieves cookies
  - [express](https://github.com/expressjs/express) : web framework for node
  - [querystring](https://github.com/Gozala/querystring) : transform js objects in url params
  - [request](https://github.com/request/request) : make http calls to the ContaAzul API
