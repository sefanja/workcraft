/* Generated By:JavaCC: Do not edit this line. GenlibParser.java */
package org.workcraft.plugins.circuit.javacc;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.NotFoundException;

public class GenlibParser implements GenlibParserConstants {

    public class Gate {
        public final String name;
        public final Function function;
        public Gate(String name, Function function) {
            this.name = name;
            this.function = function;
        }
    }

    public class Function {
        public final String name;
        public final Expression expression;
        public Function(String name, Expression expression) {
            this.name = name;
            this.expression = expression;
        }
    }

    public interface Expression {
        public String toString();
    }

    public class ExpressionExpression implements Expression {
        public final List<Expression> expressions;
        public ExpressionExpression(List<Expression> expressions) {
            this.expressions = expressions;
        }
        public String toString() {
                String result = "";
                boolean first = true;
                for (Expression expression: expressions) {
                        if (!first) {
                                result += "+";
                        }
                        result += expression.toString();
                        first = false;
                }
                return result;
        }
    }

    public class TermExpression implements Expression {
        public final List<Expression> expressions;
        public TermExpression(List<Expression> expressions) {
            this.expressions = expressions;
        }
        public String toString() {
                String result = "";
                boolean first = true;
                for (Expression expression: expressions) {
                        if (!first) {
                                result += "*";
                        }
                        result += expression.toString();
                        first = false;
                }
                return result;
        }
    }

    public class FactorExpression implements Expression {
        public final Expression expression;
        public FactorExpression(Expression expression) {
            this.expression = expression;
        }
        public String toString() {
                return "(" + expression.toString() + ")";
        }
    }

    public class NotExpression implements Expression {
        public final Expression expression;
        public NotExpression(Expression expression) {
            this.expression = expression;
        }
        public String toString() {
                return expression.toString() + "'";
        }
    }

    public class LiteralExpression implements Expression {
        public final String name;
        public LiteralExpression(String name) {
            this.name = name;
        }
        public String toString() {
                return name;
        }
    }

    public class ConstantExpression implements Expression {
        public final boolean value;
        public ConstantExpression(boolean value) {
            this.value = value;
        }
        public String toString() {
                return (value ? "1" : "0");
        }
    }

  final public List<Gate> parseGenlib() throws ParseException {
    trace_call("parseGenlib");
    try {
    List<Gate> gates;
      gates = parseGates();
        {if (true) return gates;}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseGenlib");
    }
  }

