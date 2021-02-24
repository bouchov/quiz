#!/bin/bash

source ./app.conf

JAVA=java
JAVA_OPTS=(
    '-Xmx412m'
    '-Xms412m'
    "-XX:+HeapDumpOnOutOfMemoryError"
    "-XX:HeapDumpPath=$logs"
    "-XX:FlightRecorderOptions:repository=$diags,stackdepth=1024"
    "-XX:StartFlightRecording:name=main,settings=default,delay=1m,maxsize=10G,maxage=24h,disk=true,filename=$diags/main.jfr,dumponexit=true"
    "-Doracle.net.tns_admin=$confdir/ora"
)

check_pids "$app"

JAVA_CLASS=org.springframework.boot.loader.JarLauncher
PARAMS=$*

CLASSPATH="$homedir/classes"
for f in $(ls -1 "$libdir"/*.jar 2> /dev/null); do
    CLASSPATH="$CLASSPATH:$f"
done

mkdir -p "$homedir/classes"
mkdir -p "$rundir"
mkdir -p "$logs"
mkdir -p "$diags"

pid_file_prefix="$rundir/$app"
err_file="${logs}/${app}.err"

CLASSPATH="${CLASSPATH}" "${JAVA}" "${JAVA_OPTS[@]}" "${JAVA_CLASS}" "${PARAMS[@]}" >> "${logs}/${app}.out" 2>> "$err_file" &

pid="$!"
sleep 1
if ! ps | grep "$pid" > /dev/null; then
    echo "$JAVA" "${JAVA_OPTS[@]}" "${JAVA_CLASS}" "${PARAMS[@]}" >> "$err_file"
    die 1 "could not start $app background process"
fi
pidfile="$pid_file_prefix.$pid"
echo "PARAMS=${PARAMS[@]}" > "$pidfile"
echo "CLASSPATH=$CLASSPATH"  >> "$pidfile"
warn "$app background process is started"
