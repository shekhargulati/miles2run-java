Miles2Run -- Social Network for runners [![Build Status](https://travis-ci.org/miles2run/miles2run.svg?branch=master)](https://travis-ci.org/miles2run/miles2run)
=============
A Java EE 7 Application that uses MySQL, MongoDB, and Redis. Also uses AngularJS, D3, Twitter Bootstrap, and C3.js.

You can learn more about miles2run architecture at http://blog.arungupta.me/2014/09/log-your-miles-and-community-runs-java-ee-7-real-world-experience/.

This technical archtiectire of this aapplication was presented at JavaOne 2014 conference https://www.parleys.com/talk/lessons-learned-from-real-world-deployments-java-ee-7

## Building miles2run application

To build the application, run the following command.

```
$ mvn clean install
```
The first time you build the project it will take some time to download the dependencies and WildFly container.
The application uses Arquillian for writing real Java EE test cases. 
