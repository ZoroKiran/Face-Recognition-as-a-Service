package com.aws.listener.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.ParseException;

public interface ListenerAndDispatchingService {
	
	public void generalFunction() throws IOException, ParseException;
	
	public byte[] convertToImg(String base64) throws IOException;
	
	public void writeByteToImageFile(byte[] imgBytes, String imgFileName) throws IOException ; 

	public void deleteImageFile(String imgFileName);


}
