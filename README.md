# streaming

Java program for streaming twitter data.

## Property File
```
# Twitter Credentials
twitter.consumer.key=
twitter.consumer.secret=
twitter.access.token=
twitter.access.token.secret=
#Keywords, leave empty for sample
keywords=
#Output
server.name=localhost
server.port=44444
print=false
json=true
```

## Execution

1. Compile ```mvn clean package```
2. Copy the tar file to the running environment ```scp target/streaming-0.0.1.tar.gz myserver:/my/path```
3. Untar ```tar -xzvf streaming-0.0.1.tar.gz```
4. Modify example.properties
5. Execute 
     ```
     java -cp lib/twitter4j-core-4.0.5.jar:lib/twitter4j-stream-4.0.5.jar:lib/streaming-0.0.1.jar \
     com.idiro.streaming.SimpleTwitterStream example.properties
     ```
     
## Execution with flume

0. Install flume on the running environment
1. Compile ```mvn clean package```
2. Copy the tar file to the running environment ```scp target/streaming-0.0.1.tar.gz myserver:/my/path```
3. Untar ```tar -xzvf streaming-0.0.1.tar.gz```
4. Modify example.properties
5. Modify flume-hdfs-copy.conf
6. Change environment settings in kickoff_streaming.sh 
6. Run ```kickoff_streaming.sh```

## Notes

1. Apache Flume has a twitter-source action by default. It is doing exclusively sampling. But more importantly, problem can arise when trying to read the data generated in Hive (see [here](http://stackoverflow.com/questions/36053306/cloudera-5-4-2-avro-block-size-is-invalid-or-too-large-when-using-flume-and-twi)).
2. CDH provides as well a [twitter streaming plugins](https://github.com/cloudera/cdh-twitter-example), but there are library conflicts with the official flume version 1.6.0
