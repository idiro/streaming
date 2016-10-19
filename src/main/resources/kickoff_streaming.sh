#!/bin/bash
set -e
######################################################################
######################################################################
#Change me

#Twitter Streaming vars
#PROXY_PORT=3000
#PROXY_HOST=myproxy.local.net
PROPERTY_FILE=example.properties

#FLUME VARS
FLUME_HOME=~/apache-flume-1.6.0-bin
FLUME_EXEC_CONF=flume-hdfs-copy.conf
######################################################################
######################################################################

SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"
LIB_PATH=${SCRIPT_PATH}/lib
JARS=${LIB_PATH}/twitter4j-core-4.0.5.jar:${LIB_PATH}/twitter4j-stream-4.0.5.jar:${LIB_PATH}/streaming-0.0.1.jar
CLASS=com.idiro.streaming.SimpleTwitterStream
FLUME_BIN=${FLUME_HOME}/bin/flume-ng
FLUME_CONF=${FLUME_HOME}/conf
FLUME_EXEC_NAME=a1
FLUME_EXTRA_PROP="-Dflume.root.logger=DEBUG,console"

#Kill the previous jobs 
if [[ -f ${SCRIPT_PATH}/save_flume_pid.txt ]]; then
    kill -9 `cat ${SCRIPT_PATH}/save_flume_pid.txt`||true
    rm ${SCRIPT_PATH}/save_flume_pid.txt
fi
if [[ -f ${SCRIPT_PATH}/save_twitter_pid.txt ]]; then
    kill -9 `cat ${SCRIPT_PATH}/save_twitter_pid.txt`||true
    rm ${SCRIPT_PATH}/save_twitter_pid.txt
fi

#Kick-off flume
EXEC_FLUME="nohup ${FLUME_BIN} agent --conf ${FLUME_CONF} --conf-file ${FLUME_EXEC_CONF} --name ${FLUME_EXEC_NAME} ${FLUME_EXTRA_PROP}"
echo ${EXEC_FLUME}
${EXEC_FLUME} 2>&1 &
echo $! > ${SCRIPT_PATH}/save_flume_pid.txt
#Wait for the internal network port to open
sleep 5

#Kick-off twitter
if [[ -n "${PROXY_HOST}" ]]; then
    EXTRAS="-DproxyHost=${PROXY_HOST} -DproxyPort=${PROXY_PORT} -DproxySet=true"
fi
EXEC_STREAM="nohup java -cp ${JARS} ${EXTRAS} ${CLASS} ${PROPERTY_FILE}"
echo ${EXEC_STREAM}
${EXEC_STREAM} 2>&1 &
echo $! > ${SCRIPT_PATH}/save_twitter_pid.txt
