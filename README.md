# bloom-server
Requirements
============
* Java >= 17
* ActiveMQ 5.18.2

You can run the required version of the ActiveMq server in a container, instead of having to install it, like this:

    docker-compose -f src/main/resources/activemq-docker-compose.yml up

and stop and destroy it like this:

    docker rm -f 
