/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

/**
 *
 * @author Admin
 */
public class VerbPhrase {
    public String verb;
    public String ref;
    public double score = 1.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method.
        return ref.equals(((VerbPhrase)obj).ref);  //but not verb;
    }

    @Override
    public String toString() {
        return "VerbPhrase{" + "verb=" + verb + ", ref=" + ref + ", score=" + score + '}';
    }
    
    
    

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public VerbPhrase(String verb, String ref, double score) {
        this.verb = verb;
        this.ref = ref;
        this.score = score;
    }
}
