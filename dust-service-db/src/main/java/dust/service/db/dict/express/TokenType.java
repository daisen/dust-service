package dust.service.db.dict.express;

/**
 * @author huangshengtao on 2018-1-17.
 */
public enum TokenType {
    NULL("NULL"),
    NOT("NOT"),
    FUNCTION("FUNCTION"),

    FOR("FOR"),
    IF("IF"),

    AND("AND"),
    OR("OR"),

    // mysql
    TRUE("TRUE"),
    FALSE("FALSE"),

    IDENTIFIER,
    VARIANT,
    LITERAL_INT,
    LITERAL_FLOAT,
    LITERAL_HEX,
    LITERAL_CHARS,
    LITERAL_NCHARS,
    LITERAL_ALIAS,

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMI(";"),
    COMMA(","),
    DOT("."),
    DOTDOT(".."),
    DOTDOTDOT("..,"),
    EQ("="),
    GT(">"),
    LT("<"),
    BANG("!"),
    BANGBANG("!!"),
    BANG_TILDE("!~"),
    BANG_TILDE_STAR("!~*"),
    TILDE("~"),
    TILDE_STAR("~*"),
    TILDE_EQ("~="),
    QUES("?"),
    COLON(":"),
    COLONCOLON("::"),
    COLONEQ(":="),
    EQEQ("=="),
    LTEQ("<="),
    LTEQGT("<=>"),
    LTGT("<>"),
    GTEQ(">="),
    BANGEQ("!="),
    BANGGT("!>"),
    BANGLT("!<"),
    AMPAMP("&&"),
    BARBAR("||"),
    BARBARSLASH("||/"),
    BARSLASH("|/"),
    PLUS("+"),
    SUB("-"),
    SUBGT("->"),
    SUBGTGT("->>"),
    STAR("*"),
    SLASH("/"),
    AMP("&"),
    BAR("|"),
    CARET("^"),
    PERCENT("%"),
    LTLT("<<"),
    GTGT(">>"),
    MONKEYS_AT("@"),
    MONKEYS_AT_AT("@@"),
    POUND("#"),
    POUNDGT("#>"),
    POUNDGTGT("#>>"),
    MONKEYS_AT_GT("@>"),
    LT_MONKEYS_AT("<@"),
    EOF;

    public final String name;

    TokenType(){
        this(null);
    }

    TokenType(String name){
        this.name = name;
    }
}
