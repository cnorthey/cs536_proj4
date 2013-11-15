/* CS 536 PROJECT 1 - IDENTIFIER CROSS-REFERENCE ANALYSIS
 *
 * Caela Northey (905 653 2238)
 * DUE: 9/18/2013
 *
 * (I am not the original author of this code, see README)
 */

/*************************************************************
 * 
 * After scanning  and parsing, the structure and content of a program is represented as an
 * Abstract Syntax Tree (AST).
 * The root of the AST represents the entire program. Subtrees represent various
 * components, like declarations and statements.
 * Program translation and analysis is done by recursively walking the AST, starting
 * at the root.
 * CSX will use a variety of AST nodes since it contains a variety of structures (declarations,
 * methods, statements, expressions, etc.).
 * The AST nodes defined here represent CSX Lite, a small subset of CSX. Hence many fewer
 * nodes are needed.
 * 
 * The analysis implemented here counts the number of identifier declarations and uses
 * on a per scope basis. The entire program is one scope. A block (rooted by a blockNode)
 * is a local scope (delimited in the source program by a "{" and "}").
 * The method countDeclsAndUses implements this analysis. Each AST node has a definition of this
 * method. It may be an explicit definition intended especially for one particular node.
 * If an AST node has no local definition of countDeclsAndUses it inherits a definition from its
 * parent class. The class ASTNode (which is the ancestor of all AST nodes) has a default definition
 * of countDeclsAndUses. The definition is null (does nothing).
 *
 */
import java.util.*;
// abstract superclass; only subclasses are actually created
abstract class ASTNode {

	public final int 	linenum;
	public final int	colnum;
	
	ASTNode(){linenum=-1;colnum=-1;}
	ASTNode(int l,int c){linenum=l;colnum=c;}
	boolean   isNull(){return false;}; // Is this node null?

    	abstract void accept(Visitor v, int indent);// Will be defined in sub-classes    

	// default action on an AST node is to record no declarations and no identifier uses
	 void countDeclsAndUses(ScopeInfo currentScope){ 
		return;
	}

	//default action
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		return;
	}

};

// This node is used to root only CSXlite programs
class csxLiteNode extends ASTNode {
	
  public final fieldDeclsOption	                  progDecls;
	public final stmtsOption 	                      progStmts;
	private ScopeInfo  		                          scopeList;
	private ArrayList<IdentInfo>                    idList; //track identifier use info
	private ArrayList<Hashtable<String, IdentInfo>> idHash; //track identifier scope
	
	csxLiteNode(fieldDeclsOption decls, stmtsOption stmts, int line, int col){      
		super(line,col);
		progDecls=decls;
		progStmts=stmts;
		scopeList=null;
		idList = null;
		idHash= null;
	}; 
	
	
	void accept(Visitor u, int indent){ u.visit(this,indent); }
	
	// This method begins the count declarations and uses analysis.
	//  It first creates a ScopeInfo node for the entire program.
	//  It then passes this ScopeInfo node to the declarations subtree and then
	//   the statements subtree. Visiting these two subtrees causes all identifier uses and
	//     declarations to be recognized and recorded in the list rooted by the ScopeInfo node.
	//  Finally, the information stored in the ScopeInfo list is converted to string form
	//   and returned to the caller of the analysis.
	 String countDeclsAndUses(){
		 scopeList = new ScopeInfo(1,linenum);
		 progDecls.countDeclsAndUses(scopeList);
		 progStmts.countDeclsAndUses(scopeList);
		 return scopeList.toString();
	 }

	// This method begins the cross reference analysis. It first creates our
	// two data structures, an ArrayList of IdentInfo objs, and one of Hashtable
	// of IdentInfo objs. They will both initially be empty until variable
	// declarations are found. 
	String buildCrossReferences(){

		idList = new ArrayList<IdentInfo>();
		idHash = new ArrayList<Hashtable<String,IdentInfo>>();

		//create first, global Hashtable and add to ArrayList of tables
		Hashtable<String,IdentInfo> h = new Hashtable<String,IdentInfo>();
		idHash.add(h);

		//then, build data structures
		progDecls.buildCrossReferences(idList, idHash);
		progStmts.buildCrossReferences(idList, idHash);

		//prepare output string according to specifications:
		//  linenumber: identifier(type): use1, use2,...
		//
		//  ex. 1. a(int): 4,4
		//      2. b(bool): 3
		String output = "\n";
		for(int i = 0; i < idList.size(); i++){
			output += idList.get(i).toString();
		}

		return output;
	}

};

