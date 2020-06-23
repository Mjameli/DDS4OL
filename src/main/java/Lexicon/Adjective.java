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
public class Adjective {
    public String adj;
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
        return ref.equals(((Adjective)obj).ref); // but not adj.
    }

    @Override
    public String toString() {
        return "Adjective{" + "adj=" + adj + ", ref=" + ref + ", score=" + score + '}';
    }

    

    public String getAdj() {
        return adj;
    }

    public void setAdj(String adj) {
        this.adj = adj;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Adjective(String adj, String ref , double score) {
        this.adj = adj;
        this.ref = ref;
        this.score = score;
    }

    
}
