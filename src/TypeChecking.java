/* CS 536: PROJECT 3 - CSX TYPE CHECKER
 * 
 * Caela Northey (cs login: caela)	905 653 2238 
 * Alan Irish    (cs login: irish) 906 591 2819
 *
 * DUE DATE: FRIDAY NOV 22, 2013
 */

// The following methods type check  AST nodes used in CSX Lite
//  You will need to complete the methods after line 238 to type check the
//   rest of CSX
//  Note that the type checking done for CSX lite may need to be extended to
//   handle full CSX (for example binaryOpNode).

public class TypeChecking extends Visitor { 

//	static int typeErrors =  0;     // Total number of type errors found 
//  	public static SymbolTable st = new SymbolTable(); 	
	int typeErrors;     // Total number of type errors found
	SymbolTable st;
	
	TypeChecking(){
		typeErrors = 0;
		st = new SymbolTable(); 
	}
	
	boolean isTypeCorrect(csxLiteNode n) {
        	this.visit(n);
        	return (typeErrors == 0);
	}
	
	boolean isTypeCorrect(classNode n) {
    	this.visit(n);
    	return (typeErrors == 0);
	}
	
	static void assertCondition(boolean assertion){  
		if (! assertion)
			 throw new RuntimeException();
	}

	void typeMustBe(ASTNode.Types testType,ASTNode.Types requiredType,String errorMsg) {
		if ((testType != ASTNode.Types.Error) && (testType != requiredType)) {
			System.out.println(errorMsg);
			typeErrors++;
    }
  }

	void typesMustBeEqual(ASTNode.Types type1,ASTNode.Types type2,String errorMsg) {
		if ((type1 != ASTNode.Types.Error) && 
        (type2 != ASTNode.Types.Error)&& (type1 != type2)) {
			System.out.println(errorMsg);
			typeErrors++;
		}
  }

	//ie, var, array, scalar param, or array param
	boolean kindIsAssignable(ASTNode.Kinds testKind){
		return (testKind == ASTNode.Kinds.Var)||
           (testKind == ASTNode.Kinds.Array)||
           (testKind == ASTNode.Kinds.ScalarParm)||
           (testKind == ASTNode.Kinds.ArrayParm);
	}

	//ie, Var, Value, ScalarParm
	boolean isScalar(ASTNode.Kinds testKind){
		return (testKind == ASTNode.Kinds.ScalarParm)||
           (testKind == ASTNode.Kinds.Value)||
           (testKind == ASTNode.Kinds.Var);
	}

	String error(ASTNode n) {
		return "Error (line " + n.linenum + "): ";
  }

	static String opToString(int op) {
		switch (op) {
			case sym.PLUS:
				return(" + ");
			case sym.MINUS:
				return(" - ");
			case sym.EQ:
				return(" == ");
			case sym.NOTEQ:
				return(" != ");
			default:
				assertCondition(false);
				return "";
		}
	}

// Extend this to handle all CSX binary operators
	static void printOp(int op) {
		switch (op) {
			case sym.PLUS:
				System.out.print(" + ");
				break;
			case sym.MINUS:
				System.out.print(" - ");
				break;
			case sym.EQ:
				System.out.print(" == ");
				break;
			case sym.NOTEQ:
				System.out.print(" != ");
				break;
			default:
				throw new Error();
		}
	}

	void visit(csxLiteNode n){
		this.visit(n.progDecls);
		this.visit(n.progStmts);
	}
	
	void visit(fieldDeclsNode n){
			this.visit(n.thisField);
			this.visit(n.moreFields);
	}
	void visit(nullFieldDeclsNode n){}

	void visit(stmtsNode n){
		  //System.out.println ("In stmtsNode\n");
		  this.visit(n.thisStmt);
		  this.visit(n.moreStmts);

	}
	void visit(nullStmtsNode n){}

// Extend varDeclNode's method to handle initialization
	void visit(varDeclNode n){

		SymbolInfo id;
 		// id = (SymbolInfo) st.localLookup(n.varName.idname);
    id = (SymbolInfo) st.localLookup(n.varName.idname);
    if (id != null) {
			System.out.println(error(n) + id.name()+ " is alreintliady declared.");
      typeErrors++;
      n.varName.type = ASTNode.Types.Error;
     } else {
       id = new SymbolInfo(n.varName.idname, ASTNode.Kinds.Var, n.varType.type);
       n.varName.type = n.varType.type;

				try {
        	st.insert(id);
				} catch (DuplicateException d) 
        { /* can't happen */ }
			  	catch (EmptySTException e) 
        { /* can't happen */ }
        n.varName.idinfo=id;
     }

	};
	
	void visit(nullTypeNode n){}
	