  final public List<Gate> parseGates() throws ParseException {
    trace_call("parseGates");
    try {
    Gate gate;
    List<Gate> gates = new LinkedList<Gate>();
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case GATE:
          ;
          break;
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        gate = parseGate();
            gates.add(gate);
      }
        {if (true) return gates;}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseGates");
    }
  }

  final public Gate parseGate() throws ParseException {
    trace_call("parseGate");
    try {
    Token nameToken;
    Function function;
      jj_consume_token(GATE);
      nameToken = jj_consume_token(NAME);
      jj_consume_token(NUMERAL);
      function = parseFunction();
      jj_consume_token(25);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PIN:
          ;
          break;
        default:
          jj_la1[1] = jj_gen;
          break label_2;
        }
        parsePin();
      }
        String name = nameToken.image;
        {if (true) return new Gate(name, function);}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseGate");
    }
  }

  final public void parsePin() throws ParseException {
    trace_call("parsePin");
    try {
      jj_consume_token(PIN);
      jj_consume_token(NAME);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INV:
      case NONINV:
      case UNKNOWN:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case INV:
          jj_consume_token(INV);
          break;
        case NONINV:
          jj_consume_token(NONINV);
          break;
        case UNKNOWN:
          jj_consume_token(UNKNOWN);
          break;
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[3] = jj_gen;
        ;
      }
      jj_consume_token(NUMERAL);
      jj_consume_token(NUMERAL);
      jj_consume_token(NUMERAL);
      jj_consume_token(NUMERAL);
      jj_consume_token(NUMERAL);
      jj_consume_token(NUMERAL);
    } finally {
      trace_return("parsePin");
    }
  }

  final public Function parseFunction() throws ParseException {
    trace_call("parseFunction");
    try {
        Token nameToken;
        Expression expression;
      nameToken = jj_consume_token(NAME);
      jj_consume_token(24);
      expression = parseExpression();
                String name = nameToken.image;
                {if (true) return new Function(name, expression);}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseFunction");
    }
  }

  final public Expression parseExpression() throws ParseException {
    trace_call("parseExpression");
    try {
        Expression term;
        List<Expression> terms = new LinkedList<Expression>();
      term = parseTerm();
                        terms.add(term);
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case OR:
          ;
          break;
        default:
          jj_la1[4] = jj_gen;
          break label_3;
        }
        jj_consume_token(OR);
        term = parseTerm();
      }
                        terms.add(term);
                {if (true) return new ExpressionExpression(terms);}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseExpression");
    }
  }

  final public Expression parseTerm() throws ParseException {
    trace_call("parseTerm");
    try {
        Expression factor;
        List<Expression> factors = new LinkedList<Expression>();
      factor = parseFactor();
                        factors.add(factor);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
          ;
          break;
        default:
          jj_la1[5] = jj_gen;
          break label_4;
        }
        jj_consume_token(AND);
        factor = parseFactor();
      }
                        factors.add(factor);
                {if (true) return new TermExpression(factors);}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseTerm");
    }
  }

  final public Expression parseFactor() throws ParseException {
    trace_call("parseFactor");
    try {
        Expression factor;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NAME:
        factor = parseLiteral();
        break;
      case CONST0:
      case CONST1:
        factor = parseConstant();
        break;
      case NOT_PREFIX:
        jj_consume_token(NOT_PREFIX);
        factor = parseFactor();
                        {if (true) return new NotExpression(factor);}
        break;
      case 20:
        jj_consume_token(20);
        factor = parseExpression();
        jj_consume_token(21);
        break;
      default:
        jj_la1[6] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
                {if (true) return factor;}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseFactor");
    }
  }

  final public Expression parseLiteral() throws ParseException {
    trace_call("parseLiteral");
    try {
        Token nameToken;
      nameToken = jj_consume_token(NAME);
                String name = nameToken.image;
                {if (true) return new LiteralExpression(name);}
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseLiteral");
    }
  }

  final public Expression parseConstant() throws ParseException {
    trace_call("parseConstant");
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CONST0:
        jj_consume_token(CONST0);
                        {if (true) return new ConstantExpression(false);}
        break;
      case CONST1:
        jj_consume_token(CONST1);
                        {if (true) return new ConstantExpression(true);}
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    throw new Error("Missing return statement in function");
    } finally {
      trace_return("parseConstant");
    }
  }

  /** Generated Token Manager. */
  public GenlibParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[8];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x40,0x80,0x700,0x700,0x20000,0x10000,0x14c800,0xc000,};
   }

  /** Constructor with InputStream. */
  public GenlibParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public GenlibParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new GenlibParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public GenlibParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new GenlibParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public GenlibParser(GenlibParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(GenlibParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      trace_token(token, "");
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
      trace_token(token, " (in getNextToken)");
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[27];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 8; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 27; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  private int trace_indent = 0;
  private boolean trace_enabled = true;

/** Enable tracing. */
  final public void enable_tracing() {
    trace_enabled = true;
  }

/** Disable tracing. */
  final public void disable_tracing() {
    trace_enabled = false;
  }

  private void trace_call(String s) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
      System.out.println("Call:   " + s);
    }
    trace_indent = trace_indent + 2;
  }

  private void trace_return(String s) {
    trace_indent = trace_indent - 2;
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
      System.out.println("Return: " + s);
    }
  }

  private void trace_token(Token t, String where) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
      System.out.print("Consumed token: <" + tokenImage[t.kind]);
      if (t.kind != 0 && !tokenImage[t.kind].equals("\"" + t.image + "\"")) {
        System.out.print(": \"" + t.image + "\"");
      }
      System.out.println(" at line " + t.beginLine + " column " + t.beginColumn + ">" + where);
    }
  }

  private void trace_scan(Token t1, int t2) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
      System.out.print("Visited token: <" + tokenImage[t1.kind]);
      if (t1.kind != 0 && !tokenImage[t1.kind].equals("\"" + t1.image + "\"")) {
        System.out.print(": \"" + t1.image + "\"");
      }
      System.out.println(" at line " + t1.beginLine + " column " + t1.beginColumn + ">; Expected token: <" + tokenImage[t2] + ">");
    }
  }

}