// *****************************************************************************
abstract class fieldDeclsOption extends ASTNode{
	fieldDeclsOption(int line,int column){
		super(line,column);
	}
	fieldDeclsOption(){ super(); }
};

// *****************************************************************************
class fieldDeclsNode extends fieldDeclsOption {

	public final declNode		thisField;
	public final fieldDeclsOption 	moreFields;
	
	fieldDeclsNode(declNode d, fieldDeclsOption f, int line, int col){
		super(line,col);
		thisField=d;
		moreFields=f;
	}
	
	static nullFieldDeclsNode NULL = new nullFieldDeclsNode();

	void accept(Visitor u, int indent){ u.visit(this,indent);}

	void countDeclsAndUses(ScopeInfo currentScope){
		thisField.countDeclsAndUses(currentScope);
		moreFields.countDeclsAndUses(currentScope);
		return;
	}

	//continue AST traversal
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		thisField.buildCrossReferences(idList, idHash);
		moreFields.buildCrossReferences(idList, idHash);
		return;
	}

};

// *****************************************************************************
class nullFieldDeclsNode extends fieldDeclsOption {
	
	nullFieldDeclsNode(){};

	boolean   isNull(){return true;};

	void accept(Visitor u, int indent){ u.visit(this,indent);}

	void countDeclsAndUses(ScopeInfo currentScope){
			return;
		}

	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		return;
	}
};

// *****************************************************************************
// abstract superclass; only subclasses are actually created
abstract class declNode extends ASTNode {
	declNode(){super();};
	declNode(int l,int c){super(l,c);};
};

// *****************************************************************************
class varDeclNode extends declNode { 
	
	public final	identNode	varName;
	public final	typeNode 	varType;
	public final	exprOption 	initValue;
	
	varDeclNode(identNode id, typeNode t, exprOption e,
			int line, int col){
		super(line,col);
		varName=id;
		varType=t;
		initValue=e;
	}
	
	void accept(Visitor u, int indent){ u.visit(this,indent);}

	// This node represents a variable declaration, so we increment the declarations
	//  count by 1
	void countDeclsAndUses(ScopeInfo currentScope){
		currentScope.declsCount+=1;
	}
	
	// This node represents a variable declaration, so we add to the
	// ArrayList idList. id is a new IdentInfo object with the following
	// fields:
	//          name: from this node's first child, identNode varName
	//          type: from this node's second child, typeNode varType
	//          line: from final int linenum from all AST nodes
	//
	// We also add id to the ArrayList of Hashtables, idHash, keyed by
	// the name of the ident and into the last element in the ArrayList
	// (ie, inner most scope currently in)
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		String n = varName.idname;		
		String t = "int";
		if(varType instanceof boolTypeNode){ //only two types, int and bool
			t = "bool";
		}
		IdentInfo id = new IdentInfo(n, t, this.linenum);
		idList.add(id);

		idHash.get(idHash.size()-1).put(n, id);
		return;
	}

};

// *****************************************************************************
abstract class typeNode extends ASTNode {
// abstract superclass; only subclasses are actually created
	typeNode(){super();};
	typeNode(int l,int c){super(l,c);};
	static nullTypeNode NULL = new nullTypeNode();
};

// *****************************************************************************
class nullTypeNode extends typeNode {

	nullTypeNode(){};

	boolean   isNull(){return true;};

	void accept(Visitor u, int indent){ u.visit(this,indent); }
};

// *****************************************************************************
class intTypeNode extends typeNode {
	intTypeNode(int line, int col){
		super(line,col);
	}

	void accept(Visitor u, int indent){ u.visit(this,indent); }
};

// *****************************************************************************
class boolTypeNode extends typeNode {
	boolTypeNode(int line, int col){
		super(line,col);
	}

	void accept(Visitor u, int indent){ u.visit(this,indent); }
};

// *****************************************************************************
//abstract superclass; only subclasses are actually created
abstract class stmtOption extends ASTNode {
	stmtOption(){super();};
	stmtOption(int l,int c){super(l,c);};
	//static nullStmtNode NULL = new nullStmtNode();
};

