import java.util.Scanner;

// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

/**
 * 프로그램 상태를 변환하는 함수들의 집합
 * Program의 의미와 추상 구문에 나오는 모든 종류의 문장들의 의미를 정의함
 * ex) Skip, Block, Conditional, Loop, Assignment.
 * AbstractSyntax Class와 함께 Clite의 인터프리터를 형성한다.
 */
public class Semantics {

	/**
	 * 프로그램의 의미를 함수형으로 정의
	 * Program의 의미는 State를 산출하는 함수
	 * State는 HashMap<Variable, Value>, 즉 변수가 어떠한 값을 가진 상태를 나타냄
	 * @param p	Program
	 * @return State
	 */
	State M (Program p) { 
		return M (p.body, initialState(p.decpart)); 	// 선언된 각 변수가 해당 타입의 
		// undef 값으로 초기화된 상태를 생성 
	}

	/**
	 * 선언된 각 변수가 해당 타입의 undef 값으로 초기화 상태를 생성한다.
	 * @param d	ArrayList로 구현된 Declarations
	 * @return 초기화된 State
	 */
	State initialState (Declarations d) {
		State state = new State();
		Value intUndef = new IntValue();
		for (Declaration decl : d)
			state.put(decl.v, Value.mkValue(decl.t));	// Value.mkValue()가 해당 타입의 미정의 값 생성
		return state;
	}

	/**
	 * Statement의 종류에 따라서 의미를 결정 ( Statement * State => State )
	 * 								   ( Statement = Skip | Assignment | Conditional | Loop 
	 * 									| Block | InputStatement | OutputStatement)
	 * 즉, 현재 State에 대하여 Statement의 종류에 따라 새로운 State를 생성
	 * @param s	Statement 
	 * @param state	현재 State
	 * @return Statement의 종류에 따른 새로운 State
	 */
	// 추가
	State M (Statement s, State state) {
		if (s instanceof Skip) return M((Skip)s, state);
		if (s instanceof Assignment)  return M((Assignment)s, state);
		if (s instanceof Conditional)  return M((Conditional)s, state);
		if (s instanceof Loop)  return M((Loop)s, state);
		if (s instanceof Block)  return M((Block)s, state);
		if (s instanceof InputStatement) return M((InputStatement)s, state);
		if (s instanceof OutputStatement) return M((OutputStatement)s, state);
		throw new IllegalArgumentException("should never reach here");
	}

	/**
	 * Skip문에 대한 새로운 상태를 반환하는 메소드
	 * Skip문은 상태에 아무런 변화를 주지 않음
	 * @param s Skip Statement
	 * @param state	현재 State
	 * @return 아무런 변화가 없는 State
	 */
	State M (Skip s, State state) {
		return state;
	}

	/**
	 * 현재 상태에서 수식 source의 값을 구하고,
	 * 변수 target의 값을 이 값으로 대체하여 새로운 상태를 반환하는 메소드
	 * 
	 * @param a Assignment Statement
	 * @param state	현재 State
	 * @return 새로운 State
	 */
	State M (Assignment a, State state) {
		// state.onion()은 해당 변수의 값을 덮어써 새로운 상태를 만든다.
		return state.onion(a.target, M (a.source, state));	
	}

	/**
	 * 논리식 test가 참이면 Conditional의 의미는 문장 thenbranch의 의미가 되고,
	 * 반대로 거짓이면 문장 elsebranch의 의미가 되는 새로운 상태를 반환하는 메소드
	 * @param c Conditional Statement
	 * @param state	현재 State
	 * @return 새로운 State
	 */
	State M (Conditional c, State state) {
		if (M(c.test, state).boolValue( ))
			return M (c.thenbranch, state);
		else
			return M (c.elsebranch, state);
	}

