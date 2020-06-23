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
public enum GrammaticalType {

    DirectObj, Subject, PossessiveAdjunct, CopulativeArg, PrepositionalObject, AdverbialComplement, AdpositionalObject;

    private GrammaticalType() {
    }

    /**
     *
     * @param gt
     * @return
     */
    public static GrammaticalType valueof(String gt) {
        gt = gt.toLowerCase().trim();
        switch (gt) {
            case ("directobj"):
                return GrammaticalType.DirectObj;
            case ("directobject"):
                return GrammaticalType.DirectObj;
            case ("subject"):
                return GrammaticalType.Subject;
            case ("copulativesubject"):
                return GrammaticalType.Subject;

            case ("possessiveadjunct"):
                return GrammaticalType.PossessiveAdjunct;
            case ("copulativearg"):
                return GrammaticalType.CopulativeArg;
            case ("prepositionalobject"):
                return GrammaticalType.PrepositionalObject;
            case ("adverbialcomplement"):
                return GrammaticalType.AdverbialComplement;
            case ("adpositionalobject"):
                   
                return GrammaticalType.AdpositionalObject;
            default:
                System.out.println("There is not a grammatical type:" + gt);
                return null;
        }

    }
}
