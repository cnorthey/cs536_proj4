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

class SymbolInfo extends Symb {
 public ASTNode.Kinds kind; // Should always be Var in CSX-lite
 public ASTNode.Types type; // Should always be Integer or Boolean in CSX-lite
 public int arraySize;      // -1 if not array, update via setArraySize()

 public SymbolInfo(String id, ASTNode.Kinds k, ASTNode.Types t){    
	super(id);
	kind = k; type = t;
  arraySize = -1;
 };

 public void setArraySize(int i){
  this.arraySize = i;
 };

 public String toString(){
             return "("+name()+": kind=" + kind+ ", type="+  type+")";
 };
}

