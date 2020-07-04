# first of all import the socket library
import json
import socket
import time
# next create a socket object
from json import JSONEncoder

import numpy
import tensorflow as tf
#import tensorflow_hub as hub
import tensorflow_hub as hub
import os
#import ssl

#ssl._create_default_https_context = ssl._create_unverified_context


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


#disable GPU
os.environ['CUDA_VISIBLE_DEVICES'] = '0'

#disable GPU
#os.environ['CUDA_VISIBLE_DEVICES'] = '-1'

#url = "https://tfhub.dev/google/elmo/3"
url = "/media/Volume3/Jabalameli/ELMO/ELMO3/"
embed = hub.KerasLayer(url,output_key='default')
#output_key='elmo' for word embeddings
#without outputkey   for sentence embeddings   ('default')

#The output dictionary contains:
# word_emb: the character-based word representations with shape [batch_size, max_length, 512].
# lstm_outputs1: the first LSTM hidden state with shape [batch_size, max_length, 1024].
# lstm_outputs2: the second LSTM hidden state with shape [batch_size, max_length, 1024].
# elmo: the weighted sum of the 3 layers, where the weights are trainable. This tensor has shape [batch_size, max_length, 1024]
# default: a fixed mean-pooling of all contextualized word representations with shape [batch_size, 1024].



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
#with tf.Session() as sess:

#    sess.run(tf.global_variables_initializer())
#    sess.run(tf.tables_initializer())



    # Establish connection with client.
    conn, addr = s.accept()

    print('Got connection from', addr)

    while True:
        t = conn.recv(1024)
#        starttime = time.perf_counter()
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


        embeddings = embed(tf.convert_to_tensor(SentenceList))


#        sentence_embeddings = sess.run(embeddings)
        
		
 #       print("Sentence Embedded in {} second.",time.perf_counter()-starttime)

        sendNumpyArray(embeddings[0].numpy(), conn)  #[0] for first sentence

#        print("the sentence "+sentence.decode()+" was embedded.")
    # Close the connection with the client
    conn.close()
  

