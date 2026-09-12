class Socket {
  socket: WebSocket | undefined;

  constructor() {
    this.socket = undefined;
  }

  connect(url: string) {
    if (!this.socket) {
      this.socket = new WebSocket(url);
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = undefined;
    }
  }

  isConnected() {
    return !!this.socket;
  }

  send(message: any) {
    if (this.socket?.readyState === WebSocket.OPEN) {
      // A string is sent as-is (e.g. the "ping" keepalive, matched verbatim
      // server-side) - JSON.stringify-ing it too would double-encode it into
      // a quoted string ('"ping"') that never matches. Anything else (a
      // real object payload) still gets encoded normally.
      this.socket.send(typeof message === "string" ? message : JSON.stringify(message));
    } else if (this.socket?.readyState === WebSocket.CLOSED) {
      this.socket.close();
      this.socket = undefined;
    }
  }

  onMessage(messageListener: (message: MessageEvent) => void) {
    if (this.socket) {
      this.socket.addEventListener("message", messageListener);
    }
  }

  on(eventName: string, callback: EventListenerOrEventListenerObject) {
    if (this.socket) {
      this.socket.addEventListener(eventName, callback);
    }
  }
}

export { Socket };
