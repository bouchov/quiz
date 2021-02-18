#!/bin/bash

source ./app.conf

SIGNAL='-15'
action='shutdown'

while [[ ${#@} -gt 0 ]]; do
    case "$1" in
        -f | --force)
            CMD='-9'
            action='killed'
            shift
            ;;
        *)
            die 1 "Illegal argument $1"
            shift
            ;;
    esac
done

pids=($(ls -1 "${rundir}/$app".* 2> /dev/null))

if [[ ${#pids[@]} == 1 ]]; then
    pid="${pids##*.}"
    procdir="/proc/${pid}"
    if [ -d "$procdir" ]; then
        warn "Found $app process $pid. The process will be $action"
        kill "${SIGNAL}" "${pid}"

        wait_time="${kill_timeout:-5}"
        sleep_seconds=$wait_time
        while [[ $sleep_seconds -gt 0 ]]; do
            if [ ! -d $procdir ] ; then
                rm "$pids"
                echo
                die 0 "$app is stopped"
            fi
            sleep 1
            ((sleep_seconds -= 1))
            echo -n '.'
        done
        if [ -d "$procdir" ] ; then
            echo
            die 2 "background $app process with PID=$pid didn't stop after $wait_time seconds"
        else
            rm "$pids"
            echo
            warn "$app is stopped"
        fi
    else
      warn "background $app process with PID=$pid does not exist, removing $pids"
      rm "$pids"
    fi
else
  die 1 "$app PID file not found: " "[${pids[@]}]"
fi
