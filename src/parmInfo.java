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
		
		if(this.type != p.type)
			return false;
		
		if(this.kind == ASTNode.Kinds.ArrayParm ||
		   this.kind == ASTNode.Kinds.Array ||
		   this.kind == ASTNode.Kinds.String)
			return (p.kind == ASTNode.Kinds.Array || 
					 p.kind == ASTNode.Kinds.ArrayParm ||
					 p.kind == ASTNode.Kinds.String);
		
		if(this.kind == ASTNode.Kinds.ScalarParm ||
		   this.kind == ASTNode.Kinds.Value ||
		   this.kind == ASTNode.Kinds.Var)
			return (p.kind == ASTNode.Kinds.ScalarParm || 
					 p.kind == ASTNode.Kinds.Value ||
					 p.kind == ASTNode.Kinds.Var);
		
		return false;
	}
	
	public String toString(){
		return "Kind: "+kind+" Type: "+type;
	}
}
