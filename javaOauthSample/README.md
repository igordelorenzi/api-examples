# Java Conta Azul Integration Example

This project contains a simple application that integrates with Conta Azul APIs using 0Auth 2 Authentication.

## Before you start

Before you run the application, you  need first to get your integration credentials, and redirect uri:

- client_id
- client_secret
- redirect_uri

*** It's very important not to let  you credentials exposed in the frontend or any public place ***

For more information on how to get API Credentials go to [http://developers.contaazul.com](http://developers.contaazul.com)

Your redirect uri is the URL to the screen in your application to which the user will be redirect after authorizing the access to Conta Azul API.

## Running this application:

This application must be accessible through the internet, so Conta Azul servers can redirect the user to it after the authorization proccess.
You can either deploy this application in a public server, or run locally and use some tool to make the tunneling.

### Configuration that needs to be changed:

In the UserController file there are some changes that need to be made. At the top of the file, the following constants:

- CLIENT_ID - set this to the client id received from Conta Azul team
- CLIENT_SECRET - set this to the client secret received from Conta Azul team
- REDIRECT_URL - set this to the URL of your server(when deployed), or to the ngrock url (if you are running locally) plus 
"/user/controller"(i.e. http://01ebaa6e.ngrok.io/user/token)

### Running locally

In order to run this example locally you need to create a tunnel which will redirect outside traffic from an especific port 
into your local server correspondent port.

We strongly recommend [NGROCK](https://ngrok.com/). It can be used for free, and is very easy to use.

To run Ngrok on linux, you first need to download the app, and then  to access the directory where it is placed, and run the following command:

./ngrock http 8080

It will give you a public URL that will redirect all trafic on the 8080 port to your local server.
You can change the port where it runs, if needed.  This URL is part of the redirect uri(together with '/user/token')

#### Importing and running on an IDE (IntelliJ or Eclipse)

Import this project as a maven project in IntelliJ or Eclipse,  then run the JavaOauthSampleApplication class
(right click + Eclipse:'run as java application' or 'IntelliJ:run JavaOauthSampleApplication main' )

To run from command line, use the following command:

mvn spring-boot:run

### Deploying to a server

Before building the application, don't forget to update the UserController with your application and redirect uri.  

To build the deployable artifact you  need to run the following command:

mvn clean install

The resulting file will be at: target/javaOauthSample-0.0.1-SNAPSHOT.war