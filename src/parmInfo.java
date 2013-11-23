
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
