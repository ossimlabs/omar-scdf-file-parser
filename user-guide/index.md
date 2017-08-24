#  omar-scdf-file-parser
The File parser is a Spring Cloud Data Flow (SCDF) Processor.
This means it:
1. Receives a message on a Spring Cloud input stream using Kafka.
2. Performs an operation on the data.
3. Sends the result on a Spring Cloud output stream using Kafka to a listening SCDF Processor or SCDF Sink.

## Purpose
The file parser receives a JSON message from the Downloader containing a list of files that were downloaded.
The file parser then loops through the files and if it has the appropriate extension, opens the file and extracts the email address.
An example of the directory structure the Downloader will create:
```
/data/2017/06/22/933657b1-6752-42dc-98d8-73ef95a5e780/
```
The Downloader then sends a message to the Extractor with the list of files successfully downloaded and their full filepaths.

## JSON Input Example (from the Downloader)
```json
{
   "files":[
      "/data/2017/06/22/09/933657b1-6752-42dc-98d8-73ef95a5e780/12345/SCDFTestImages.zip",
      "/data/2017/06/22/09/933657b1-6752-42dc-98d8-73ef95a5e780/12345/SCDFTestImages.zip_56734.email"
   ]
}
```

## JSON Output Example (to the omar-scdf-notifier-email)
```json
{
    to: "",
    zipFileUrl      }}
```