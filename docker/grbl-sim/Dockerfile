# GRBL 1.1 simulator reachable by UGS over TCP.
#
# UGS connection: driver TCP, firmware GRBL, host 127.0.0.1, port 2323.
# The port is entered in the baud-rate field because UGS uses that value as
# the TCP port.
#
# Run the container in the foreground (no -d). Ctrl-C stops it.
# Connection lines are written to the container terminal. Set GRBL_LOG=1
# to also print the G-code sent by the client and the simulator replies.
FROM debian:bookworm-slim

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      build-essential \
      ca-certificates \
      git \
      patch \
      socat \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt
RUN git clone --depth 1 https://github.com/gnea/grbl.git \
 && git clone --depth 1 https://github.com/grbl/grbl-sim.git grbl/grbl/grbl-sim

COPY pr-29.patch /tmp/pr-29.patch
RUN cd /opt/grbl/grbl/grbl-sim \
 && sed -i 's/\r$//' validator.c \
 && patch -p1 < /tmp/pr-29.patch \
 && sed -i 's/#define SIM_ECHO_TERMINAL 1/#define SIM_ECHO_TERMINAL 0/' platform_LINUX.c \
 && make main \
 && rm -rf /opt/grbl/.git /opt/grbl/grbl/grbl-sim/.git /tmp/pr-29.patch

ENV GRBL_PORT=2323
ENV GRBL_LOG=0
EXPOSE 2323
COPY entrypoint.sh /entrypoint.sh
COPY session.sh /session.sh
RUN chmod +x /entrypoint.sh /session.sh
ENTRYPOINT ["/entrypoint.sh"]