// *****************************************************************************
// abstract superclass; only subclasses are actually created
abstract class stmtNode extends stmtOption {
	stmtNode(){super();};
	stmtNode(int l,int c){super(l,c);};
	static nullStmtNode NULL = new nullStmtNode();
};

// *****************************************************************************
class nullStmtNode extends stmtOption {
	nullStmtNode(){};
	boolean   isNull(){return true;};
	void accept(Visitor u, int indent){ u.visit(this,indent);}
	void countDeclsAndUses(ScopeInfo currentScope){return;}
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		return;
	}
};

// *****************************************************************************
abstract class stmtsOption extends ASTNode{
	stmtsOption(int line,int column){
		super(line,column);
	}
	stmtsOption(){ super(); }
};

// *****************************************************************************
class stmtsNode extends stmtsOption { 
	public final stmtNode	    	thisStmt;
	public final stmtsOption 	moreStmts;

	stmtsNode(stmtNode stmt, stmtsOption stmts, int line, int col){
		super(line,col);
		thisStmt=stmt;
		moreStmts=stmts;
	};

	static nullStmtsNode NULL = new nullStmtsNode();
	
	void accept(Visitor u, int indent){ u.visit(this,indent);}

	void countDeclsAndUses(ScopeInfo currentScope){
	 // Count decls and uses in both subtrees:
		 thisStmt.countDeclsAndUses(currentScope);
		 moreStmts.countDeclsAndUses(currentScope);
		}

	//continue AST traversal
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		thisStmt.buildCrossReferences(idList, idHash);
		moreStmts.buildCrossReferences(idList, idHash);

		return;
	}
};

// *****************************************************************************
class nullStmtsNode extends stmtsOption {
	nullStmtsNode(){};
	boolean   isNull(){return true;};

	void accept(Visitor u, int indent){ u.visit(this,indent);}
	
	void countDeclsAndUses(ScopeInfo currentScope){return;}

	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		return;
	}
};

// *****************************************************************************
class asgNode extends stmtNode {    

	public final identNode	target;
	public final exprNode 	source;
	
	asgNode(identNode n, exprNode e, int line, int col){       
		super(line,col);
		target=n;
		source=e;
	};
	
	void accept(Visitor u, int indent){ u.visit(this,indent);}

	void countDeclsAndUses(ScopeInfo currentScope){
		// The target of the assign counts as 1 use
		currentScope.usesCount +=1;
		// Visit the source expression to include the identifiers in it
		source.countDeclsAndUses(currentScope);
		}

	//find ident used in idHash and update uses of that var in idList.
	//
	//we search for this var in idHash from back to front b/c
	//the end of the ArrayList represents the inner most scope.
	//this way, if we have variables of the same name that exist
	//in different scope, then we correcly update the "closest" one
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		String name = target.idname;
		for(int i = idHash.size()-1; i > -1; i--){
			//if indent was declared in this scope
			if(idHash.get(i)!=null && idHash.get(i).get(name)!= null){
				//increment uses count
				idHash.get(i).get(name).addUse(this.linenum);
				break;
			}
			//else, try next outer scope
		}
		//note that this will always find an ident b/c only valid
		//programs will be considered, so don't need to worry about 
		//usages of undeclared vars
		
		//continue traversing AST
		source.buildCrossReferences(idList, idHash);
		return;
	}
};

// *****************************************************************************
class ifThenNode extends stmtNode {
	
	public final exprNode 		condition;
	public final stmtNode 		thenPart;
	public final stmtOption 	elsePart;
	
	ifThenNode(exprNode e, stmtNode s1, stmtOption s2, int line, int col){
		super(line,col);
		condition=e;
		thenPart=s1;
		elsePart=s2;
	};
	
	void accept(Visitor u, int indent){ u.visit(this,indent);}

	void countDeclsAndUses(ScopeInfo currentScope){
		// Count identifier uses in control expression and then statement.
		// In CSX Lite the else statement is always null
		condition.countDeclsAndUses(currentScope);
		thenPart.countDeclsAndUses(currentScope);
		}

	//continue AST traversal
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		condition.buildCrossReferences(idList, idHash);
		thenPart.buildCrossReferences(idList, idHash);
		return;
	}
};

