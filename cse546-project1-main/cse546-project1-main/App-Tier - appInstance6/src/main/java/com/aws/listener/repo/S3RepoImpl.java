package com.aws.listener.repo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.aws.listener.config.AwsConfiguration;
import com.aws.listener.constants.Constants;

@Repository
public class S3RepoImpl implements S3Repo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(S3RepoImpl.class);
	
	@Autowired
	private AwsConfiguration awsConfiguration;

	@Override
	public Bucket createInBucket() {
		LOGGER.debug("Creating the bucket if not exists.");
		Bucket b = null;
		if (awsConfiguration.amazonS3().doesBucketExist(Constants.INBUCKETNAME)) {
			LOGGER.debug("The bucket exits, so returning the existing bucket.");
			b = getInBucket();
		} else {
			LOGGER.debug("Creating the bucket "+Constants.INBUCKETNAME);
			b = awsConfiguration.amazonS3().createBucket(Constants.INBUCKETNAME);
		}

		return b;
	}
	
	public Bucket createOutBucket() {
		LOGGER.debug("Creating the bucket if not exists.");
		Bucket b = null;
		if (awsConfiguration.amazonS3().doesBucketExist(Constants.OUTBUCKETNAME)) {
			LOGGER.debug("The bucket exits, so returning the existing bucket.");
			b = getOutBucket();
		} else {
			LOGGER.debug("Creating the bucket "+Constants.OUTBUCKETNAME);
			b = awsConfiguration.amazonS3().createBucket(Constants.OUTBUCKETNAME);
		}

		return b;
	}

	@Override
	public Bucket getInBucket() {
		LOGGER.debug("Returning the bucket.");
		Bucket namedBucket = null;
		List<Bucket> buckets = awsConfiguration.amazonS3().listBuckets();
		for (Bucket b : buckets) {
			if (b.getName().equals(Constants.INBUCKETNAME)) {
				namedBucket = b;
			}
		}

		return namedBucket;
	}
	
	public Bucket getOutBucket() {
		LOGGER.debug("Returning the bucket.");
		Bucket namedBucket = null;
		List<Bucket> buckets = awsConfiguration.amazonS3().listBuckets();
		for (Bucket b : buckets) {
			if (b.getName().equals(Constants.OUTBUCKETNAME)) {
				namedBucket = b;
			}
		}

		return namedBucket;
	}

	@Override
	public void putObjectOutBucket(String key, String value) {
		LOGGER.debug("Inserting the object into the out bucket.");
		System.out.println("Inserting the object into the out bucket - " + key);
		this.createOutBucket();
		byte[] contentAsBytes = null;
		try {
			contentAsBytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
		ObjectMetadata md = new ObjectMetadata();
		md.setContentLength(contentAsBytes.length);
		//System.out.println("byte array: " + contentAsBytes);
		//System.out.println("byte array length: " + md);
		//System.out.println("value string: " + value);
		//awsConfiguration.amazonS3().putObject(new PutObjectRequest(Constants.INBUCKETNAME, key, contentsAsStream, md));
		awsConfiguration.amazonS3().putObject(Constants.OUTBUCKETNAME, key, contentsAsStream, md);
	}
	
	@Override
	public void putObjectInBucket(String key, String value) {
		LOGGER.debug("Inserting the object into the in bucket.");		
		this.createInBucket();
		byte[] contentAsBytes = null;
		System.out.println("Inserting the object into the in bucket - " + key);
		contentAsBytes = Base64.getDecoder().decode(value);		
		this.createInBucket();
		String filePath = "/home/ec2-user/" + key;		
		try {
			PutObjectRequest request = new PutObjectRequest(Constants.INBUCKETNAME, key, new File(filePath));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentAsBytes.length);
            metadata.setContentType("image/jpg");
            //metadata.setCacheControl("public, max-age=31536000");
            request.setMetadata(metadata);
            awsConfiguration.amazonS3().putObject(request);            
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
