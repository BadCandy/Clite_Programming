import java.util.*;

/**
 * 타입변환을 위한 클래스
 */
public class TypeTransformer {

	/**
	 * 본래의 Declarations와 변환된 본체(Block)으로 이루어진 새로운 Program을 생성하는 메소드
	 * @param p	타입 변환되기 전 프로그램
	 * @param tm TypeMap
	 * @return 타입 변환된 프로그램
	 */
	public static Program T (Program p, TypeMap tm) {
		Block body = (Block)T(p.body, tm);	// body부분을 타입 변환
		return new Program(p.decpart, body);	// 프로그램 리턴
		// 여기서 프로그램의 선언부는 타입변환이 필요없음.
		// 이것은 연산자를 가지지 않기 때문.
	} 

	/**
	 * Expression에서 타입 변환을 수행하는 메소드
	 * @param e	타입 변환 전 Expression
	 * @param tm TypeMap
	 * @return 타입 변환 후 Expression
	 */
	public static Expression T (Expression e, TypeMap tm) {
		if (e instanceof Value) // 식이 Value이면 Value 그대로 리턴
			return e;
		if (e instanceof Variable) 	// 식이 변수이면 그대로 리턴
			return e;
		if (e instanceof Binary) {	// 식이 이항 연산이면
			Binary b = (Binary)e; 
			Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);	// 피연산자1의 타입을 추출
			Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);	// 피연산자2의 타입을 추출
			Expression t1 = T (b.term1, tm);	// 피연산자1의 타입 변환 후 Expression 리턴
			// 이항 연산의 피연산자가 이항연산일 경우가 있으므로!
			Expression t2 = T (b.term2, tm);	// 피연산자2의 타입 변환 후 Expression 리턴
			if (typ1 == Type.INT) 	// 피연산자1의 타입이 Int이면
				return new Binary(b.op.intMap(b.op.val), t1,t2);	// int_minus, int_plus 등 계산
			else if (typ1 == Type.FLOAT) // 피연산자1의 타입이 Float이면
				return new Binary(b.op.floatMap(b.op.val), t1,t2);	// float_minus, float_plus 등 계산
			else if (typ1 == Type.CHAR) // 피연산자1의 타입이 char이면
				return new Binary(b.op.charMap(b.op.val), t1,t2);	// char_eq, char_ne 등 계산
			else if (typ1 == Type.BOOL) // 피연산자1의 타입이 Bool이면
				return new Binary(b.op.boolMap(b.op.val), t1,t2);	// bool_eq, bool_ne 등 계산
			throw new IllegalArgumentException("should never reach here");
		}
		if (e instanceof Unary) {	// 식이 단항 연산이면
			Unary u = (Unary) e;	
			Type typ1 = StaticTypeCheck.typeOf(u.term, tm);	// 피연산자의 타입 추출
			Expression term = T(u.term, tm);	// 피연산자의 타입 변환
			Operator op = u.op;		// 단항 연산자 추출
			//    System.err.println("TT: " + u.op);
			if (u.op.equals(Operator.NOT));		// 연산자가 ! 이면 그대로
			else if (u.op.equals(Operator.NEG)) {	// 연산자가 - 이면
				if (typ1== Type.INT)		// 피연산자가 Int이면
					op = op.intMap(op.val);	// int_neg
				else if (typ1== Type.FLOAT)	// 피연산자가 Float이면
					op = op.floatMap(op.val);	// float_neg
			}
			else if (u.op.equals(Operator.FLOAT))	// 연산자가 타입변환 float이면
				op = op.intMap(op.val);		// I2F
			else if (u.op.equals(Operator.CHAR))	// 연산자가 타입변환 char이면
				op = op.intMap(op.val);		// I2C
			else if (u.op.equals(Operator.INT)) {	// 연산자가 타입변환 Int이면
				if (typ1== Type.FLOAT)		// 피연산자 타입이 Float이면
					op = op.floatMap(op.val);	// F2I
				else if (typ1== Type.CHAR)		// 피연산자 타입이 Char이면
					op = op.charMap(op.val);	// C2I
			}
			else {
				throw new IllegalArgumentException("should never reach here");
			}         
			return new Unary(op, term);
		}
		// student exercise
		throw new IllegalArgumentException("should never reach here");
	}

	/**
	 * 문장 타입변환을 수행하는 메소드
	 * @param s	타입변환 전 Statement
	 * @param tm TypeMap
	 * @return 타입변환 후 Statement
	 */
	public static Statement T (Statement s, TypeMap tm) {
		if (s instanceof Skip) return s;	// Skip문은 바로 리턴
		if (s instanceof Assignment) {	// 저장문일 경우
			Assignment a = (Assignment)s;	
			Variable target = a.target;	
			Expression src = T (a.source, tm);	// source의 Expression 타입 변환
			Type ttype = (Type)tm.get(a.target);	// target의 타입
			Type srctype = StaticTypeCheck.typeOf(a.source, tm);	// source의 타입
			if (ttype == Type.FLOAT) {	// target 타입이 Float이면
				if (srctype == Type.INT) {	// src 타입이 Int이면
					src = new Unary(new Operator(Operator.I2F), src);	// I2F 단일 연산
					srctype = Type.FLOAT;	// 지역변수 srctype을 Float로 변경
				}
			}
			else if (ttype == Type.INT) {	// target 타입이 Int이면
				if (srctype == Type.CHAR) {	// src 타입이 Char이면
					src = new Unary(new Operator(Operator.C2I), src);	// C2I 단일 연산
					srctype = Type.INT;		// 지역변수 srctype을 Int로 변경
				}
			}
			StaticTypeCheck.check( ttype == srctype,
					"bug in assignment to " + target);	// target, source의 타입이 같아야 타당
			return new Assignment(target, src);
		} 
		if (s instanceof Conditional) {		// 조건문일 경우
			Conditional c = (Conditional)s;
			Expression test = T (c.test, tm);	// 수식 test의 타입변환
			Statement tbr = T (c.thenbranch, tm);	// thenbranch 문장의 타입변환
			Statement ebr = T (c.elsebranch, tm);	// elsebranch 문장의 타입변환
			return new Conditional(test,  tbr, ebr);
		}
		if (s instanceof Loop) {	// 루프문일 경우
			Loop l = (Loop)s;
			Expression test = T (l.test, tm);	// 수식 test의 타입변환
			Statement body = T (l.body, tm);	// body 문장의 타입변환
			return new Loop(test, body);
		}
		if (s instanceof Block) {	// 블록일 경우
			Block b = (Block)s;		
			Block out = new Block();
			for (Statement stmt : b.members)
				out.members.add(T(stmt, tm));	// 블록에 있는 문장들의 타입변환
			return out;
		}
		throw new IllegalArgumentException("should never reach here");
	}


	public static void main(String args[]) {
		Parser parser  = new Parser(new Lexer(args[0]));
		Program prog = parser.program();
		prog.display();           // student exercise
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		map.display();    // student exercise
		StaticTypeCheck.V(prog);
		Program out = T(prog, map);
		System.out.println("Output AST");
		out.display();    // student exercise
	} //main

} // class TypeTransformer


