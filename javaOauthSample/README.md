# Java ContaAzul Integration Example

This project contains a simple application that integrates with ContaAzul APIs using 0Auth 2 Authorization.

## Before you start

Before you run the application, you need to get your integration credentials, and redirect uri:

- client_id
- client_secret
- redirect_uri

*** It's very important not to let your credentials exposed in the frontend or any public place ***

For more information on how to get API Credentials go to [http://developers.contaazul.com](http://developers.contaazul.com)

## Configuring

Before running the example you must change your credentials in the `ContaAzulService.java` file.
The variables that must be filled with your information are :

 - client_id
 - client_secret
 - redirect_uri

To get this information you'll need to have an API Credential.
For more information about how to get an API Credential go to [http://developers.contaazul.com](http://developers.contaazul.com)

## Running this application:

This application must be accessible through the internet, so that ContaAzul can redirect the user to it after the authorization process.
You can either deploy this application to a public server, or run locally and use some tool to make the tunneling.

### Running locally

In order to run this example locally you need to create a tunnel which will redirect outside traffic from an specific port into your local server correspondent port.

We strongly recommend [NGROK](https://ngrok.com/). It can be used for free, and is very easy to use.

To run Ngrok on linux, you need to download the app, access the directory where it is placed, and run the following command:

./ngrok http 8080

It will give you a public URL that will redirect all trafic on the 8080 port to your local server.
You can change the port where it runs, if needed. This URL is part of the redirect uri(together with '/user/token')

### Importing and running on an IDE (IntelliJ or Eclipse)

Import this project as a maven project in IntelliJ or Eclipse,  then run the JavaOauthSampleApplication class
(right click + Eclipse:'run as java application' or 'IntelliJ:run JavaOauthSampleApplication main' )

To run from command line, use the following command:

mvn spring-boot:run

## Deploying to a server

To build the deployable artifact you need to run the following command:

mvn clean install

The resulting file will be at: target/javaOauthSample-0.0.1-SNAPSHOT.war