	void visit(intTypeNode n){
		//no type checking needed}
	}
	void visit(boolTypeNode n){
		//no type checking needed}
	}

	// 1) lookup identNode.idname in symbol table; error if absent
	// 2) copy symbol table's type and kind info into identNode   
	// 3) store a link to the symbol table entry in the identNode 
	void visit(identNode n){
		SymbolInfo    id;
		//In CSX-lite all IDs should be vars!
		// (not true for CSX, can be var or method or lable)
    //assertCondition(n.kind == ASTNode.Kinds.Var);
    id =  (SymbolInfo) st.globalLookup(n.idname);
    if (id == null) {
      System.out.println(error(n) +  n.idname + " is not declared.");
      typeErrors++;
      n.type = ASTNode.Types.Error;
    } else {
      n.type = id.type; 
			n.kind = id.kind;
      n.idinfo = id; // Save ptr to correct symbol table entry
    }
	}

	// Extend nameNode's method to handle subscripts
	// 1) type check identNode
	// 2) if subscriptVal is a null node, copy identNode's type and
  //    and kind values into nameNode and return
	// 3) type check subscriptVal
	// 4) check that identNode's kind is an array
	// 5) check that subscriptVals's kind is scalar and type is int or char
	// 6) set nameNode's type to the idendNode's type and the
  //    nameNode's kind to Variable
	void visit(nameNode n){
		this.visit(n.varName);    // step 1
		if(n.subscriptVal.isNull()){ // step 2
			n.type = n.varName.type;
			n.kind = n.varName.kind;
			return;
		}
		this.visit(n.subscriptVal);  // step 3
    assertCondition(n.varName.kind == ASTNode.Kinds.Array); // step 4
		exprNode temp = (exprNode)n.subscriptVal;
    assertCondition(isScalar(temp.kind)); // step 5
    assertCondition((temp.type == ASTNode.Types.Integer) ||
                    (temp.type == ASTNode.Types.Character));
    n.type=n.varName.type;    // step 6

	}

	// 1,2) type check nameNode and expression tree
	// 3) check that nameNode' kind is assignable (var, array, scalar
  //    param, or array param)
	// 4) if nameNode's kind is scalar then check expr tree's kind is also
  //    scalar and that booth have same type
	// 5) if nameNode and expr tree's kinds are both arrays and both have
	//    same type, check that length same; then return
	// 6) if nameNode's kind is array and type is char, and expr tree's kind
	//    is string, check that both have same length; then return
	// 7) otherwise, expr may not be assigned to name node
	// other notes: can't asign to const var
	void visit(asgNode n){

		this.visit(n.target); // step 1
		this.visit(n.source); // step 2
		assertCondition(kindIsAssignable(n.target.kind)); // step 3
		if(isScalar(n.target.kind)){ // step 4
			assertCondition(isScalar(n.source.kind));
			typesMustBeEqual(n.source.type, n.target.type,
        error(n) + "Both the left and right hand sides of an assignment must"
                 + " have the same type.");
		}
		assertCondition(n.target.varName.kind != ASTNode.Kinds.Value);
		if((n.target.varName.kind == ASTNode.Kinds.Array) && // step 5
       (n.source.kind == ASTNode.Kinds.Array) &&
       (n.target.varName.type == n.source.type)){
			//look up target and source to get array info
			SymbolInfo id_s = (SymbolInfo)st.globalLookup(n.target.varName.idname);
			nameNode temp = (nameNode)n.target; //know is array => nameNode
			SymbolInfo id_t = (SymbolInfo)st.globalLookup(temp.varName.idname);
			assertCondition(id_s.arraySize == id_t.arraySize);
			return;			
		}
		if(n.target.kind == ASTNode.Kinds.Array && // step 6
			n.target.type == ASTNode.Types.Character &&
      n.source.kind == ASTNode.Kinds.String){
			SymbolInfo id_s = (SymbolInfo)st.globalLookup(n.target.varName.idname);
			strLitNode temp2 = (strLitNode)n.source; //know is string => strLitNode
			assertCondition(id_s.arraySize == temp2.strval.length());
			return;
		}

		assertCondition(false); // step 7
	}

	// Extend ifThenNode's method to handle else parts
	void visit(ifThenNode n){
		this.visit(n.condition);
    typeMustBe(n.condition.type, ASTNode.Types.Boolean,
               error(n) + "The control expression of an if must be a bool.");
    this.visit(n.thenPart);
		this.visit(n.elsePart);
	}