	/**
	 * 논리식 test가 거짓이면 Loop의 의미는 메소드 파라미터의 상태(현재 상태)와 같다.
	 * 참이라면 현재 상태에서 얻은 body의 의미에 다시 이 의미 규칙을 적용한 결과이다
	 * @param l Loop Statement
	 * @param state 현재 State
	 * @return 새로운 State
	 */
	State M (Loop l, State state) {
		if (M (l.test, state).boolValue( ))
			return M(l, M (l.body, state));	// 재귀적으로 구현
		else return state;
	}

	/**
	 * Block은 문장들의 나열이고 문장들은 나타난 순서대로 실행된다. ( Block = Statement* )
	 * 현재 상태에 문장들을 차례로 적용하여 얻어지는 복합적인 의미이다.
	 * Block에 하나 이상의 문장이 있으면, 첫 번째 문장의 의미로부터 얻어진 상태가
	 * Block의 나머지 문장들의 의미를 정의하는 입력 상태가 된다.
	 * @param b	Block Statement
	 * @param state 현재 State
	 * @return 새로운 State
	 */
	State M (Block b, State state) {
		for (Statement s : b.members)
			state = M (s, state);	// 얻어진 이전 상태를 재귀적으로 입력하여 새로운 상태를 만든다.
		return state;
	}
	
	/**
	 * OutputStatement(출력, cout) 구문을 위해 작성한 메소드이다.
	 * OutputStatement는 상태를 변경시키지 않는다.
	 * @param o	OutputStatement
	 * @param state 현재 State
	 * @return 동일한 State
	 */
	State M (OutputStatement o, State state) {
		if (M(o.expr, state).type() == Type.BOOL) 
			System.out.print(M(o.expr, state).boolValue());
		else if (M(o.expr, state).type() == Type.CHAR) 
			System.out.print(M(o.expr, state).charValue());
		else if (M(o.expr, state).type() == Type.INT) 
			System.out.print(M(o.expr, state).intValue());
		else if (M(o.expr, state).type() == Type.FLOAT) 
			System.out.print(M(o.expr, state).floatValue());
		else
			throw new IllegalArgumentException("No Exist Type in Clite.");
		return state;
	}
	
	/**
	 * InputStatement(입력, cin) 구문을 위해 작성한 메소드이다.
	 * InputStatement는 입력을 받아야 한다는 점빼고 Assignment와 비슷하다.
	 * @param i InputStatement
	 * @param state 현재 State
	 * @return 새로운 State
	 * @throws IllegalArgumentException CLite에서 허용하는 타입(bool, char, int, float)이외의 타입을 입력받으려고 하면 예외발생
	 */
	State M (InputStatement i, State state) throws IllegalArgumentException{		
		// state.onion()은 해당 변수의 값을 덮어써 새로운 상태를 만든다.
		Scanner sc = new Scanner(System.in);
		String str = sc.next();
		if (isStringInteger(str))
			return state.onion(i.target, M (new IntValue(Integer.parseInt(str)), state));	
		if (isStringFloat(str))
			return state.onion(i.target, M (new FloatValue(Float.parseFloat(str)), state));	
		if (isStringBoolean(str))
			return state.onion(i.target, M (new BoolValue(Boolean.parseBoolean(str)), state));	
		if (isStringChar(str))
			return state.onion(i.target, M (new CharValue(str.charAt(0)), state));
		throw new IllegalArgumentException("Not Exist Type in Clite.");
	}

