#!/bin/bash
cd /home/ec2-user/
sudo chmod 776 listener-running-0.0.1-SNAPSHOT.jar
java -jar /home/ec2-user/listener-running-0.0.1-SNAPSHOT.jar
