/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.net.*;
import java.io.*;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class PythonConnectionClient {

    // A Java program for a Client 
    // initialize socket and input output streams 
    private Socket socket = null;
    //private DataInputStream input = null;
    private DataOutputStream out = null;
    private DataInputStream socketin = null;

    // constructor to put ip address and port 
    public PythonConnectionClient(String address, int port) {
        // establish a connection 
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // takes input from terminal 
            //input = new DataInputStream(System.in);
            // sends output to the socket 
            out = new DataOutputStream(socket.getOutputStream());

            socketin = new DataInputStream((socket.getInputStream()));
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }

        // string to read message from input 
//        String line = "";
    }

    public void close() throws IOException {

        //Terminate the conversation
        out.writeUTF("##Over##");

// close the connection 
        try {
            //    input.close();
            out.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public double[] getSentenceEmbeddings(String sentence) throws IOException {

        out.writeUTF(sentence);
        int size = readInt(socketin);
        double[][] embedding = readArray(socketin, size);
        //the embedding have just one row

        return embedding[0];  //return the embeddings of the senetnce from the first row of the embedding. 

    }

    public double[] getSentencePropPairSimilarity(String sentence, String prop) throws IOException {

        String message = sentence + "\t" + prop;
        out.writeUTF(message);
        int size = readInt(socketin);
        double[][] sim = readArray(socketin, size);

        return sim[0];  //return the different similarities of the senetnce and the property from the first row of the result. 
        //In BertScore, for example, similarity results as as (P,R,F1)
    }

    public double[][] getWordEmbeddings(String sentence) throws IOException {

        out.writeUTF(sentence);
        int size = readInt(socketin);
        double[][] embedding = readArray(socketin, size);

        return embedding;

    }

    public static void main(String args[]) throws IOException {

        PythonConnectionClient client = new PythonConnectionClient("127.0.0.1", 12345);
//    PythonConnectionClient client = new PythonConnectionClient("94.184.90.117", 12345);
        double[] embedding = client.getSentenceEmbeddings("This is a simple sentence.");
        for (int i = 0; i < embedding.length; i++) {
            System.out.print(embedding[i] + " ");
        }
        client.close();

    }

    private int readInt(DataInputStream dis) throws IOException {
        int ch1 = dis.read();
        int ch2 = dis.read();
        int ch3 = dis.read();
        int ch4 = dis.read();

//        if ((ch1 | ch2 |) < 0)
//            throw new EOFException();
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4);
    }

    private double[][] readArray(DataInputStream dis, int size) throws IOException {
        byte[] b = new byte[size];

        dis.readFully(b);
        //dis.read(b, 0, size); //it cause a size problem with large messages

        String message = new String(b, "ISO-8859-1");

//        System.out.println(message);
        String[] rows = message.split("\t");

        int row_count = rows.length;
        int column_count = rows[0].split(",").length;
//        
        double[][] result = new double[row_count][column_count];

        for (int i = 0; i < rows.length; i++) {
            String[] elements = rows[i].split(",");
            for (int j = 0; j < elements.length; j++) {
                result[i][j] = Double.parseDouble(elements[j]);
            }
        }
        return result;

    }
}
