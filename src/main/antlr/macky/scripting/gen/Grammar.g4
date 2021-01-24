grammar Grammar;

@header {
package macky.scripting.gen;
}

WS : [ \t\r\n]+ -> skip;

COMMENT: '//' ~( '\r' | '\n' )* -> channel(HIDDEN);

STRING : ('"' (~'"')* '"') | ('\'' (~'\'')* '\'');
NUMBER: [0-9]+ (('.' [0-9]+ 'D'?) | 'L')?;
IDENTIFIER: ([$_] | [a-z] | [A-Z]) ([$_] | [a-z] | [A-Z] | [0-9])*;

identifier: IDENTIFIER;
number: NUMBER;
string: STRING;

parametre_header: (identifier (',' identifier)*)?;
parametre_list: (expression (',' expression)*)?;

bracketed_expr: '{' expression_list '}';

assignment_operator: '=' | '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '|=';
equality_operator: '==' | '!=';
relational_operator: '<' | '>' | '<=' | '>=';
additive_operator: '+' | '-';
multiplicative_operator: '*' | '/' | '%';

map_init_entry: expression (':' | '=') expression;

expression:
    '(' expression ')' # Paren
    | ('true' | 'false') # BoolLiteral
    | 'break' # Break
    | 'function' '(' parametre_header ')' bracketed_expr # DeclFunc
    | 'if' expression bracketed_expr ('else' bracketed_expr)? # If
    | 'while' expression bracketed_expr # While
    | 'for' expression ';' expression ';' expression bracketed_expr # For
    | 'return' expression? # Return
    | 'loop' bracketed_expr # Loop
    | 'map' '{' (map_init_entry ((',' | ';') map_init_entry)*)? '}' # MapInit
    | 'list' '[' (expression ((',' | ';') expression)*)? ']' # ListInit
    | ';' # Nop

    | string # StringExpr
    | number # NumberExpr
    | bracketed_expr # Bracketed
    | identifier # AccessVar

    | expression '(' parametre_list ')' # CallFunc
    | expression '.' identifier # AccessValueSimple
    | expression '[' expression ']' # AccessValueComplex
    | expression ':' identifier # AccessMethodSimple
    | expression ':' '[' expression ']' # AccessMethodComplex
    | <assoc=right> 'let' identifier '=' expression # DeclVar

    | <assoc=right> '-' expression # UnaryNegate
    | expression multiplicative_operator expression # OpMul
    | expression additive_operator expression # OpAdd
    | expression relational_operator expression # OpRel
    | expression equality_operator expression # OpEq
    | expression '&&' expression # OpLogicalAnd
    | expression '||' expression # OpLogicalOr
    | <assoc=right> expression assignment_operator expression # OpAssignment
    ;


expression_list: expression*;