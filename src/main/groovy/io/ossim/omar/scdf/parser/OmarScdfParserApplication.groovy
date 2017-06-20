package io.ossim.omar.scdf.parser

import groovy.util.logging.Slf4j
import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.SendTo
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

/**
 * Created by  on 5/31/2017
 */
@SpringBootApplication
@EnableBinding(Processor.class)
@Slf4j
class OmarScdfNotifierApplication
{
	/**
	 * AWS access key
	 */
	@Value('${cloud.aws.credentials.accessKey}')
	String accessKey

	/**
	 * AWS secret key
	 */
	@Value('${cloud.aws.credentials.secretKey}')
	String secretKey

	/**
	 * Extention
	 */
	@Value('${extension}')
	String extension




	/**
	 * The client used to connect to S3 for downloading files
	 */
	AmazonS3Client s3Client

	/**
	 * The main entry point of the SCDF Downloader application.
	 * @param args
	 */
	static final void main(String[] args)
	{
		SpringApplication.run OmarScdfNotifierApplication, args
	}

	/**
	 * Receives a message from the SCDF aggregator, downloads the files in the message
	 * and puts them in the filepath on the SCDF server
	 * @param message The message object from the SCDF aggregrator (in JSON)
	 * @return a JSON message of the files downloaded
	 */
	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	final String download(final Message<?> message)
	{
		log.debug("Message received: ${message}")

		if (null != message.payload)
		{
			final def parsedJson = new JsonSlurper().parseText(message.payload)


			final BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey)
			s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(creds)).build()


			// Local storage vars for the json iteration
			String email
            String filenameWithPathandExt
			File localFile
			ObjectMetadata object

			// Loop through each received JSON file and download
			parsedJson.files.each { file ->

				filenameWithPathandExt = file
				String ext1 = FilenameUtils.getExtension(filenameWithPathandExt)


				if(ext1 == extension) {

					// open file, grab e-mail address, send
					localFile = new File(filenameWithDirectory)
					email = localFile.text

				}
			}

			// Create the output JSON
			final JsonBuilder emailJson = new JsonBuilder()
			emailJson(email: email)

			log.debug("email: ${email}")

			return emailJson.toString()
		}
	}
}