	//can print int, bool and chars values + char arrays and sting lits
	void visit(printNode n){
		this.visit(n.outputValue);
    assertCondition((n.outputValue.kind == ASTNode.Kinds.Value &&
                    (n.outputValue.type == ASTNode.Types.Integer ||
                     n.outputValue.type == ASTNode.Types.Boolean ||
                     n.outputValue.type == ASTNode.Types.Character )) ||
                    (n.outputValue.type == ASTNode.Types.Character &&
                     n.outputValue.kind == ASTNode.Kinds.Array ) ||
                    (n.outputValue.kind == ASTNode.Kinds.String));
	}
	  
	void visit(blockNode n){
	  // open a new local scope for the block body
		st.openScope();
		this.visit(n.decls);
		this.visit(n.stmts);
		// close this block's local scope
		try { st.closeScope();
		}  catch (EmptySTException e) 
	    { /* can't happen */ }
	}

	  void visit(binaryOpNode n){
		  
		//Only four bin ops in CSX-lite
		assertCondition(n.operatorCode== sym.PLUS||n.operatorCode==sym.MINUS 
        			|| n.operatorCode== sym.EQ||n.operatorCode==sym.NOTEQ);
		this.visit(n.leftOperand);
		this.visit(n.rightOperand);
        	if (n.operatorCode== sym.PLUS||n.operatorCode==sym.MINUS){
        		n.type = ASTNode.Types.Integer;
        		typeMustBe(n.leftOperand.type, ASTNode.Types.Integer,
                	error(n) + "Left operand of" + opToString(n.operatorCode) 
                         	+  "must be an int.");
        		typeMustBe(n.rightOperand.type, ASTNode.Types.Integer,
                	error(n) + "Right operand of" + opToString(n.operatorCode) 
                         	+  "must be an int.");
        	} else { // Must be a comparison operator
        		n.type = ASTNode.Types.Boolean;
        		String errorMsg = error(n)+"Both operands of"+
                           opToString(n.operatorCode)+"must have the same type.";
        		typesMustBeEqual(n.leftOperand.type,n.rightOperand.type,errorMsg);
        		
        	}
	  }

	void visit(intLitNode n){
	//      All intLits are automatically type-correct
	}
 
//*** Extend these unparsing methods to correctly unparse CSX AST nodes ***
	 
	 void visit(classNode n){
		System.out.println("Type checking for classNode not yet implemented");
		}

	 void  visit(memberDeclsNode n){
		System.out.println("Type checking for memberDeclsNode not yet implemented");
	 }
	 
	 void  visit(methodDeclsNode n){
		System.out.println("Type checking for methodDeclsNode not yet implemented");
		 }
	 
	 void visit(nullStmtNode n){}
	 
	 void visit(nullReadNode n){}

	 void visit(nullPrintNode n){}

	 void visit(nullExprNode n){}

	 void visit(nullMethodDeclsNode n){}

	 void visit(methodDeclNode n){
		System.out.println("Type checking for methodDeclNode not yet implemented");
	 }

	// only vars(including params) of type int or char may be ++/--
	void visit(incrementNode n){
		assertCondition((n.target.kind == ASTNode.Kinds.Var ||
                    n.target.kind == ASTNode.Kinds.ScalarParm ||
                    n.target.kind == ASTNode.Kinds.ArrayParm) &&
                    (n.target.type == ASTNode.Types.Character ||
                    n.target.type == ASTNode.Types.Integer));
	}

	void visit(decrementNode n){
		assertCondition((n.target.kind == ASTNode.Kinds.Var ||
                    n.target.kind == ASTNode.Kinds.ScalarParm ||
                    n.target.kind == ASTNode.Kinds.ArrayParm) &&
                    (n.target.type == ASTNode.Types.Character ||
                    n.target.type == ASTNode.Types.Integer));
	}

	void visit(argDeclsNode n){
		System.out.println("Type checking for argDeclsNode not yet implemented");
	}

	void visit(nullArgDeclsNode n){}

	
	void visit(valArgDeclNode n){
		System.out.println("Type checking for valArgDeclNode not yet implemented");
	}
	
	void visit(arrayArgDeclNode n){
		System.out.println("Type checking for arrayArgDeclNode not yet implemented");
	}
	
	void visit(constDeclNode n){
		System.out.println("Type checking for constDeclNode not yet implemented");
	 }
	 
	 void visit(arrayDeclNode n){
		System.out.println("Type checking for arrayDeclNode not yet implemented");
	 }
	
	void visit(charTypeNode n){
		System.out.println("Type checking for charTypeNode not yet implemented");
	}
	void visit(voidTypeNode n){
		System.out.println("Type checking for voidTypeNode not yet implemented");
	}

