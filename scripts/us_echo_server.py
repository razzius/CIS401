import time
import signal
import sys
import SocketServer


def signal_handler(*a):
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

# logging.basicConfig(stream=sys.stdout,
#                     level=logging.DEBUG,
#                     format='%(asctime)s\t%(levelname)s\t%(message)s')

# logger = logging.getLogger()

class MyUDPHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        data = self.request[0]
        print('{} {}'.format(int(time.time() * 1000000), data))
        # socket = self.request[1]
        # logger.info('Packet from {}'.format(self.client_address[0]))
        # socket.sendto(data.upper(), self.client_address)


if __name__ == '__main__':
    server = SocketServer.UDPServer(('165.123.199.173', 9000), MyUDPHandler)
    server.serve_forever()
