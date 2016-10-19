#!/bin/bash

kill -9 `cat save_twitter_pid.txt` || true
kill -9 `cat save_flume_pid.txt` || true
rm save_twitter_pid.txt || true
rm save_flume_pid.txt || true