// *****************************************************************************
//"Whenever a block is processed (a blockNode) a new hash table is allocated
// and linked to the head of a list of hash tables, one for each nested scope.
// When the AST for the block is fully processed, the hash table for its
// scope is removed (since the block’s local declarations are no longer visible)."
class blockNode extends stmtNode {
	
	public final fieldDeclsOption 	decls;  
	public final stmtsOption 	stmts;
	
	blockNode(fieldDeclsOption f, stmtsOption s, int line, int col){
		super(line,col);
		decls=f;
		stmts=s;
	}
	
	 void accept(Visitor u, int indent){ u.visit(this,indent);}

	 void countDeclsAndUses(ScopeInfo currentScope){
		/* A block opens a new scope, so a new ScopeInfo node is created.
		   It is appended to the end of the ScopeInfo list.
		   The new scope is used to record local declarations and uses in the block
		*/ 
		 ScopeInfo  localScope = new ScopeInfo(linenum);
		 ScopeInfo.append(currentScope,localScope);
		 decls.countDeclsAndUses(localScope);
		 stmts.countDeclsAndUses(localScope);
	}

	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		//add new node to idHash
		Hashtable<String,IdentInfo> h = new Hashtable<String,IdentInfo>();
		idHash.add(h);
		//continue AST traversal
		decls.buildCrossReferences(idList, idHash);
		stmts.buildCrossReferences(idList, idHash);
		//now, blockNode fully processed, so we can de-link this "scope"
		idHash.remove(h);
		return;
	}

};

// *****************************************************************************
//abstract superclass; only subclasses are actually created
abstract class exprOption extends ASTNode {
	exprOption(){super();};
	exprOption(int l,int c){super(l,c);};
	//static nullStmtNode NULL = new nullStmtNode();
};

// *****************************************************************************
// abstract superclass; only subclasses are actually created
abstract class exprNode extends exprOption {
	exprNode(){super();};
	exprNode(int l,int c){super(l,c);};
	static nullExprNode NULL = new nullExprNode();
};

// *****************************************************************************
class nullExprNode extends exprOption {
	nullExprNode(){super();};
	boolean   isNull(){return true;};
	void accept(Visitor u, int indent){}
};

// *****************************************************************************
class binaryOpNode extends exprNode {
	
	public final exprNode 	leftOperand;
	public final exprNode 	rightOperand;
	public final int	operatorCode; // Token code of the operator
	
	binaryOpNode(exprNode e1, int op, exprNode e2, int line, int col){
		super(line,col);
		operatorCode=op;
		leftOperand=e1;
		rightOperand=e2;
	};

	void accept(Visitor u, int indent){ u.visit(this,indent);}

	 // Count identifier uses in left and right operands
	 void countDeclsAndUses(ScopeInfo currentScope){
			leftOperand.countDeclsAndUses(currentScope);
			rightOperand.countDeclsAndUses(currentScope);
		}

	//continue AST traversal
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		leftOperand.buildCrossReferences(idList, idHash);
		rightOperand.buildCrossReferences(idList, idHash);
		return;
	}
};

// *****************************************************************************
class identNode extends exprNode {
	
	public final String 	idname;
	
	identNode(String identname, int line, int col){
		super(line,col);
		idname   = identname;
	};

	void accept(Visitor u, int indent){ u.visit(this,indent);}

	//One identifier used here:
	void countDeclsAndUses(ScopeInfo currentScope){
			currentScope.usesCount+=1;
		}

	//find ident used in idHash and update uses of that var in idList
	void buildCrossReferences(ArrayList<IdentInfo> idList,
                            ArrayList<Hashtable<String, IdentInfo>> idHash){
		for(int i = idHash.size()-1; i > -1; i--){
			//if indent was declared in this scope
			if(idHash.get(i)!=null && idHash.get(i).get(idname)!= null){
				//increment uses count
				idHash.get(i).get(idname).addUse(this.linenum);
				break;
			}
			//else, try next outer scope
		}
		//note that this will always find an ident b/c only valid
		//programs will be considered, so don't need to worry about 
		//usages of undeclared vars

		return;
	}
};

// *****************************************************************************
class intLitNode extends exprNode {
	public final int 	intval;
	intLitNode(int val, int line, int col){
		super(line,col);
		intval=val;
	}

	void accept(Visitor u, int indent){ u.visit(this,indent);}
};
