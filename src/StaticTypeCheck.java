// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {

	/**
	 * 해쉬맵을 상속받은 타입맵 반환, <변수, 타입>
	 * @param d	선언들, ArrayList로 구현
	 * @return	TypeMap 반환
	 */
    public static TypeMap typing (Declarations d) {	
        TypeMap map = new TypeMap();	// 타입맵 생성
        for (Declaration di : d) 	// 각 선언마다
            map.put (di.v, di.t);	// 선언의 변수와 타입을 연결
        return map;					// 그 맵을 반환
    }

    /**
     * 변수의 이름이 중복이 되는지 검사하는데 쓰이는 메소드
     * @param test	boolean 값
     * @param msg	중복됬을 때 출력할 오류 메시지
     */
    public static void check(boolean test, String msg) {	
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }

    /**
     * 선언들 중에서 각각의 선언들을 일일이 비교하여 변수이름이 중복되는지 검사
     * @param d	ArrayList로 구현된 선언 전체
     */
    public static void V (Declarations d) {		
        for (int i=0; i<d.size() - 1; i++)		
            for (int j=i+1; j<d.size(); j++) {	
                Declaration di = d.get(i);	
                Declaration dj = d.get(j);	
                check( ! (di.v.equals(dj.v)),
                       "duplicate declaration: " + dj.v);
            }
    } 

    /**
     * 프로그램의 선언부분 decpart와 블록부분 body(Statements)가 타입 환경에 의해 타당한지 검사
     * @param p	선언부의 decpart와 block(Statements)를 나타내는 body
     */
    public static void V (Program p) {
        V (p.decpart);		// 프로그램 선언부의 변수 이름이 중복되는지 검사
        V (p.body, typing (p.decpart));		// Statement들이 선언부의 type에 타당한지 검사
    } 

    /**
     * 해당 Expression의 결과 타입을 반환. 어떠한 것에도 해당되지 않으면 예외를 던진다.
     * @param e	Expression
     * @param tm TypeMap
     * @return
     */
    public static Type typeOf (Expression e, TypeMap tm) {
        if (e instanceof Value) 	// 식이 값일 때
        	return ((Value)e).type;	// 그 값의 타입을 바로 리턴
        
        if (e instanceof Variable) {	// 식이 변수일 때
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);	// 타입맵에 포함되어 있지않은 v면
            														// (제대로 선언되어 있지 않다면)
            														// 오류 출력, 그렇지 않다면 정상동작
            return (Type) tm.get(v);
        }
        
        if (e instanceof Binary) {		// 식이 Binary (이항 연산) 이면
            Binary b = (Binary)e;		// Expression을 Binary로 DownCasting
            if (b.op.ArithmeticOp( ))	// 산술 연산자이면
                if (typeOf(b.term1,tm)== Type.FLOAT)	// term1이 Float이면
                    return (Type.FLOAT);				// Float를 반환
                else return (Type.INT);					// 그렇지 않으면 Int 반환
            if (b.op.RelationalOp( ) || b.op.BooleanOp( )) 	// 관계 연산자이거나 bool 연산자이면
                return (Type.BOOL);							// Bool 반환
        }
        
        if (e instanceof Unary) {	// 식이 Unary (단항 연산) 이면
            Unary u = (Unary)e;		// Unary로 DownCasting
            if (u.op.NotOp( ))        return (Type.BOOL);	// ! 연산자이면 Bool
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);	// - 연산자이면 피연산자의 타입
            else if (u.op.intOp( ))    return (Type.INT);	//	타입 변환 int 연산이면	Int 
            else if (u.op.floatOp( )) return (Type.FLOAT);	// 타입 변환 float 연산이면 Float
            else if (u.op.charOp( ))  return (Type.CHAR);	// 타입 변환 char 연산이면 Char
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    /**
     * Statement들 중에서 Expression일 경우 타입환경에 의해 타당한지 검사
     * @param e	Expression
     * @param tm TypeMap
     */
    public static void V (Expression e, TypeMap tm) {
        if (e instanceof Value) 	// Value는 타당하다. (Value만 나와도 상관없음)
            return;
        if (e instanceof Variable) { 	// 식이 변수일 경우
            Variable v = (Variable)e;	
            check( tm.containsKey(v)
                   , "undeclared variable: " + v);	// 변수의 값의 타입이 타입맵에 포함될 경우 정상
            return;
        }
        if (e instanceof Binary) {	// 이항 연산일 경우
            Binary b = (Binary) e;	
            Type typ1 = typeOf(b.term1, tm);	// 피연산자 term1의 타입 추출
            Type typ2 = typeOf(b.term2, tm);	// 피연산자 term2의 타입 추출
            V (b.term1, tm);	// 피연산자가 타당한지 검사
            V (b.term2, tm);	// 피연산자가 타당한지 검사
            if (b.op.ArithmeticOp( ))  // 만약 산술 연산이면
                check( typ1 == typ2 &&	// 두 개의 피연산자 타입이 같고 Int 또는 Float 타입이면 타당
                       (typ1 == Type.INT || typ1 == Type.FLOAT)
                       , "type error for " + b.op);
            else if (b.op.RelationalOp( )) 	// 비교 연산일 경우
                check( typ1 == typ2 , "type error for " + b.op);	// 무조건 타입이 같아야한다.
            else if (b.op.BooleanOp( )) 	// 논리 연산일 경우
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,	// 무조건 피연산자는 둘다 Bool 타입
                       b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        
        // 단항 연산일 경우
        if (e instanceof Unary) {// student exercise
            Unary u = (Unary) e;	
            Type typ1 = typeOf(u.term, tm);	// 피연산자의 타입 추출
            //System.err.println("Unary: " + u.op);
            V(u.term, tm);	// 피연산자의 타입이 타당한지 검사
            if (u.op.equals(Operator.NOT))	// ! 연산자이면
                check( typ1 == Type.BOOL , "! has non-bool operand");	// 피연산자의 타입은 Bool이여야 함
            else if (u.op.equals(Operator.NEG))	// - 연산자이면
                check( typ1 == Type.INT || typ1 == Type.FLOAT	// 피연산자의 타입은 Int나 Float여야 함
                       , "Unary - has non-int/float operand");
            else if (u.op.equals(Operator.FLOAT))	// 타입 변환 Float 연산이면 피연산자 타입은 Int
                check( typ1== Type.INT, "float() has non-int operand");
            else if (u.op.equals(Operator.CHAR))	// 타입 변환 Char 연산이면 피연산자 타입은 Int
                check( typ1== Type.INT , "char() has non-int operand");
            else if (u.op.equals(Operator.INT))		// 타입변 변환 Int 연산이면 피연산자 타입은 Float 또는 Char
                check( typ1== Type.FLOAT || typ1== Type.CHAR
                       , "int() has non-float/char operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }

        throw new IllegalArgumentException("should never reach here");
    }

    /**
     * Statement가 프로그램 타입 환경에 의해서 타당한지 검사
     * @param s	Statement
     * @param tm TypeMap
     */
    public static void V (Statement s, TypeMap tm) {
        if ( s == null )	// 아무것도 써있지 않을경우 예외를 던짐
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Skip) return;	// Skip문은 항상 타당하다.
        if (s instanceof Assignment) {	// 저장문일 경우
            Assignment a = (Assignment)s;
            check( tm.containsKey(a.target)	// 저장문의 target(lv)이 올바르게 선언되있는지 검사
                   , " undefined target in assignment: " + a.target);
            V(a.source, tm);	// 저장문의 source(rv)의 타입이 타당한지 검사
            Type ttype = (Type)tm.get(a.target);	// target의 타입
            Type srctype = typeOf(a.source, tm);	// source의 타입
            
            if (ttype != srctype) {				// target, source의 타입이 같지 않다면
                if (ttype == Type.FLOAT)		// target의 타입이 Float이고
                    check( srctype == Type.INT	// source의 타입이 Int이면 타당
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)		// target의 타입이 Int이고
                    check( srctype == Type.CHAR	// source의 타입이 Char이면 타당
                           , "mixed mode assignment to " + a.target);
                else	// 그 외엔 타당하지 않음
                    check( false	
                           , "mixed mode assignment to " + a.target);
            }
            return;
        } 
        
        if (s instanceof Conditional) {		// 조건문일 경우
            Conditional c = (Conditional)s;
            V (c.test, tm);		// 조건문의 수식 test가 타당한지 검사
            check( typeOf(c.test, tm)== Type.BOOL ,		// 수식 test가 bool이여야 함
                   "non-bool test in conditional");
            V (c.thenbranch, tm);	// 조건문의 thenbranch 문장이 타당한지 검사
            V (c.elsebranch, tm);	// 조건문의 elsebranch 문장이 타당한지 검사
            return;
        }
        if (s instanceof Loop) {	// 루프문일 경우
            Loop l = (Loop)s;
            V (l.test, tm);		// 루프문의 수식 test가 타당한지 검사
            check(  typeOf(l.test, tm)== Type.BOOL ,	// 수식 test가 bool이여야 함
                    "loop has non-bool test");
            V (l.body, tm);		// 루프문의 body 문장이 타당한지 검사
            return;
        }
        if (s instanceof Block) {	// 블록일 경우
            Block b = (Block)s;		
            for (int j=0; j < b.members.size(); j++)
                V((Statement)(b.members.get(j)), tm);	// 각 Statement가 타당한지 검사
            return;
        }
        // student exercise
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   // student exercise
        V(prog);
    } //main

}