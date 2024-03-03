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
    if (this.socket) {
      this.socket.send(JSON.stringify(message));
    }
  }

  onMessage(messageListener : (message: MessageEvent) => void) {
    if (this.socket) {
        this.socket.addEventListener("message", messageListener);
      }
  };

  on(eventName: string, callback: EventListenerOrEventListenerObject) {
    if (this.socket) {
      this.socket.addEventListener(eventName, callback);
    }
  }
}

export { Socket };
