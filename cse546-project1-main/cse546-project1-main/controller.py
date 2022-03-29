import boto3
import prop

starter = False
user_data = '''Content-Type: multipart/mixed; boundary="//"
MIME-Version: 1.0

--//
Content-Type: text/cloud-config; charset="us-ascii"
MIME-Version: 1.0
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename="cloud-config.txt"

#cloud-config
cloud_final_modules:
- [scripts-user, always]

--//
Content-Type: text/x-shellscript; charset="us-ascii"
MIME-Version: 1.0
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename="userdata.txt"

#!/bin/bash
/bin/echo "Hello World" >> /home/ec2-user/testfile.txt
nohup /home/ec2-user/autoStartrunningListener.sh > /home/ec2-user/listner.out &
--//--'''


def auto_scaler():
    sqs = boto3.resource('sqs')
    queue = sqs.get_queue_by_name(QueueName='request_queue')
    queue_len = int(queue.attributes['ApproximateNumberOfMessages'])
    print(f"Queue length: {queue_len}")
    if queue_len > 0:
        ec2 = boto3.resource('ec2')
        instances_running = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'pending']}])
        no_of_instances_running = 0
        for instance in instances_running:
            no_of_instances_running += 1
        print(f"No. instances running: {no_of_instances_running}")
        if no_of_instances_running >= 20:
            print("Maximum instances already created")
            return
        elif no_of_instances_running > queue_len:
            return
        else:
            no_of_instances_available = 20 - no_of_instances_running
            print(f'Instances available: {no_of_instances_available}')
            if no_of_instances_available > queue_len:
                for i in range(queue_len):
                    print(f"Creating {i + 1} new instances")
                    ec2_client = boto3.client("ec2", region_name="us-east-1")
                    instance = ec2_client.run_instances(ImageId="ami-0f25321ba5b9bf7ba", MinCount=1, MaxCount=1,
                                                        UserData=user_data,
                                                        InstanceType="t2.micro", KeyName="project1-keypair",
                                                        SecurityGroupIds=["sg-0cfb955ecc133e9b1"], TagSpecifications=[
                            {'ResourceType': 'instance', 'Tags': [{'Key': 'Name', 'Value': 'App-instance'}]}])
                    print("instance created")
            else:
                for i in range(no_of_instances_available):
                    print(f"Creating {i + 1} new instances")
                    ec2_client = boto3.client("ec2", region_name="us-east-1")
                    instance = ec2_client.run_instances(ImageId="ami-0f25321ba5b9bf7ba", MinCount=1, MaxCount=1,
                                                        UserData=user_data,
                                                        InstanceType="t2.micro", KeyName="project1-keypair",
                                                        SecurityGroupIds=["sg-0cfb955ecc133e9b1"], TagSpecifications=[
                            {'ResourceType': 'instance', 'Tags': [{'Key': 'Name', 'Value': 'App-instance'}]}])
                    print("instance created")
    else:
        return


if not starter:
    print("auto scaling...")
    starter = True
while starter:
    auto_scaler()
    prop.rest()
