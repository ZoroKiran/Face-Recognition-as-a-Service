package com.aws.listener.repo;

import com.amazonaws.services.s3.model.Bucket;

public interface S3Repo {
	
	public Bucket createInBucket();
	
	public Bucket getInBucket();
	
	public Bucket createOutBucket();
	
	public Bucket getOutBucket();
	
	public void putObjectInBucket(String key, String value);
	
	public void putObjectOutBucket(String key, String value);

}
