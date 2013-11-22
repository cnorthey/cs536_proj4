/* CS 536: PROJECT 3 - CSX TYPE CHECKER
 * 
 * Caela Northey (cs login: caela)	905 653 2238 
 * Alan Irish    (cs login: irish)  906 591 2819
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

	void typeMustBe(ASTNode.Types testType,ASTNode.Types requiredType,
			String errorMsg) {
		if ((testType != ASTNode.Types.Error) && (testType != requiredType)) {
			System.out.println(errorMsg);
			typeErrors++;
		}
	}

	//Not sure if all of these are necessary
	void typeMustBe(ASTNode.Types testType, ASTNode.Types option1, 
			ASTNode.Types option2, String errorMsg){
		if ((testType != ASTNode.Types.Error) && !((testType == option1) || 
				(testType == option2)))
		{
			System.out.print(errorMsg);
			typeErrors++;
		}
	}

	void typeMustBe(ASTNode.Types testType, ASTNode.Types option1, 
			ASTNode.Types option2, ASTNode.Types option3,
			String errorMsg){
		if ((testType != ASTNode.Types.Error) && !((testType == option1) || 
				(testType == option2) || (testType == option3)))
		{
			System.out.print(errorMsg);
			typeErrors++;
		}
	}

	void typesMustBeEqual(ASTNode.Types type1,ASTNode.Types type2,
			String errorMsg) {
		if ((type1 != ASTNode.Types.Error) && (type2 != ASTNode.Types.Error) &&
				(type1 != type2)) {
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

	void typesMustBeComparable(ASTNode.Types type1,ASTNode.Types type2,
			String errorMsg) {
		if ((type1 == ASTNode.Types.Error) || (type2 == ASTNode.Types.Error))
			return;
		if ((type1 == ASTNode.Types.Boolean)&&(type2 == ASTNode.Types.Boolean))
			return;
		if (((type1 == ASTNode.Types.Integer) || 
				(type1 == ASTNode.Types.Character)) &&
				((type2 == ASTNode.Types.Integer) || 
						(type2 == ASTNode.Types.Character)))
			return;
		System.out.println(errorMsg);
		typeErrors++;
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
		case sym.CAND:
			return(" && ");
		case sym.COR:
			return(" || ");
		case sym.GEQ:
			return(" >= ");
		case sym.GT:
			return(" > ");
		case sym.LEQ:
			return(" <= ");
		case sym.LT:
			return(" < ");
		case sym.SLASH:
			return(" / ");
		case sym.TIMES:
			return(" * ");
		default:
			assertCondition(false);
			return "";
		}
	}

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
		case sym.CAND:
			System.out.print(" && ");
			break;
		case sym.COR:
			System.out.print(" || ");
			break;
		case sym.GEQ:
			System.out.print(" >= ");
			break;
		case sym.GT:
			System.out.print(" > ");
			break;
		case sym.LEQ:
			System.out.print(" <= ");
			break;
		case sym.LT:
			System.out.print(" < ");
			break;
		case sym.SLASH:
			System.out.print(" / ");
			break;
		case sym.TIMES:
			System.out.print(" * ");
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
		this.visit(n.thisStmt);
		this.visit(n.moreStmts);
	}

	void visit(nullStmtsNode n){}

	void visit(varDeclNode n){
		SymbolInfo     id;
		id = (SymbolInfo) st.localLookup(n.varName.idname);

		// Check that identNode.idname is not already in the symbol table.
		if (id != null) {
			System.out.println(error(n) + id.name()+ " is already declared.");
			typeErrors++;
			n.varName.type = ASTNode.Types.Error;
		} else {
			//If the initValue is not null, handle initialization
			if(!n.initValue.isNull())
			{
				//2. Type check initial value expression.
				visit(n.initValue);

				// Check that the initial value’s type is typeNode.type
				typesMustBeEqual(n.varType.type, ((exprNode)n.initValue).type, 
						error(n)+"Initializer must be of type "+n.varType.type);

				// Check that the initial value’s kind is scalar
				// Not sure if this is the best way to do it
				try{
				assertCondition(isScalar(((exprNode)n.initValue).kind));
				} catch (RuntimeException r){
					System.out.println(error(n) + "Initial value must be scalar");
				}
				// Enter identNode.idname into symbol table with 
				// 			type = typeNode.type and kind = Variable.
				id = new SymbolInfo(n.varName.idname,
						ASTNode.Kinds.Var, n.varType.type);

				n.varName.type = n.varType.type;
				try {
					st.insert(id);
				} catch (DuplicateException d) 
				{ /* can't happen */ }
				catch (EmptySTException e) 
				{ /* can't happen */ }
				n.varName.idinfo=id;

			} else { // Declaration without initialization

				// Enter identNode.idname into symbol table with 
				// type=typeNode.type and kind = Variable.
				id = new SymbolInfo(n.varName.idname,
						ASTNode.Kinds.Var, n.varType.type);
				n.varName.type = n.varType.type;
				try {
					st.insert(id);
				} catch (DuplicateException d) 
				{ /* can't happen */ }
				catch (EmptySTException e) 
				{ /* can't happen */ }
				n.varName.idinfo=id;
			}
		}
	}

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

		//Not sure if an assertCondition is necessary
	/*	assertCondition(n.kind == ASTNode.Kinds.Var ||
				n.kind == ASTNode.Kinds.Value ||
				n.kind == ASTNode.Kinds.Array ||
				n.kind == ASTNode.Kinds.Method ||
				n.kind == ASTNode.Kinds.VisibleLabel ||
				n.kind == ASTNode.Kinds.ScalarParm ||
				n.kind == ASTNode.Kinds.ArrayParm);

		id =  (SymbolInfo) st.globalLookup(n.idname);
		if (id == null) {
			System.out.println(error(n) +  n.idname + " is not declared.");
			typeErrors++;
			n.type = ASTNode.Types.Error;
		} else {
			n.type = id.type; 
			n.idinfo = id; // Save ptr to correct symbol table entry
		}
*/
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
		try{
    	assertCondition(n.varName.kind == ASTNode.Kinds.Array); // step 4
		} catch (RuntimeException e) {
			System.out.println(error(n) + "expected array.");
		}
		exprNode temp = (exprNode)n.subscriptVal;
		try{
    	assertCondition(isScalar(temp.kind)); // step 5
    	assertCondition((temp.type == ASTNode.Types.Integer) ||
                    (temp.type == ASTNode.Types.Character));
		} catch (RuntimeException e) {
			System.out.println(error(n) + "subscript value should be an integer or" +
                         " character, but " + temp.type + " was found instead.");
		}
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
		try{
			assertCondition(kindIsAssignable(n.target.kind)); // step 3
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Target of kind " + n.target.kind +
                         " is not assignable.");
		}
		if(isScalar(n.target.kind)){ // step 4
			assertCondition(isScalar(n.source.kind));
			typesMustBeEqual(n.source.type, n.target.type,
					error(n) + "Both the left and right hand sides of an assignment must"
							+ " have the same type.");
		}
		try{
			//can't assign to a constant (aka value)
			assertCondition(n.target.varName.kind != ASTNode.Kinds.Value);
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Cannot assign to a constant value.");
		}
		if((n.target.varName.kind == ASTNode.Kinds.Array) && // step 5
				(n.source.kind == ASTNode.Kinds.Array) &&
				(n.target.varName.type == n.source.type)){
			//look up target and source to get array info
			SymbolInfo id_s = (SymbolInfo)st.globalLookup(n.target.varName.idname);
			nameNode temp = (nameNode)n.target; //know is array => nameNode
			SymbolInfo id_t = (SymbolInfo)st.globalLookup(temp.varName.idname);
			try{
				assertCondition(id_s.arraySize == id_t.arraySize);
			} catch (RuntimeException e) {
				System.out.println(error(n) + "Array must be same length.");
			}
			return;			
		}
		if(n.target.kind == ASTNode.Kinds.Array && // step 6
				n.target.type == ASTNode.Types.Character &&
				n.source.kind == ASTNode.Kinds.String){
			SymbolInfo id_s = (SymbolInfo)st.globalLookup(n.target.varName.idname);
			strLitNode temp2 = (strLitNode)n.source; //know is string => strLitNode
			try{
				assertCondition(id_s.arraySize == temp2.strval.length());
			} catch (RuntimeException e) {
				System.out.println(error(n) + "Character array and String must have same length");
			}
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
		try{
    	assertCondition((n.outputValue.kind == ASTNode.Kinds.Value &&
      	              (n.outputValue.type == ASTNode.Types.Integer ||
      	               n.outputValue.type == ASTNode.Types.Boolean ||
      	               n.outputValue.type == ASTNode.Types.Character )) ||
      	              (n.outputValue.type == ASTNode.Types.Character &&
      	               n.outputValue.kind == ASTNode.Kinds.Array ) ||
      	              (n.outputValue.kind == ASTNode.Kinds.String));
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Can only print Integer, Boolean, and "+
                         "Character values, String, or arrays of Characters.");
		}
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

		//Make sure the binary operator has a valid binary operator symbol
		assertCondition(n.operatorCode == sym.PLUS  ||
				n.operatorCode == sym.MINUS || 
				n.operatorCode == sym.EQ    ||
				n.operatorCode == sym.NOTEQ ||
				n.operatorCode == sym.CAND  ||
				n.operatorCode == sym.COR   ||
				n.operatorCode == sym.GEQ   ||
				n.operatorCode == sym.GT    ||
				n.operatorCode == sym.LEQ   ||
				n.operatorCode == sym.LT    ||
				n.operatorCode == sym.SLASH ||
				n.operatorCode == sym.TIMES);


		// Set binaryOpNode.kind = Value
		// This is handled in the abstract syntax tree

		// Type check left and right operands
		this.visit(n.leftOperand);
		this.visit(n.rightOperand);

		// Check that left and right operands are both scalar.
		//These are causing problems
		try{
		assertCondition(isScalar(n.leftOperand.kind));
		assertCondition(isScalar(n.rightOperand.kind));
		} catch (RuntimeException r){
			System.out.print(error(n)+"Operands of"+opToString(n.operatorCode)
					+ "must be scalar.");
		}
		//Check for arithmetic operation, + - / *
		if (n.operatorCode== sym.PLUS  || n.operatorCode== sym.MINUS ||
				n.operatorCode== sym.SLASH || n.operatorCode== sym.TIMES){
			// Arithmetic operators may be applied to int or char values;
			// the result is of type int.
			n.type = ASTNode.Types.Integer;

			//Check that operands have an arithmetic type (int or char)
			typeMustBe(n.leftOperand.type, ASTNode.Types.Integer, 
					ASTNode.Types.Character, error(n) + "Left operand of" + 
							opToString(n.operatorCode) + "must be arithmetic.");
			typeMustBe(n.rightOperand.type, ASTNode.Types.Integer, 
					ASTNode.Types.Character, error(n) + "Right operand of" + 
							opToString(n.operatorCode) + "must be arithmetic.");

		} //Check if relational operation, == < > != <= >=
		else if (n.operatorCode == sym.EQ  || n.operatorCode == sym.NOTEQ ||
				n.operatorCode == sym.GEQ || n.operatorCode == sym.GT  ||
				n.operatorCode == sym.LEQ || n.operatorCode == sym.LT) {
			// Relational operatorsmay be applied only to a pair of arithmetic 
			// values or to a pair of bool values; the result is of type bool.
			n.type = ASTNode.Types.Boolean;
			
			String errorMsg = error(n)+"operands of"+
					opToString(n.operatorCode)+"must both be arithmetic or both"
							+ "must be boolean.";
			// Both operands must be arithmetic, or both must be booleans
			typesMustBeComparable(n.leftOperand.type, n.rightOperand.type,
																	errorMsg);
		} //Check if logical operation
		else {
			//Logical operators may be applied only to bool values
			//The result is of type bool.
			n.type = ASTNode.Types.Boolean;

			//Check that left and right operands have a boolean type. 
			typeMustBe(n.leftOperand.type, ASTNode.Types.Boolean, error(n) + 
					"Left operand of" + opToString(n.operatorCode) +  
					"must be arithmetic.");
			typeMustBe(n.rightOperand.type, ASTNode.Types.Boolean, error(n) + 
					"Right operand of" + opToString(n.operatorCode) +  
					"must be arithmetic.");
		}
	}

	void visit(intLitNode n){
		//      All intLits are automatically type-correct
	}

	void visit(classNode n){
		//"The class name is external to all other scopes; 
		//it never conflicts with any other declaration."
		SymbolInfo	id;
		id = new SymbolInfo(n.className.idname, ASTNode.Kinds.VisibleLabel, 
				ASTNode.Types.Void);
		try {
			st.insert(id);
			st.openScope();
			//Type check the members of the class
			this.visit(n.members); 
			st.closeScope();
		} catch (DuplicateException e) {
			// Can't occur
		} catch (EmptySTException e) {
			// Can't occur
		}
	}


	void  visit(memberDeclsNode n){
		//Type check fields
		this.visit(n.fields);
		//Type check methods
		this.visit(n.methods);
	}

	void  visit(methodDeclsNode n){
		this.visit(n.thisDecl);
		this.visit(n.moreDecls);
	}

	void visit(nullStmtNode n){}

	void visit(nullReadNode n){}

	void visit(nullPrintNode n){}

	void visit(nullExprNode n){}

	void visit(nullMethodDeclsNode n){}


	void visit(methodDeclNode n){		
		SymbolInfo     id;
		id = (SymbolInfo) st.localLookup(n.name.idname);

		// Check that identNode.idname is not already in the symbol table.
		if (id != null) {
			//If already in symbol table, check if overloading is possible
			// Create new scope in symbol table.
			st.openScope();

			//Type check args subtree. 
			// A list of symbol table nodes is created while doing this
			this.visit(n.args);
			
			//If it is in the table, check for unique parameters
			if(id.containsParms(st.getParms()))
			{
				System.out.println(error(n) + n.name.idname + " is already "
						+ "declared. Duplicate parameters, "
						+ "invalid overloading.");
			} else {
				id.addMethodParms(st.getParms());
			}

			//Check for correct return type
			if(id.type != n.returnType.type){
				System.out.println(error(n) + n.name.idname 
						+ " must be of type " + id.type);
				n.name.type = ASTNode.Types.Error;
				typeErrors++;
			}

			//Type check the decls subtree
			this.visit(n.decls);
			//Type check the stmts
			this.visit(n.stmts);

			try {
				st.closeScope();
			} catch (EmptySTException e) {
				// Nothing to do
			}

			n.name.idinfo=id;
		} else {
			// A method declaration requires a new symbol table entry.
			//Create new entry m, with type = typeNode.type, and kind = Method
			id = new SymbolInfo(n.name.idname, ASTNode.Kinds.Method, 
														n.returnType.type);

			//Create new scope in symbol table.
			st.openScope();
			
			// Not sure if this step is needed anymore:
			//4. Set currentMethod = methodDeclNode

			//Type check args subtree
			// A list of symbol table nodes is created while doing this
			this.visit(n.args);

			// Add the parameters to the methods symbol info
			id.addMethodParms(st.getParms());
			
			//Type check the decls subtree
			this.visit(n.decls);
			//Type check the stmts
			this.visit(n.stmts);

			//Close the current scope at the top of the symbol table 
			try { st.closeScope();
			} catch (EmptySTException e)
			{ /* can't happen? */ }
			n.name.idinfo=id;

			//Add method to symbol table
			try {
				st.insert(id);
			} catch (DuplicateException d) 
			{ /* can't happen */ }
			catch (EmptySTException e) 
			{ /* can't happen */ }
		}
	}

	// only vars(including params) of type int or char may be ++/--
	void visit(incrementNode n){
		try{
			assertCondition((n.target.kind == ASTNode.Kinds.Var ||
      	              n.target.kind == ASTNode.Kinds.ScalarParm ||
      	              n.target.kind == ASTNode.Kinds.ArrayParm) &&
      	              (n.target.type == ASTNode.Types.Character ||
      	              n.target.type == ASTNode.Types.Integer));
		} catch (RuntimeException e ){
			System.out.println(error(n) + "Only variables and parameters of type"+
                         "Integer or Characters may be incremented.");
		}
	}

	void visit(decrementNode n){
		try{
			assertCondition((n.target.kind == ASTNode.Kinds.Var ||
      	              n.target.kind == ASTNode.Kinds.ScalarParm ||
      	              n.target.kind == ASTNode.Kinds.ArrayParm) &&
      	              (n.target.type == ASTNode.Types.Character ||
      	              n.target.type == ASTNode.Types.Integer));
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Only variables and parameters of type"+
                         "Integer or Characters may be decremented.");
		}
	}

	void visit(argDeclsNode n){
		this.visit(n.thisDecl);
		this.visit(n.moreDecls);
	}

	void visit(nullArgDeclsNode n){}

	void visit(valArgDeclNode n){
		SymbolInfo	id;
		
		// Check that identNode.idname is not already in the symbol table.
		id = (SymbolInfo) st.localLookup(n.argName.idname);
		if (id != null) {
			System.out.println(error(n) + id.name()+ " is already declared.");
			typeErrors++;
			n.argName.type = ASTNode.Types.Error;
			//Inserts the erroneous node in the param list
			st.insertParm(n);
		} else {
			
			
			//Enter new id into the symbol table
			id = new SymbolInfo(n.argName.idname,
					ASTNode.Kinds.ScalarParm, n.argType.type);
			n.argName.type = n.argType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) 
			{ /* can't happen */ }
			catch (EmptySTException e) 
			{ /* can't happen */ }
			n.argName.idinfo=id;
			
			// Insert parameter into the list of parameters
			st.insertParm(n);
		}
	}

	void visit(arrayArgDeclNode n){
		SymbolInfo	id;
		
		// Check that identNode.idname is not already in the symbol table.
		id = (SymbolInfo) st.localLookup(n.argName.idname);
		if (id != null) {
			System.out.println(error(n) + id.name()+ " is already declared.");
			typeErrors++;
			n.argName.type = ASTNode.Types.Error;
			
			// Insert Error parameter node, not so sure about this
			st.insertParm(n);
		} else {		
			// Enter parameter into symbol table 
			id = new SymbolInfo(n.argName.idname,
					ASTNode.Kinds.ArrayParm, n.argName.type);
			n.argName.type = n.elementType.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) 
			{ /* can't happen */ }
			catch (EmptySTException e) 
			{ /* can't happen */ }
			n.argName.idinfo=id;

			//Add to scope's list of parameters
			st.insertParm(n);
		}
	}


	void visit(constDeclNode n){
		SymbolInfo	id;

		// Check that identNode.idname is not already in the symbol table.
		id = (SymbolInfo) st.localLookup(n.constName.idname);
		if (id != null) {
			System.out.println(error(n) + id.name()+ " is already declared.");
			typeErrors++;
			n.constName.type = ASTNode.Types.Error;
		} else {

			// Type check the const value expr.
			visit(n.constValue);	

			// 3. Check that the const value’s kind is scalar (Variable, Value or ScalarParm)
			try{
			assertCondition(isScalar(n.constValue.kind));
			} catch (RuntimeException r){
				System.out.println(error(n)+"Only scalars can be made constants");
			}
			//Enter identNode.idname into symbol table with 
			//type = constValue.type and kind = Value.
			id = new SymbolInfo(n.constName.idname,
					ASTNode.Kinds.Value, n.constValue.type);

			//The type of a constant is the type of the expression 
			//that defines the constant’s value.
			n.constName.type = n.constValue.type;
			try {
				st.insert(id);
			} catch (DuplicateException d) 
			{ /* can't happen */ }
			catch (EmptySTException e) 
			{ /* can't happen */ }
			n.constName.idinfo=id;
		}	
	}


	void visit(arrayDeclNode n){
		SymbolInfo	id;

		// Check that identNode.idname is not already in the symbol table.
		id = (SymbolInfo) st.localLookup(n.arrayName.idname);
		if (id != null) {
			System.out.println(error(n) + id.name()+ " is already declared.");
			typeErrors++;
			n.arrayName.type = ASTNode.Types.Error;
		} else {

			//The size of an array (in a declaration) must be greater than zero.
			if(n.arraySize.intval < 1){
				System.out.println(error(n) + n.arrayName.idname 
						+ " must have more than 0 elements.");
				typeErrors++;
				n.arrayName.type = ASTNode.Types.Error;
			}

			id = new SymbolInfo(n.arrayName.idname,
					ASTNode.Kinds.Array, n.elementType.type);

			n.arrayName.type = n.elementType.type; //not sure if correct
			try {
				st.insert(id);
			} catch (DuplicateException d) 
			{ /* can't happen */ }
			catch (EmptySTException e) 
			{ /* can't happen */ }
			n.arrayName.idinfo=id;
		}
		return;
	}

	void visit(charTypeNode n){
		//No type checking needed?
	}

	void visit(voidTypeNode n){
		//No type checking needed?
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
		try{
			assertCondition(n.condition.type == ASTNode.Types.Boolean && //step 2
                    isScalar(n.condition.kind));
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Condition must be a scalar boolean.");
		}
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
		try{
			assertCondition(n.label.kind == ASTNode.Kinds.VisibleLabel);
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Label "+n.label.idname+"out of bounds.");
		}
	}

	void visit(continueNode n){
		SymbolInfo id;
		id = (SymbolInfo) st.localLookup(n.label.idname);
		if (id == null) {
			System.out.println(error(n) + n.label.idname + " isn't declared.");
			typeErrors++;
		}
		try{
			assertCondition(n.label.kind == ASTNode.Kinds.VisibleLabel);
		} catch (RuntimeException e) {
			System.out.println(error(n) + "Label "+n.label.idname+"out of bounds.");
		}
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
		// Type and kind assigned in abstract syntax tree
	}

	void visit(strLitNode n){
		//automatically type correct like intLitNode?
	}

	void visit(trueNode n){
		// Type and kind assigned in abstract syntax tree
	}

	void visit(falseNode n){
		// Type and kind assigned in abstract syntax tree
	}

}
