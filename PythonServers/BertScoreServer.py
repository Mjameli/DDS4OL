# first of all import the socket library
import json
import socket

# next create a socket object
from json import JSONEncoder

import numpy
import torch
from transformers import *     #install it with     pip install transformers 
import bert_score

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




layer_number = 12     # it must be a non-negative number
modelpath_or_name = "/media/Volume3/Jabalameli/CoreBertFromSentenceBert/bert"       #the last word of the path should be 'bert' 
scorer = bert_score.BERTScorer(model_type=modelpath_or_name, lang="en",num_layers = layer_number, batch_size=1, rescale_with_baseline=False)

print("The layer " , layer_number, "of " , modelpath_or_name, " is used.")

#from bert_score.utils import  (
 #   get_model,
  #  get_idf_dict,
#    bert_cos_score_idf,
#    get_bert_embedding,
#    model_types,
#    lang2model,
#    model2layers,
 #   get_hash,
 #   cache_scibert,
 #   sent_encode,
#)
#get_bert_embedding(["this is for test"], scorer._model,scorer._tokenizer, batch_size=1).shape


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
        sentenceandprop = t[2:]  # remove the bytes which are added by java client to the socket string
        #print(sentence)
#        print("recieved from client", sentence.decode())   #decode the UTF-8 encoded string by JAVA
 #       print("message length is :", len(sentence.decode()))

        if sentenceandprop == b'##Over##':
            break

        if not sentenceandprop:
            break
        [sentence , prop] = sentenceandprop.decode().split("\t")    #the sentence and the property  must be separated by a "\t"

        (P, R, F) = scorer.score([sentence], [prop], return_hash=False)
#        print(f"P={P.mean().item():.6f} R={R.mean().item():.6f} F={F.mean().item():.6f}")
        #print(type(P))
        #print(P.shape)
        
        result = torch.cat((P, R, F))
        #print(result)

        sendNumpyArray(result.numpy(), conn)

    # Close the connection with the client
    conn.close()
    break

