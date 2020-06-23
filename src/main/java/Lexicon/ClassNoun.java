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
public class ClassNoun {
    public String noun;
    public String ref;
    public double score = 1.0;

    @Override
    public String toString() {
        return "ClassNoun{" + "noun=" + noun + ", ref=" + ref + ", score=" + score + '}';
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method.
        return ref.equals(((ClassNoun)obj).ref); //but not noun
    }
    
    
    public String getNoun() {
        return noun;
    }

    public void setNoun(String noun) {
        this.noun = noun;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public ClassNoun(String noun, String ref, double score) {
        this.noun = noun;
        this.ref = ref;
        this.score = score;
    }
}
