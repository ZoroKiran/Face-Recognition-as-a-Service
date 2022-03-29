package com.aws.listener.service;

public interface S3Service {
	
	public void putObjectOutBucket(String key, String value);

	public void putObjectInBucket(String key, String value);
}
