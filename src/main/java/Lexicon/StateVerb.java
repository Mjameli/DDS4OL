/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

import Lexicon.GrammaticalType;

/**
 *
 * @author Admin
 */
public class StateVerb {

   
    

    public String verb;
    public String ref;
    public GrammaticalType propSubj;
    public GrammaticalType propObj;
    public double score = 1.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method.
        return ref.equals(((StateVerb)obj).ref)&&propObj.equals(((StateVerb)obj).propObj)
                &&propSubj.equals(((StateVerb)obj).propSubj); //but not verb
    }
    
    public StateVerb(String verb, String ref, GrammaticalType propSubj, GrammaticalType propObj, double score) {
        this.verb = verb;
        this.ref = ref;
        this.propSubj = propSubj;
        this.propObj = propObj;
        this.score = score;
        
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setPropSubj(GrammaticalType propSubj) {
        this.propSubj = propSubj;
    }

    public void setPropObj(GrammaticalType propObj) {
        this.propObj = propObj;
    }
    
    public String getVerb() {
        return verb;
    }

    public String getRef() {
        return ref;
    }

    public GrammaticalType getPropSubj() {
        return propSubj;
    }

    public GrammaticalType getPropObj() {
        return propObj;
    }
     
    @Override
    public String toString() {
        return "StateVerb{" + "verb=" + verb + ", ref=" + ref + ", propSubj=" + propSubj + ", propObj=" + propObj + ", score=" + score +'}';
    }

    

}
