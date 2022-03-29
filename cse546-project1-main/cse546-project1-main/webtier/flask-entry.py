from flask import Flask, redirect, url_for, request
import boto3
import json
import base64
import uuid
import sys
from botocore.exceptions import ClientError
import logging
import os
from pymemcache.client.base import PooledClient

client = PooledClient('localhost')
app = Flask(__name__)
sqs = boto3.resource('sqs')
request_queue = sqs.get_queue_by_name(QueueName='request_queue')
response_queue = sqs.get_queue_by_name(QueueName='response_queue')
s3_client = boto3.client('s3')
bucket = 'input-project1'
vis_timeout_sec = 10


# print(queue.url)
# print(queue.attributes.get('DelaySeconds'))

@app.route("/query", methods=["GET", "POST"])
@app.route("/", methods=["GET", "POST"])
def printquery():
    if 'myfile' in request.files.keys():
        file = request.files['myfile']
    else:
        return "Received request, but no file found"
    if file:
        current_id = str(uuid.uuid4())
        b64str = base64.b64encode(file.read())
        queryobj = {'uuid': current_id, 'image': str(b64str), 'filename': file.filename}

        temp_json_filepath = f'./{file.filename}.json'
        with open(temp_json_filepath, 'w') as outfile:
            json.dump(queryobj, outfile)

        temp_image_filepath = f'./{file.filename}'
        with open(temp_image_filepath, 'wb') as outfile:
            outfile.write(base64.b64decode(b64str))

        try:
            with open(temp_image_filepath, 'rb') as uploadfile:
                s3response = s3_client.upload_fileobj(uploadfile, bucket, file.filename)
            # os.remove(temp_image_filepath)
            # os.remove(temp_json_filepath)
        except ClientError as e:
            logging.error(e)
        sqs_request_response = request_queue.send_message(MessageBody=json.dumps(queryobj))
        while True:
            result = client.get(current_id)
            print(f"Getting result = {str(result)}")
            if result is not None:
                return result
            for message in response_queue.receive_messages(MaxNumberOfMessages=10, VisibilityTimeout=vis_timeout_sec,
                                                           WaitTimeSeconds=1):
                message_dict = json.loads(message.body)
                print(f'Got message: uuid={message_dict["uuid"]}, result={message_dict["result"]}')
                client.set(message_dict['uuid'], message_dict['result'])
                message.delete()
            result = client.get(current_id)
            if result is not None:
                return result

    return 'query error'


if __name__ == "__main__":
    app.run(debug=True)
