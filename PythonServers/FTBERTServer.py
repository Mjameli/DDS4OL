# first of all import the socket library
import json
import socket

# next create a socket object
from json import JSONEncoder

import numpy
import torch
from transformers import *     #install it with  ---->   pip install transformers 

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


def tokenEmbed2wordEmbed(sentence ,embeddings, tokenizer):
    tokens = tokenizer.tokenize(sentence)
    wordembeddings = []
    limit = len(tokens)
    startIndex = 0;
    while (startIndex<limit):
       #word = tokens[startIndex]
        wordembedding = tokenembeddings[startIndex]
        endIndex = startIndex + 1

        while (endIndex<limit and tokens[endIndex].startswith("##")):
            wordembedding = wordembedding + tokenembeddings[endIndex] 
         #  word = word+(tokens[endIndex].replace("##", ""))
            endIndex = endIndex + 1

        wordembedding = torch.div(wordembedding,endIndex-startIndex)
        wordembeddings.append(wordembedding) 
        startIndex = endIndex

    return torch.stack(wordembeddings)
     


#Very Important: you should set "output_hidden_states" to true in config.json file to get all layers embeddings
tokenizer = BertTokenizer.from_pretrained('../Embeddings/bert')
model = BertModel.from_pretrained('../Embeddings/bert')

layer_number = -1;     # -1 means last layer


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


        input_ids = torch.tensor(tokenizer.encode(sentence.decode())).unsqueeze(0)  # Batch size 1
        outputs = model(input_ids)
        hidden_states = outputs[2]   #if you get "tuple index out of range" , it means you do not set "output_hidden_states" to true in config.json file
        #print(len(hidden_states))  # 13

        #embedding_output = hidden_states[0]   # means initial embeddings(before layer #1)
        attention_hidden_states = hidden_states[1:]   #output of 12 layers

        stacked_layers = torch.stack(attention_hidden_states).squeeze()
        specific_layer = stacked_layers[layer_number]    #-1 means last layer (#12)
        token_embeddings = specific_layer[1:-1]   #remove special tokens(CLS,SEP)

        sendNumpyArray(token_embeddings.detach().numpy(), conn)

    # Close the connection with the client
    conn.close()
    break
