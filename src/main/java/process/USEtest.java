/*fsdfsafsffsa
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class USEtest {
        public static void main(String[] args) throws UnsupportedEncodingException {

    SavedModelBundle bundle = SavedModelBundle.load("/media/Volume3/Jabalameli/UniversalSentEmbed/", "serve");
Session sess = bundle.session();
Tensor<String> inputTensor = Tensor.create("Hello World".getBytes("UTF-8"), String.class);
Tensor result = sess.runner().feed("input", inputTensor).fetch("output").run().get(0);
float[] output = new float[512];
result.copyTo(output);
System.out.println(Arrays.toString(output));
    
        }
}
