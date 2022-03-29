# cse546-project1

Group 31

Group member:
1. Qiang Fu
2. Kiran Shanthappa
3. Sreshta Chowdary Kampally


SQS query names:
  request_queue
  response_queue

S3 Bucket names:
  input-project1
  output-project1



**Web-tier**
********
Generator Usage:
python multithread_workload_generator.py --num_request 100 --url "http://35.174.113.237/" --image_folder "./face_images_1000/"

config file location:
/etc/nginx/sites-available/default
/etc/nginx/nginx.conf
/etc/systemd/system/flask-entry.service

startup_script is not used.

**App-Tier**
********
1. App tier application is built using java springboot framework.
2. By default zero app tier instance will be running. App instances of EC2 will be started by the controller based on the number of message in the queue. This relation between number of messages and EC2 instance active is directly proportional with a maximum limit of 19.
3. App tier self terminate when there are no message for processing in the message queue.
4. The application starts automatically when the instance is launched. The bash script autoStartrunningListner.sh will be triggered by cloud-init as part of the user-data.
