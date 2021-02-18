#!/bin/bash

source ./app.conf

if [[ ${#@} == 0 ]]; then
  echo 'Diagnostic tool'
  echo "Usage: $(basename ${0}) [option...]"
  echo 'Where options:'
  echo '-t  | --threads     - taking thread dump of java process'
  echo '-d  | --dump        - taking heap dump of java process'
  echo '-fm | --flush-main  - copy last 10m of main jfr to file'
  echo '-p  | --profile     - start java profiling for 5 minutes'
  echo '-s  | --stop        - stop profiling before 5 minutes pass'
  echo '-f  | --flush       - copy profiling data to file'
  echo '-c  | --check       - check flight recorder state'
  exit 1
fi

pids=($(ls -1 "${rundir}/$app".* 2> /dev/null))

if [[ ${#pids[@]} == 1 ]]; then
    pid="${pids##*.}"
    warn "Found $app process $pid"
else
    die 1 "$app PID file not found: " "[${pids[@]}]"
fi

CMD=(
    'jcmd'
    "$pid"
)

mkdir -p "${diags}"
PREFIX="${diags}/$(date +"%Y%m%d-%H%M%S").$pid"

while [[ ${#@} -gt 0 ]]; do
  case "$1" in
      -t | --threads)
        echo 'taking thread dump'
        "${CMD[@]}" "Thread.print -l -e" >> "${PREFIX}.threads"
        shift
        ;;
      -d | --dump)
        echo 'taking heap dump'
        "${CMD[@]}" "GC.heap_dump -all ${PREFIX}.hprof"
        shift
        ;;
      -p | --profile)
        echo 'start profiling for 5 minutes'
        "${CMD[@]}" "JFR.start name=memory filename=${PREFIX}.memory-full.jfr" \
                      "settings=${confdir}/memory.jfc duration=5m dumponexit=true disk=true"
        shift
        ;;
      -s | --stop)
        echo 'stop profiling'
        "${CMD[@]}" "JFR.stop name=memory filename=${PREFIX}.memory.jfr"
        shift
        ;;
      -f | --flush)
        echo 'copy profiling data to file'
        "${CMD[@]}" "JFR.dump name=memory filename=${PREFIX}.memory.jfr"
        shift
        ;;
      -c | --check)
        echo 'check flight recorder state'
        "${CMD[@]}" "JFR.check"
        shift
        ;;
      -fm | --flush-main)
        echo 'copy last 10m of main jfr to file'
        "${CMD[@]}" "JFR.dump name=main begin=-10m filename=${PREFIX}.main.jfr"
        shift
        ;;
      *)
        echo "unknown option $1"
        shift
        ;;
  esac
done
