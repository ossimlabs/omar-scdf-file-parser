package io.ossim.omar.scdf.parser

import groovy.util.logging.Slf4j
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
import org.apache.commons.io.FilenameUtils

/**
 * Created by  on 5/31/2017
 */
@SpringBootApplication
@EnableBinding(Processor.class)
@Slf4j
class OmarScdfParserApplication {

    /**
     * Extension
     */
    @Value('${extension}')
    String extension

    /**
     * The main entry point of the SCDF file parser application.
     * @param args
     */
    static final void main(String[] args) {
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
    @SuppressWarnings("unused")
    final String parse(final Message<?> message) {

        if (null != message.payload) {
            final def parsedJson = new JsonSlurper().parseText(message.payload)

            // Local storage vars for the json iteration
            String email
            String filename
            String zipFileUrl = parsedJson.zipFileUrl
            def lineReader

            // Loop through each received JSON file and download
            parsedJson.files.each { file ->

                filename = file
                log.debug("filenameWithPathandExt : ${filename}")
                String ext = "." + FilenameUtils.getExtension(filename)
                log.debug("ext : ${ext}")

                if (ext == extension) {

                    // Assume that the e-mail file is only one line
                    // and that the line is the e-mail address in
                    // question
                    try {
                        lineReader = new Scanner(new File(filename))
                        email = lineReader.nextLine()

                        if (log.isDebugEnabled()) {
                            log.debug("email to send to: ${email}")
                        }
                    } catch (final NoSuchElementException e){
                        // Scanner did not have a line
                        log.error("Email file did not have data!")
                    } finally {
                        lineReader.close()
                    }

                }
            }

            // Create the output JSON. Assume that there is only 1 e-mail
            // file or that the contents of all e-mail files are the same
            final JsonBuilder emailJson = new JsonBuilder()
            emailJson to: email,
                      zipFileUrl: zipFileUrl


            log.debug("emailJson.toString(): ${emailJson.toString()}")

            return emailJson.toString()
        }
    }
}
