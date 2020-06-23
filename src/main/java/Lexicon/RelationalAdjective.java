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
public class RelationalAdjective {
        
    public String verb;

    @Override
    public String toString() {
        return "RelationalAdjective{" + "verb=" + verb + ", ref=" + ref + ", propSubj=" + propSubj + ", propObj=" + propObj + ", score=" + score + '}';
    }
    public String ref;
    public GrammaticalType propSubj;
    public GrammaticalType propObj;
    public double score =1.0;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {  //it needed by the "contain" method.
        return ref.equals(((RelationalAdjective)obj).ref)&&propObj.equals(((RelationalAdjective)obj).propObj)
                &&propSubj.equals(((RelationalAdjective)obj).propSubj);  //but not verb
    }
    
    public GrammaticalType getPropObj() {
        return propObj;
    }

    public void setPropObj(GrammaticalType propObj) {
        this.propObj = propObj;
    }

    public RelationalAdjective(String verb,String ref, GrammaticalType propSubj, GrammaticalType propObj, double score) {
        this.ref = ref;
        this.verb = verb;
        this.propSubj = propSubj;
        this.propObj = propObj;
        this.score = score;
    }

   
   

    public GrammaticalType getPropSubj() {
        return propSubj;
    }

    public void setPropSubj(GrammaticalType propSubj) {
        this.propSubj = propSubj;
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

}