	// 1) check condition (expr tree)
	// 2) check condition's type is boolean and kind is scalar
	// 3) if label is null, then type check stmtNode (loop body); return
	// 4) if there is a label (identNode):
	//		a) check lable is not already present in sym table
	//    b) if isn't enter label in sym table with kind = VisibleLabel
	//       and type = void
	//    c) type check stmtNode
	//    d) change label's kind in sym table to HiddenLabel
	void visit(whileNode n){
		this.visit(n.condition); // step 1
		assertCondition(n.condition.type == ASTNode.Types.Boolean && //step 2
                    isScalar(n.condition.kind));
		if(n.label.isNull()){ //step 3
			this.visit(n.loopBody);
			return;
		} else { //step 4
			nameNode temp = (nameNode)n.label;
    	SymbolInfo id = (SymbolInfo) st.localLookup(temp.varName.idname); // 4a
    	if (id != null) {
				System.out.println(error(n) + id.name()+ " is already declared.");
      	typeErrors++;
      	temp.varName.type = ASTNode.Types.Error;
			}else { // 4b
       id = new SymbolInfo(temp.varName.idname, ASTNode.Kinds.VisibleLabel,
                           ASTNode.Types.Void);
       try {
        	st.insert(id);
				} catch (DuplicateException d) 
        { /* can't happen, already checked */ }
			  	catch (EmptySTException e) 
        { /* can't happen */ }
			}
			this.visit(n.loopBody); // 4c
			id.kind = ASTNode.Kinds.HiddenLabel; //4d
		}

	}

	// 1) check that identNode is declared in sym table
	// 2) check that identNode's kind is VisibleLable (error if hidden)
	void visit(breakNode n){
		SymbolInfo id;
   	id = (SymbolInfo) st.localLookup(n.label.idname);
    if (id == null) {
			System.out.println(error(n) + n.label.idname + " isn't declared.");
      typeErrors++;
		}
		assertCondition(n.label.kind == ASTNode.Kinds.VisibleLabel);
	}

	void visit(continueNode n){
		SymbolInfo id;
   	id = (SymbolInfo) st.localLookup(n.label.idname);
    if (id == null) {
			System.out.println(error(n) + n.label.idname + " isn't declared.");
      typeErrors++;
		}
		assertCondition(n.label.kind == ASTNode.Kinds.VisibleLabel);
	}

	// 1) check that identNode.idname is declared in sym table with type
	//    void and kind = Method
	// 2) type check args subtree
	// 3) build a list of the expre nodes found in args subtree
	// 4) get list of param symbols declared for method (stored in method's
	//    symbol table entry)
	// 5) check arg list and param symbols list both have same length
	// 6) compare each arg node w corresponding param symbol:
	//    a) both same type
	//    b) variable, value, or scalarparam kind in an arg matches a 
	//       scalarparam parm; an array or arrayparam kind in an argument
	//       node matches an arrayparm param
	void visit(callNode n){
		SymbolInfo id;
    id = (SymbolInfo) st.localLookup(n.methodName.idname); // step 1
   	if (id == null) {
			System.out.println(error(n) + n.methodName.idname+"()"+ " isn't declared.");
     	typeErrors++;
     	//n.methodName.type = ASTNode.Types.Error;
		}
		assertCondition(id.type == ASTNode.Types.Void && id.kind == ASTNode.Kinds.Method);
		this.visit(n.args); // step 2
		

	}

	  
	void visit(readNode n){
		System.out.println("Type checking for readNode not yet implemented");
	}
	  

	// 1) if returnVal is null, check that currentMethod.returnType = void
	// 2) if returnVal is not null, check that returnVal's kind is scalar
	//    and its type is currentMethod.returnType
	void visit(returnNode n){
		if(n.returnVal.isNull()){ // step 1
			//assertCondition(currentMethod.returnType == ASTNode.Types.Void);
		} else { //step 2
			//assertCondition(isScalar(n.returnVal.kind) &&
                      //(n.returnVal.type == currentMethod.returnType));
		}
	}
	  
	  void visit(argsNode n){
		System.out.println("Type checking for argsNode not yet implemented");
	  }
	  	
	  void visit(nullArgsNode n){}
		
	  void visit(castNode n){
		System.out.println("Type checking for castNode not yet implemented");
	  }

	  void visit(fctCallNode n){
		System.out.println("Type checking for fctCallNode not yet implemented");
	  }

	  void visit(unaryOpNode n){
		System.out.println("Type checking for unaryOpNode not yet implemented");
	  }

	
	void visit(charLitNode n){
		//automatically type correct like intLitNode?
	}
	  
	void visit(strLitNode n){
		//automatically type correct like intLitNode?
	}

	void visit(trueNode n){
		//automatically type correct?
	}

	void visit(falseNode n){
		//automatically type correct?
	}


}
