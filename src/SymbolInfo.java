/* CS 536: PROJECT 3 - CSX TYPE CHECKER
 * 
 * Caela Northey (cs login: caela)	905 653 2238 
 * Alan Irish    (cs login: irish)  906 591 2819
 *
 * DUE DATE: FRIDAY NOV 22, 2013
 *
 ***************************************************
 *  class used to hold information associated w/
 *  Symbs (which are stored in SymbolTables)
 *  Update to handle arrays and methods
 * 
 ****************************************************/

import java.util.ArrayList;

class SymbolInfo extends Symb {
	public ASTNode.Kinds kind; // Should always be Var in CSX-lite
	public ASTNode.Types type; // Should always be Integer or Boolean in CSX-lite
	private ArrayList<ArrayList<argDeclNode>> parameters; //List of parameters, used by methods
	int arraySize;
	private exprNode[] elements;   //List of elements, used by arrays

	public SymbolInfo(String id, ASTNode.Kinds k, ASTNode.Types t){    
		super(id);
		kind = k; type = t;
		parameters = new ArrayList<ArrayList<argDeclNode>>();
		arraySize = 0;
	};
	
	public void setArraysize(int size){
		arraySize = size;
	}

	public void addMethodParms(ArrayList<argDeclNode> parms){
		parameters.add(parms);
	}

	public void addElement(int index, exprNode e){
		elements[index] = e;
	}
	
	public exprNode getElement(int index){
		return elements[index];
	}

	//This function compares a given list of parameters to see if they
	//are different than the accepted lists of parameters, allowing overloading
	public boolean containsParms(ArrayList<argDeclNode> parms){
		boolean duplicate = false;
		// For every set of parameters
		for(int i = 0; i < parameters.size(); i++)
		{
			//Check the length of parameters first
			if(parms.size() == parameters.get(i).size())
			{
				duplicate = true;
				//For every parameter in the lists of parameters
				for(int j = 0; j < parameters.get(i).size(); j++)
				{
					argDeclNode newParms = parms.get(j);
					argDeclNode oldParms = parameters.get(i).get(j);
						
					//Check if the parameters are the same kind
					if(newParms instanceof valArgDeclNode && 
							oldParms instanceof valArgDeclNode)
					{
						//Then check if the parameters are the same type
						if(((valArgDeclNode)newParms).argType.type !=
								((valArgDeclNode)oldParms).argName.type)
						{
							//If different, then this is not a duplicate list
							duplicate = false;
							break;
						}
					} else if(newParms instanceof arrayArgDeclNode &&
							oldParms instanceof arrayArgDeclNode)
					{
						if(((arrayArgDeclNode)newParms).elementType.type !=
								((arrayArgDeclNode)oldParms).elementType.type)
						{
							duplicate = false;
							break;
						}
					} else {
						duplicate = false;
						break;
					}
				}
				if(duplicate)
					return duplicate;
			}
		}
		return false;
	}


	// public SymbolInfo(String id, int k, int t){
	//	super(id);
	//	kind = new Kinds(k); type = new Types(t);};
	public String toString(){
		return "("+name()+": kind=" + kind+ ", type="+  type+")";};

}
