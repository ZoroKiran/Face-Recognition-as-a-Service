package com.aws.listener.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import com.amazonaws.services.sqs.model.Message;
import com.aws.listener.config.AwsConfiguration;
import com.aws.listener.constants.Constants;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;

@Service
public class ListenerAndDispatchingServiceImpl implements ListenerAndDispatchingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListenerAndDispatchingServiceImpl.class);

	@Autowired
	private AwsConfiguration awsConfiguration;
	
	@Autowired
	private SqsService sqsService;

	@Autowired
	private S3Service s3Service;
	
	@Autowired
	private Ec2Service ec2Service;

	@Override
	public void generalFunction() throws IOException, ParseException {
		LOGGER.debug("Listener and dispatch main routine.");
		System.out.println("Listener and dispatch main routine.");
		while (true) {
			Message message = sqsService.receiveMessage(Constants.INPUTQUEUENAME, 20, 15);
			if (message == null) {
				break;
			}
			//String messageBody = sqsService.parseImageName(message.getBody());
			//System.out.println(message.toString());
			String messageBody = message.getBody();

			Object obj = new JSONParser().parse(messageBody);
	          
	        // typecasting obj to JSONObject
	        JSONObject jo = (JSONObject) obj;
	          
	        // getting name and image string	        
            String fullnameImageIn = (String) jo.get("filename");
            String nameImageIn = fullnameImageIn.split("\\.")[0];
			String strImageIn = (String) jo.get("image");
			String uuidImageIn = (String) jo.get("uuid");
			String trimstrImageIn = strImageIn.substring(2, strImageIn.length() - 1);
	        System.out.println(fullnameImageIn);
	        //System.out.println(trimstrImageIn);
			// create a buffered image
			//byte[] base64Val=convertToImg(trimstrImageIn);  
	        //writeByteToImageFile(base64Val, fullnameImageIn);
	        System.out.println("Decoding the base64 string");
	        byte[] decodedBytes = Base64.getDecoder().decode(trimstrImageIn);
	        File file = new File("/home/ec2-user/" + fullnameImageIn);
	        file.setExecutable(false);
		    file.setReadable(false);
		    file.setWritable(false);
	        FileUtils.writeByteArrayToFile(file, decodedBytes);
			s3Service.putObjectInBucket(fullnameImageIn, trimstrImageIn);
			
			//String predictValue = sqsService.deepLearningOutput(fullnameImageIn);
			System.out.println("Running the deep learning model.");
			System.out.println("Image string: " + fullnameImageIn);
			String predictValue = null;
			
			try {
				System.out.println("Calling process builder...");
				ProcessBuilder pb = new ProcessBuilder("python3", "/home/ec2-user/face_recognition.py", "/home/ec2-user/" + fullnameImageIn);
				Process p = pb.start();
				p.waitFor();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader ebr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				predictValue = br.readLine();
				System.out.println("Reading input stream...");
				if(ebr.readLine() != null) {
					predictValue = predictValue + ebr.readLine();
				}
				br.close();
				p.destroy();
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			}
			System.out.println("image classification: "+ predictValue);
			
			if (predictValue == null) {
				predictValue = "NoPrediction";
			}
			predictValue = predictValue.trim();
			//System.out.println(predictValue);
			
			s3Service.putObjectOutBucket(nameImageIn, predictValue);
			// Write in Output SQS and then delete
	        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

	        try {
	            CreateQueueResult create_result = awsConfiguration.amazonSQS().createQueue(Constants.OUTPUTQUEUE);
	        } catch (AmazonSQSException e) {
	            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
	                throw e;
	            }
	        }

	        String queueUrl = awsConfiguration.amazonSQS().getQueueUrl(Constants.OUTPUTQUEUE).getQueueUrl();
			SendMessageRequest send_msg_request = new SendMessageRequest()
			        .withQueueUrl(queueUrl)
			        .withMessageBody("{\"uuid\": \"" + uuidImageIn + "\", \"result\": \"" + predictValue + "\"}")
			        .withDelaySeconds(5);
			awsConfiguration.amazonSQS().sendMessage(send_msg_request);
			//sqsService.sendMessage("{\"uuid\": \"" + uuidImageIn + "\", \"result\": \"" + predictValue + "\"}", Constants.OUTPUTQUEUE, 0);
			sqsService.deleteMessage(message, Constants.INPUTQUEUENAME);
			deleteImageFile("/home/ec2-user/" + fullnameImageIn);
		}
		ec2Service.endInstance();
	}
	
    public byte[] convertToImg(String base64) throws IOException  
    {  
         return Base64.getDecoder().decode(base64);  
    }  
    public void writeByteToImageFile(byte[] imgBytes, String imgFileName) throws IOException  
    {  
         File imgFile = new File(imgFileName);  
         BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));  
         ImageIO.write(img, "jpg", imgFile);         
         
    }
    
    public void deleteImageFile(String imgFileName) {
    	File imgFile = new File(imgFileName);
    	if (imgFile.delete()) {
            System.out.println("File deleted successfully");
        }
        else {
            System.out.println("Failed to delete the file");
        }
    
    }


}
