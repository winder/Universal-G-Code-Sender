# GRBL simulator for UGS

A local [GRBL 1.1](https://github.com/gnea/grbl) controller built from [grbl-sim](https://github.com/grbl/grbl-sim) and exposed over TCP. Use it to try streaming and jogging in UGS without a router connected.

The image compiles `grbl_sim` and puts `socat` in front of it. grbl-sim talks on a terminal, so each TCP client gets its own pseudo-terminal. Local echo is turned off, because UGS would otherwise treat the echoed commands as controller output.

The build applies [grbl-sim pull request 29](https://github.com/grbl/grbl-sim/pull/29), which is still open. Current GCC rejects the stock simulator sources (`-fno-common`). On the first start the container also writes a valid `EEPROM.DAT` before it accepts a connection, so UGS sees the normal `Grbl 1.1h ['$' for help]` banner instead of a settings dump.

## Start it

From the repository root:

```sh
docker build -t ugs-grbl-sim docker/grbl-sim
docker run --rm --name ugs-grbl-sim -it -p 2323:2323 ugs-grbl-sim
```

This stays in the foreground. Ctrl-C stops it. 
The terminal prints a line when a client connects and when it disconnects. 

To also print each command and each simulator reply, set `GRBL_LOG=1`:

```sh
docker run --rm --name ugs-grbl-sim -it -p 2323:2323 -e GRBL_LOG=1 ugs-grbl-sim
```

UGS polls status continuously, so that log is busy while a client is connected. `>` is data from the client, `<` is data from the simulator.

To use another host port, change the left side of `-p` and the port you enter in UGS.

## Connect UGS

1. Open the connection settings.
2. Set the driver to **TCP** and the firmware to **GRBL**.
3. Set the host to `127.0.0.1`.
4. Select `2323` in the baud-rate field and connect.

UGS builds the address as `tcp://host:baud`, so that field is the TCP port.

Jog commands and streamed motion run at about real-machine speed, so `ok` can arrive after the move finishes. Status lines include `Pn:PXYZRHS` because the simulator's pins default to looking triggered. Hard limits are off (`$21=0`), and jogging still moves.
