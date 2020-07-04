# first of all import the socket library
import json
import socket

# next create a socket object
from json import JSONEncoder

import numpy
from sentence_transformers import SentenceTransformer

class NumpyArrayEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, numpy.ndarray):
            return obj.tolist()
        return JSONEncoder.default(self, obj)


def sendNumpyArray(numpyarray, conn):

    #convert to dictionary
    numpyData = {"array": numpyarray}

    #seriallize numpy array
    jsonEncodedNumpyData = json.dumps(numpyData, cls=NumpyArrayEncoder)  # use dump() to write array into file

    cleandAndTabSepatatedNumpyData = jsonEncodedNumpyData[12:-3].replace(" ", "").replace("],[", "\t")

    message = cleandAndTabSepatatedNumpyData.encode('ascii')  # it is converted to asciii to reduce the size of the message

    #sending the length of the message as a 4 byte integer
    conn.send(len(message).to_bytes(4, byteorder='big'))  # send the length of message in four byte

    #sending the message
    conn.send(message)



#initialize transformer

#model = SentenceTransformer('bert-base-nli-mean-tokens')   #BERT fine tuned on NLI
model = SentenceTransformer('bert-base-nli-stsb-mean-tokens')   #BERT fine tuned on NLI then on STS benchmark



s = socket.socket()
print("Socket successfully created")

# reserve a port on your computer in our
# case it is 12345 but it can be anything
port = 12345

# Next bind to the port
# we have not typed any ip in the ip field
# instead we have inputted an empty string
# this makes the server listen to requests
# coming from other computers on the network
s.bind(('', port))
print("socket binded to %s" % (port))

# put the socket into listening mode
s.listen(5)
print("socket is listening")

# a forever loop until we interrupt it or
# an error occurs
while True:

    # Establish connection with client.
    conn, addr = s.accept()

    print('Got connection from', addr)

    while True:
        t = conn.recv(1024)
        sentence = t[2:]  # remove the bytes which are added by java client to the socket string
        #print(sentence)
#        print("recieved from client", sentence.decode())   #decode the UTF-8 encoded string by JAVA
 #       print("message length is :", len(sentence.decode()))

        if sentence == b'##Over##':
            break

        if not sentence:
            break

        SentenceList = []
        SentenceList.append(sentence.decode())

        sentence_embeddings = model.encode(SentenceList)

  #      print("Sentence Embeddings",sentence_embeddings)

        sendNumpyArray(sentence_embeddings, conn)

    # Close the connection with the client
    conn.close()
    break
