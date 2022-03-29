import logging
import boto3
import uuid
from botocore.exceptions import ClientError
"""
s3 = boto3.client('s3')
response = s3.list_buckets()

# Output the bucket names
print('Existing buckets:')
for bucket in response['Buckets']:
    print(f'  {bucket["Name"]}')"""

current_id = uuid.uuid4()
print(1)
print(str(current_id))