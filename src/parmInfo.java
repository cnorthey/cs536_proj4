/* CS 536: PROJECT 4 - CSX TYPE CHECKER
 * 
 * Caela Northey (cs login: caela)	905 653 2238 
 * Alan Irish    (cs login: irish)  906 591 2819
 *
 * DUE DATE: FRIDAY NOV 22, 2013
 *
 ***************************************************
 *  class used to hold the type and kind information
 *  of a parameter. Also included is a method to 
 *  compare a parmInfo object to another parmInfo
 *  object, detecting if they are matching
 * 
 ****************************************************/

public class parmInfo {
	ASTNode.Kinds kind;
	ASTNode.Types type;
	
	parmInfo(ASTNode.Kinds k, ASTNode.Types t){
		this.kind = k;
		this.type = t;
	}
	
	public boolean isParmEqual(parmInfo p){
		//Check that types and kinds are equal
		
		return (this.type == p.type && (
				(kind == ASTNode.Kinds.ArrayParm && 
					(p.kind == ASTNode.Kinds.Array || 
					 p.kind == ASTNode.Kinds.ArrayParm ||
					 p.kind == ASTNode.Kinds.String)) ||
				(kind == ASTNode.Kinds.ScalarParm &&
					(p.kind == ASTNode.Kinds.ScalarParm || 
					 p.kind == ASTNode.Kinds.Value ||
					 p.kind == ASTNode.Kinds.Var))));
	}
	
	public String toString(){
		return "Kind: "+kind+" Type: "+type;
	}
}
