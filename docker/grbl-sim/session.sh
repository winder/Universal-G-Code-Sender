#!/bin/sh
# One GRBL process per TCP client. stdin/stdout stay on the pseudo-terminal
# socat attached to the socket. Messages are written to the container's
# stderr so they show on the docker terminal and are not sent to UGS.
cd /opt/grbl/grbl/grbl-sim
echo "client connected" >/proc/1/fd/2
finish() {
  trap - EXIT HUP TERM INT
  echo "client disconnected" >/proc/1/fd/2
}
trap finish EXIT HUP TERM INT
./grbl_sim.exe -n -s /dev/null -b /dev/null
