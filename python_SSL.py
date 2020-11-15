import socket, ssl

users = {}
users["Jose"] = "1234" #Test User added

def sslServerLoop():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(('0.0.0.0', 8888))
    s.listen(1)

    context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain('server_finished.pem')

    while True:
        conn, addr = s.accept()
        print("Accepted!")
        sslConn = context.wrap_socket(conn, server_side=True)
        data = sslConn.recv(1024)
        print(data.decode("utf-8"))
        received = data.decode('utf-8').split("\n")
        msg = serverOptions(received)
        sslConn.send(msg.encode())
        sslConn.close()


def serverOptions(message):
    if (message[0] == "+AUTH"):
        if (message[1] in users):
            if(users[message[1]] == message[2]):
                return "OK"
            else:
                return "FAIL"
        else:    
            return "FAIL"

sslServerLoop()