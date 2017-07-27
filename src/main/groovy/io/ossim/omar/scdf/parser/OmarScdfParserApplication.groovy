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
import org.apache.commons.io.FilenameUtils;

/**
 * Created by  on 5/31/2017
 */
@SpringBootApplication
@EnableBinding(Processor.class)
@Slf4j
class OmarScdfParserApplication
{

	/**
	 * body
	 */
	@Value('${from}')
	String from

	/**
	 * Extentsion
	 */
	@Value('${body}')
	String body

	/**
	 * Extension
	 */
	@Value('${extension}')
	String extension

	/**
	 * The main entry point of the SCDF file parser application.
	 * @param args
	 */
	static final void main(String[] args)
	{
		SpringApplication.run OmarScdfParserApplication, args
	}

	/**
	 * Receives a message from the SCDF downloader, opens the files, and gets the e-mail
	 * address from them
	 * @param message The message object from the SCDF downloader (in JSON)
	 * @return a JSON message of the files downloaded
	 */
	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	final String parse(final Message<?> message)
	{

		if (null != message.payload)
		{
			final def parsedJson = new JsonSlurper().parseText(message.payload)


			// Local storage vars for the json iteration
			String email
            String filename
			File localFile
            String zipFileUrl = parsedJson.zipFileUrl

			// Loop through each received JSON file and download
			parsedJson.files.each { file ->

				filename = file
				log.debug("filenameWithPathandExt : ${filename }")
				String ext1 = FilenameUtils.getExtension(filename)
				String ext2 = "." + ext1
				log.debug("ext2 : ${ext2 }")

				if ( ext2.equals( extension ) )
				{

					// open file, grab e-mail address, send
					localFile = new File(filename)
					email = localFile.text
					log.debug("localFile.text: ${localFile.text}")

				}
			}

			// Create the output JSON
			// *** IF THERE IS ONLY 1 FILE WITH GIVEN EXTENION,
			// BELOW CODE CAN BE MOVED INSIDE IF STATEMENT IN LOOP ****
			final JsonBuilder emailJson = new JsonBuilder()
			emailJson to: email,
					from: from,
					message: body,
                     zipFileUrl: zipFileUrl


			log.debug("emailJson.toString(): ${emailJson.toString()}")

			return emailJson.toString()
		}
	}
}
