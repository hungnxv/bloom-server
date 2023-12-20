# bloom-server
Requirements
============
* Java >= 17
* ActiveMQ 5.15.9
* Redis 7.2

You can run the required version of the ActiveMq server in a container, instead of having to install it, like this:

    docker-compose -f docker/activemq.yml up

and stop and destroy it like this:

    `docker rm -f`

Start redis:

    docker-compose -f docker/redis.yml up