	public static boolean isStringInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isStringFloat(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isStringBoolean(String s) {
		if (s.equals("true") || s.equals("false"))
			return true;
		return false;
		
	}

	public static boolean isStringChar(String s) {
		if (s.length() != 1) 
			return false;
		return true;
	}

	/**
	 * 수식 Expression의 의미를 반환하는 메소드이다.
	 * 여기서 Expression의 의미는 Expression의 결과는 결국 Value이기 때문에 Value를 반환한다.
	 * @param e Expression
	 * @param state 현재 State
	 * @return Expression의 Value
	 */
	Value M (Expression e, State state) {
		if (e instanceof Value) // 식이 Value이면 자체로 식의 의미가 된다.
			return (Value)e;

		if (e instanceof Variable) 	// 식이 Variable이면
			return (Value)(state.get(e));	// 식의 의미는 Variable의 Value 자체가 된다.

		if (e instanceof Binary) {	// 식이 이항 연산이면
			Binary b = (Binary)e;	

			// 피연산자 term1과 term2의 의미를 먼저 결정한 후
			// 피연산자들의 Value에 Operator op를 적용하여 전체 수식의 의미를 결정한다.
			return applyBinary (b.op, 
					M(b.term1, state), M(b.term2, state));
		}

		if (e instanceof Unary) {	// 식이 단항 연산이면
			Unary u = (Unary)e;	

			// 피연산자 term의 의미를 먼저 결정하고
			// 피연산자의 Value에 Operator op를 적용하여 전체 수식의 의미를 결정한다.
			return applyUnary(u.op, M(u.term, state));
		}

		throw new IllegalArgumentException("should never reach here");
	}

	/**
	 * 수식 Binary 의미를 정하기 위해 Binary를 연산하여 Value를 반환하는 메소드이다.
	 * @param op Operator
	 * @param v1 피연산자 term1
	 * @param v2 피연산자 term2
	 * @return Binary를 연산한 결과 Value
	 */
	Value applyBinary (Operator op, Value v1, Value v2) {
		// 피연산자의 값이 Undef가 아니여야 한다.
		StaticTypeCheck.check( ! v1.isUndef( ) && ! v2.isUndef( ),	
				"reference to undef value");	

		/*
		 * 여기서부터는 식의 이항 결과 Value를 반환한다.
		 */
		if (op.val.equals(Operator.INT_PLUS))
			return new IntValue(v1.intValue( ) + v2.intValue( ));
		if (op.val.equals(Operator.INT_MINUS)) 
			return new IntValue(v1.intValue( ) - v2.intValue( ));
		if (op.val.equals(Operator.INT_TIMES)) 
			return new IntValue(v1.intValue( ) * v2.intValue( ));
		if (op.val.equals(Operator.INT_DIV)) 
			return new IntValue(v1.intValue( ) / v2.intValue( ));
		// student exercise
		if (op.val.equals(Operator.INT_LT)) 
			return new BoolValue(v1.intValue( ) < v2.intValue( ));
		if (op.val.equals(Operator.INT_LE)) 
			return new BoolValue(v1.intValue( ) <= v2.intValue( ));
		if (op.val.equals(Operator.INT_EQ)) 
			return new BoolValue(v1.intValue( ) == v2.intValue( ));
		if (op.val.equals(Operator.INT_NE)) 
			return new BoolValue(v1.intValue( ) != v2.intValue( ));
		if (op.val.equals(Operator.INT_GE)) 
			return new BoolValue(v1.intValue( ) >= v2.intValue( ));
		if (op.val.equals(Operator.INT_GT)) 
			return new BoolValue(v1.intValue( ) > v2.intValue( ));
		if (op.val.equals(Operator.FLOAT_PLUS)) 
			return new FloatValue(v1.floatValue( ) + v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_MINUS)) 
			return new FloatValue(v1.floatValue( ) - v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_TIMES)) 
			return new FloatValue(v1.floatValue( ) * v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_DIV)) 
			return new FloatValue(v1.floatValue( ) / v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_LT)) 
			return new BoolValue(v1.floatValue( ) < v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_LE)) 
			return new BoolValue(v1.floatValue( ) <= v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_EQ)) 
			return new BoolValue(v1.floatValue( ) == v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_NE)) 
			return new BoolValue(v1.floatValue( ) != v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_GE)) 
			return new BoolValue(v1.floatValue( ) >= v2.floatValue( ));
		if (op.val.equals(Operator.FLOAT_GT)) 
			return new BoolValue(v1.floatValue( ) > v2.floatValue( ));
		if (op.val.equals(Operator.CHAR_LT)) 
			return new BoolValue(v1.charValue( ) < v2.charValue( ));
		if (op.val.equals(Operator.CHAR_LE)) 
			return new BoolValue(v1.charValue( ) <= v2.charValue( ));
		if (op.val.equals(Operator.CHAR_EQ)) 
			return new BoolValue(v1.charValue( ) == v2.charValue( ));
		if (op.val.equals(Operator.CHAR_NE)) 
			return new BoolValue(v1.charValue( ) != v2.charValue( ));
		if (op.val.equals(Operator.CHAR_GE)) 
			return new BoolValue(v1.charValue( ) >= v2.charValue( ));
		if (op.val.equals(Operator.CHAR_GT)) 
			return new BoolValue(v1.charValue( ) > v2.charValue( ));
		if (op.val.equals(Operator.BOOL_LT)) 
			return new BoolValue(v1.intValue( ) < v2.intValue( ));
		if (op.val.equals(Operator.BOOL_LE)) 
			return new BoolValue(v1.intValue( ) <= v2.intValue( ));
		if (op.val.equals(Operator.BOOL_EQ)) 
			return new BoolValue(v1.boolValue( ) == v2.boolValue( ));
		if (op.val.equals(Operator.BOOL_NE)) 
			return new BoolValue(v1.boolValue( ) != v2.boolValue( ));
		if (op.val.equals(Operator.BOOL_GE)) 
			return new BoolValue(v1.intValue( ) >= v2.intValue( ));
		if (op.val.equals(Operator.BOOL_GT)) 
			return new BoolValue(v1.intValue( ) > v2.intValue( ));
		if (op.val.equals(Operator.AND)) 
			return new BoolValue(v1.boolValue( ) && v2.boolValue( ));
		if (op.val.equals(Operator.OR)) 
			return new BoolValue(v1.boolValue( ) || v2.boolValue( ));
		throw new IllegalArgumentException("should never reach here");
	} 

	/**
	 * 수식 Unary의 의미를 정하기 위해 Unary를 연산하여 결과 Value를 반환하는 메소드이다.
	 * @param op Operator
	 * @param v 피연산자 term1의 Value
	 * @return Unary 연산 후 결과 Value
	 */
	Value applyUnary (Operator op, Value v) {
		// 피연산자 term1이 undef이면 안된다.
		StaticTypeCheck.check( ! v.isUndef( ),
				"reference to undef value");
		/*
		 * 여기부터는 단항연산 결과를 반환한다.
		 */
		if (op.val.equals(Operator.NOT))
			return new BoolValue(!v.boolValue( ));
		else if (op.val.equals(Operator.INT_NEG))
			return new IntValue(-v.intValue( ));
		else if (op.val.equals(Operator.FLOAT_NEG))
			return new FloatValue(-v.floatValue( ));
		else if (op.val.equals(Operator.I2F))
			return new FloatValue((float)(v.intValue( ))); 
		else if (op.val.equals(Operator.F2I))
			return new IntValue((int)(v.floatValue( )));
		else if (op.val.equals(Operator.C2I))
			return new IntValue((int)(v.charValue( )));
		else if (op.val.equals(Operator.I2C))
			return new CharValue((char)(v.intValue( )));
		throw new IllegalArgumentException("should never reach here");
	} 

	public static void main(String args[]) {
		Parser parser  = new Parser(new Lexer(args[0]));
		Program prog = parser.program();
		prog.display();    // student exercise
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		map.display();    // student exercise
		StaticTypeCheck.V(prog);
		Program out = TypeTransformer.T(prog, map);
		System.out.println("Output AST");
		out.display();    // student exercise
		Semantics semantics = new Semantics( );
		State state = semantics.M(out);
		System.out.println("Final State");
		state.display( );  // student exercise
	}
}
