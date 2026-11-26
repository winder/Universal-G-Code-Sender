#!/bin/sh
set -eu
cd /opt/grbl/grbl/grbl-sim

# The first launch restores default settings and writes them to EEPROM.DAT.
# Finish that before accepting a client so UGS sees a normal GRBL startup.
if [ ! -f .eeprom-ready ]; then
  socat EXEC:"./grbl_sim.exe -n -s /dev/null -b /dev/null",pty,raw,echo=0 SYSTEM:"sleep 1" >/dev/null 2>&1 || true
  touch .eeprom-ready
fi

# session.sh prints connect and disconnect on the container terminal.
# GRBL_LOG=1 also copies each direction of the byte stream there (-v).
socat_opts=""
if [ "${GRBL_LOG:-0}" = "1" ]; then
  socat_opts="-v"
fi

echo "GRBL simulator listening on port ${GRBL_PORT:-2323}"
# Stay pid 1 so Ctrl-C is delivered here. The kernel ignores signals that
# pid 1 has not handled, and socat would not exit on them.
socat $socat_opts \
  "TCP-LISTEN:${GRBL_PORT:-2323},bind=0.0.0.0,reuseaddr,fork,keepalive" \
  "EXEC:/session.sh,pty,raw,echo=0" &
child=$!
trap 'kill -TERM "$child" 2>/dev/null || true; wait "$child" 2>/dev/null || true; exit 0' INT TERM
wait "$child"
