package dust.service.db.dict.express;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static dust.service.db.dict.express.CharTypes.isFirstIdentifierChar;
import static dust.service.db.dict.express.CharTypes.isIdentifierChar;
import static dust.service.db.dict.express.CharTypes.isWhitespace;

/**
 * @author huangshengtao on 2018-1-17.
 */
public class Lexer {

    protected final String text;
    protected int          pos;
    protected int          mark;

    protected char         ch;

    protected char[]       buf;
    protected int          bufPos;

    protected TokenType token;

    protected String       stringVal;

    private SavePoint      savePoint    = null;

    protected int            line         = 0;

    protected int            lines        = 0;

    public Lexer(String input){
        this.text = input;
        this.pos = -1;

        scanChar();
    }

    public final char charAt(int index) {
        if (index >= text.length()) {
            return ConstantCharaters.EOI;
        }

        return text.charAt(index);
    }

    public final String addSymbol() {
        return subString(mark, bufPos);
    }

    public final String subString(int offset, int count) {
        return text.substring(offset, offset + count);
    }

    protected void initBuff(int size) {
        if (buf == null) {
            if (size < 32) {
                buf = new char[32];
            } else {
                buf = new char[size + 32];
            }
        } else if (buf.length < size) {
            buf = Arrays.copyOf(buf, size);
        }
    }

    public void arraycopy(int srcPos, char[] dest, int destPos, int length) {
        text.getChars(srcPos, srcPos + length, dest, destPos);    }

    private static class SavePoint {

        int   bp;
        int   sp;
        int   np;
        char  ch;
        TokenType token;
    }

    public void mark() {
        SavePoint savePoint = new SavePoint();
        savePoint.bp = pos;
        savePoint.sp = bufPos;
        savePoint.np = mark;
        savePoint.ch = ch;
        savePoint.token = token;
        this.savePoint = savePoint;
    }

    public void reset() {
        this.pos = savePoint.bp;
        this.bufPos = savePoint.sp;
        this.mark = savePoint.np;
        this.ch = savePoint.ch;
        this.token = savePoint.token;
    }

    public Lexer(char[] input, int inputLength){
        this(new String(input, 0, inputLength));
    }

    protected final void scanChar() {
        ch = charAt(++pos);
    }

    protected void unscan() {
        ch = charAt(--pos);
    }

    public boolean isEOF() {
        return pos >= text.length();
    }

    /**
     * Return the current getToken, set by nextToken().
     */
    public final TokenType getToken() {
        return token;
    }

    public String info() {
        return this.token + " " + this.stringVal();
    }

    public final void nextToken() {
        bufPos = 0;
        this.lines = 0;
        int startLine = line;

        for (;;) {
            if (isWhitespace(ch)) {
                if (ch == '\n') {
                    line++;

                    lines = line - startLine;
                }

                scanChar();
                continue;
            }

            if (ch == '$' && charAt(pos + 1) == '{') {
                scanVariable();
                return;
            }

            if (isFirstIdentifierChar(ch)) {
                scanIdentifier();
                return;
            }

            switch (ch) {
                case '0':
                    if (charAt(pos + 1) == 'x') {
                        scanChar();
                        scanChar();
                        scanHexaDecimal();
                    } else {
                        scanNumber();
                    }
                    return;
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    scanNumber();
                    return;
                case ',':
                case '，':
                    scanChar();
                    token = TokenType.COMMA;
                    return;
                case '(':
                    scanChar();
                    token = TokenType.LPAREN;
                    return;
                case ')':
                    scanChar();
                    token = TokenType.RPAREN;
                    return;
                case '[':
                    scanLBracket();
                    return;
                case ']':
                    scanChar();
                    token = TokenType.RBRACKET;
                    return;
                case '{':
                    scanChar();
                    token = TokenType.LBRACE;
                    return;
                case '}':
                    scanChar();
                    token = TokenType.RBRACE;
                    return;
                case ':':
                    scanChar();
                    if (ch == '=') {
                        scanChar();
                        token = TokenType.COLONEQ;
                    } else if (ch == ':') {
                        scanChar();
                        token = TokenType.COLONCOLON;
                    } else {
                        unscan();
                        scanVariable();
                    }
                    return;
                case '.':
                    scanChar();
                    if (isDigit(ch) && !isFirstIdentifierChar(charAt(pos - 2))) {
                        unscan();
                        scanNumber();
                        return;
                    } else if (ch == '.') {
                        scanChar();
                        if (ch == '.') {
                            scanChar();
                            token = TokenType.DOTDOTDOT;
                        } else {
                            token = TokenType.DOTDOT;
                        }
                    } else {
                        token = TokenType.DOT;
                    }
                    return;
                case '\'':
                    scanString();
                    return;
                case '\"':
                    scanAlias();
                    return;
                case '*':
                    scanChar();
                    token = TokenType.STAR;
                    return;
                case '?':
                    scanChar();
                    token = TokenType.QUES;
                    return;
                case ';':
                    scanChar();
                    token = TokenType.SEMI;
                    return;
                case '`':
                    throw new ExpressException("TODO"); // TODO
                case '@':
                    scanVariable();
                    return;
                default:
                    if (Character.isLetter(ch)) {
                        scanIdentifier();
                        return;
                    }

                    if (isOperator(ch)) {
                        scanOperator();
                        return;
                    }

                    // QS_TODO ?
                    if (isEOF()) { // JLS
                        token = TokenType.EOF;
                    } else {
                        scanChar();
                    }

                    return;
            }
        }

    }

    protected void scanLBracket() {
        scanChar();
        token = TokenType.LBRACKET;
    }

    private final void scanOperator() {
        switch (ch) {
            case '+':
                scanChar();
                token = TokenType.PLUS;
                break;
            case '-':
                scanChar();
                if (ch == '>') {
                    scanChar();
                    if (ch == '>') {
                        scanChar();
                        token = TokenType.SUBGTGT;
                    } else {
                        token = TokenType.SUBGT;
                    }
                } else {
                    token = TokenType.SUB;
                }
                break;
            case '*':
                scanChar();
                token = TokenType.STAR;
                break;
            case '/':
                scanChar();
                token = TokenType.SLASH;
                break;
            case '&':
                scanChar();
                if (ch == '&') {
                    scanChar();
                    token = TokenType.AMPAMP;
                } else {
                    token = TokenType.AMP;
                }
                break;
            case '|':
                scanChar();
                if (ch == '|') {
                    scanChar();
                    if (ch == '/') {
                        scanChar();
                        token = TokenType.BARBARSLASH;
                    } else {
                        token = TokenType.BARBAR;
                    }
                } else if (ch == '/') {
                    scanChar();
                    token = TokenType.BARSLASH;
                } else {
                    token = TokenType.BAR;
                }
                break;
            case '^':
                scanChar();
                token = TokenType.CARET;
                break;
            case '%':
                scanChar();
                token = TokenType.PERCENT;
                break;
            case '=':
                scanChar();
                if (ch == '=') {
                    scanChar();
                    token = TokenType.EQEQ;
                } else {
                    token = TokenType.EQ;
                }
                break;
            case '>':
                scanChar();
                if (ch == '=') {
                    scanChar();
                    token = TokenType.GTEQ;
                } else if (ch == '>') {
                    scanChar();
                    token = TokenType.GTGT;
                } else {
                    token = TokenType.GT;
                }
                break;
            case '<':
                scanChar();
                if (ch == '=') {
                    scanChar();
                    if (ch == '>') {
                        token = TokenType.LTEQGT;
                        scanChar();
                    } else {
                        token = TokenType.LTEQ;
                    }
                } else if (ch == '>') {
                    scanChar();
                    token = TokenType.LTGT;
                } else if (ch == '<') {
                    scanChar();
                    token = TokenType.LTLT;
                } else if (ch == '@') {
                    scanChar();
                    token = TokenType.LT_MONKEYS_AT;
                } else {
                    token = TokenType.LT;
                }
                break;
            case '!':
                scanChar();
                if (ch == '=') {
                    scanChar();
                    token = TokenType.BANGEQ;
                } else if (ch == '>') {
                    scanChar();
                    token = TokenType.BANGGT;
                } else if (ch == '<') {
                    scanChar();
                    token = TokenType.BANGLT;
                } else if (ch == '!') {
                    scanChar();
                    token = TokenType.BANGBANG; // postsql
                } else if (ch == '~') {
                    scanChar();
                    if (ch == '*') {
                        scanChar();
                        token = TokenType.BANG_TILDE_STAR; // postsql
                    } else {
                        token = TokenType.BANG_TILDE; // postsql
                    }
                } else {
                    token = TokenType.BANG;
                }
                break;
            case '?':
                scanChar();
                token = TokenType.QUES;
                break;
            case '~':
                scanChar();
                if (ch == '*') {
                    scanChar();
                    token = TokenType.TILDE_STAR;
                } else if (ch == '=') {
                    scanChar();
                    token = TokenType.TILDE_EQ; // postsql
                } else {
                    token = TokenType.TILDE;
                }
                break;
            default:
                throw new ExpressException("TODO");
        }
    }

    protected void scanString() {
        mark = pos;
        boolean hasSpecial = false;

        for (;;) {
            if (isEOF()) {
                return;
            }

            ch = charAt(++pos);

            if (ch == '\'') {
                scanChar();
                if (ch != '\'') {
                    token = TokenType.LITERAL_CHARS;
                    break;
                } else {
                    if (!hasSpecial) {
                        initBuff(bufPos);
                        arraycopy(mark + 1, buf, 0, bufPos);
                        hasSpecial = true;
                    }
                    putChar('\'');
                    continue;
                }
            }

            if (!hasSpecial) {
                bufPos++;
                continue;
            }

            if (bufPos == buf.length) {
                putChar(ch);
            } else {
                buf[bufPos++] = ch;
            }
        }

        if (!hasSpecial) {
            stringVal = subString(mark + 1, bufPos);
        } else {
            stringVal = new String(buf, 0, bufPos);
        }
    }

    protected void scanAlias() {
        mark = pos;

        if (buf == null) {
            buf = new char[32];
        }

        boolean hasSpecial = false;
        for (;;) {
            if (isEOF()) {
                return;
            }

            ch = charAt(++pos);

            if (ch == '\"' && charAt(pos - 1) != '\\') {
                scanChar();
                token = TokenType.LITERAL_ALIAS;
                break;
            }

            if(ch == '\\') {
                scanChar();
                if (ch == '"') {
                    hasSpecial = true;
                } else {
                    unscan();
                }
            }

            if (bufPos == buf.length) {
                putChar(ch);
            } else {
                buf[bufPos++] = ch;
            }
        }

        if (!hasSpecial) {
            stringVal = subString(mark + 1, bufPos);
        } else {
            stringVal = new String(buf, 0, bufPos);
        }

        //stringVal = subString(mark + 1, bufPos);
    }

    protected final void scanAlias2() {
        {
            boolean hasSpecial = false;
            int startIndex = pos + 1;
            int endIndex = -1; // text.indexOf('\'', startIndex);
            for (int i = startIndex; i < text.length(); ++i) {
                final char ch = text.charAt(i);
                if (ch == '\\') {
                    hasSpecial = true;
                    continue;
                }
                if (ch == '"') {
                    endIndex = i;
                    break;
                }
            }

            if (endIndex == -1) {
                throw new ExpressException("unclosed str");
            }

            String stringVal = subString(startIndex, endIndex - startIndex);
            // hasSpecial = stringVal.indexOf('\\') != -1;

            if (!hasSpecial) {
                this.stringVal = stringVal;
                int pos = endIndex + 1;
                char ch = charAt(pos);
                if (ch != '\'') {
                    this.pos = pos;
                    this.ch = ch;
                    token = TokenType.LITERAL_CHARS;
                    return;
                }
            }
        }

        mark = pos;
        boolean hasSpecial = false;
        for (;;) {
            if (isEOF()) {
                return;
            }

            ch = charAt(++pos);

            if (ch == '\\') {
                scanChar();
                if (!hasSpecial) {
                    initBuff(bufPos);
                    arraycopy(mark + 1, buf, 0, bufPos);
                    hasSpecial = true;
                }

                switch (ch) {
                    case '0':
                        putChar('\0');
                        break;
                    case '\'':
                        putChar('\'');
                        break;
                    case '"':
                        putChar('"');
                        break;
                    case 'b':
                        putChar('\b');
                        break;
                    case 'n':
                        putChar('\n');
                        break;
                    case 'r':
                        putChar('\r');
                        break;
                    case 't':
                        putChar('\t');
                        break;
                    case '\\':
                        putChar('\\');
                        break;
                    case 'Z':
                        putChar((char) 0x1A); // ctrl + Z
                        break;
                    default:
                        putChar(ch);
                        break;
                }

                continue;
            }
            if (ch == '\"') {
                scanChar();
                token = TokenType.LITERAL_CHARS;
                break;
            }

            if (!hasSpecial) {
                bufPos++;
                continue;
            }

            if (bufPos == buf.length) {
                putChar(ch);
            } else {
                buf[bufPos++] = ch;
            }
        }

        if (!hasSpecial) {
            stringVal = subString(mark + 1, bufPos);
        } else {
            stringVal = new String(buf, 0, bufPos);
        }
    }

    public void scanSharp() {
        scanVariable();
    }

    public void scanVariable() {
        if (ch != '@' && ch != ':' && ch != '#' && ch != '$') {
            throw new ExpressException("illegal variable");
        }

        mark = pos;
        bufPos = 1;
        char vCh;

        final char c1 = charAt(pos + 1);
        if (c1 == '>' ) {
            pos += 2;
            token = TokenType.MONKEYS_AT_GT;
            this.ch = charAt(++pos);
            return;
        } else if (c1 == '{') {
            pos++;
            bufPos++;

            for (;;) {
                vCh = charAt(++pos);

                if (vCh == '}' || vCh == ConstantCharaters.EOI) {
                    break;
                }

                bufPos++;
                continue;
            }

            if (vCh != '}') {
                throw new ExpressException("syntax error");
            }
            ++pos;
            bufPos++;

            this.ch = charAt(pos);

            stringVal = addSymbol();
            token = TokenType.VARIANT;
            return;
        }

        for (;;) {
            vCh = charAt(++pos);

            if (!isIdentifierChar(vCh)) {
                break;
            }

            bufPos++;
            continue;
        }

        this.ch = charAt(pos);

        stringVal = addSymbol();
        token = TokenType.VARIANT;
    }



    public void scanIdentifier() {
        final char first = ch;

        final boolean firstFlag = isFirstIdentifierChar(first);
        if (!firstFlag) {
            throw new ExpressException("illegal identifier");
        }

        mark = pos;
        bufPos = 1;
        char ch;
        for (;;) {
            ch = charAt(++pos);

            if (!isIdentifierChar(ch)) {
                break;
            }

            bufPos++;
            continue;
        }

        this.ch = charAt(pos);

        stringVal = addSymbol();
        token = TokenType.IDENTIFIER;
    }

    public void scanNumber() {
        mark = pos;

        if (ch == '-') {
            bufPos++;
            ch = charAt(++pos);
        }

        for (;;) {
            if (ch >= '0' && ch <= '9') {
                bufPos++;
            } else {
                break;
            }
            ch = charAt(++pos);
        }

        boolean isDouble = false;

        if (ch == '.') {
            if (charAt(pos + 1) == '.') {
                token = TokenType.LITERAL_INT;
                return;
            }
            bufPos++;
            ch = charAt(++pos);
            isDouble = true;

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    bufPos++;
                } else {
                    break;
                }
                ch = charAt(++pos);
            }
        }

        if (ch == 'e' || ch == 'E') {
            bufPos++;
            ch = charAt(++pos);

            if (ch == '+' || ch == '-') {
                bufPos++;
                ch = charAt(++pos);
            }

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    bufPos++;
                } else {
                    break;
                }
                ch = charAt(++pos);
            }

            isDouble = true;
        }

        if (isDouble) {
            token = TokenType.LITERAL_FLOAT;
        } else {
            token = TokenType.LITERAL_INT;
        }
    }

    public void scanHexaDecimal() {
        mark = pos;

        if (ch == '-') {
            bufPos++;
            ch = charAt(++pos);
        }

        for (;;) {
            if (CharTypes.isHex(ch)) {
                bufPos++;
            } else {
                break;
            }
            ch = charAt(++pos);
        }

        token = TokenType.LITERAL_HEX;
    }

    public String hexString() {
        return subString(mark, bufPos);
    }

    public final boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Append a character to sbuf.
     */
    protected final void putChar(char ch) {
        if (bufPos == buf.length) {
            char[] newsbuf = new char[buf.length * 2];
            System.arraycopy(buf, 0, newsbuf, 0, buf.length);
            buf = newsbuf;
        }
        buf[bufPos++] = ch;
    }

    /**
     * Return the current getToken's position: a 0-based offset from beginning of the raw input stream (before unicode
     * translation)
     */
    public final int pos() {
        return pos;
    }

    /**
     * The value of a literal getToken, recorded as a string. For integers, leading 0x and 'l' suffixes are suppressed.
     */
    public final String stringVal() {
        return stringVal;
    }

    private boolean isOperator(char ch) {
        switch (ch) {
            case '!':
            case '%':
            case '&':
            case '*':
            case '+':
            case '-':
            case '<':
            case '=':
            case '>':
            case '^':
            case '|':
            case '~':
            case ';':
                return true;
            default:
                return false;
        }
    }

    private static final long  MULTMIN_RADIX_TEN   = Long.MIN_VALUE / 10;
    private static final long  N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;

    private final static int[] digits              = new int[(int) '9' + 1];

    static {
        for (int i = '0'; i <= '9'; ++i) {
            digits[i] = i - '0';
        }
    }

    // QS_TODO negative number is invisible for lexer
    public Number integerValue() {
        long result = 0;
        boolean negative = false;
        int i = mark, max = mark + bufPos;
        long limit;
        long multmin;
        int digit;

        if (charAt(mark) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i++;
        } else {
            limit = -Long.MAX_VALUE;
        }
        multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
        if (i < max) {
            digit = digits[charAt(i++)];
            result = -digit;
        }
        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digits[charAt(i++)];
            if (result < multmin) {
                return new BigInteger(numberString());
            }
            result *= 10;
            if (result < limit + digit) {
                return new BigInteger(numberString());
            }
            result -= digit;
        }

        if (negative) {
            if (i > mark + 1) {
                if (result >= Integer.MIN_VALUE) {
                    return (int) result;
                }
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException(numberString());
            }
        } else {
            result = -result;
            if (result <= Integer.MAX_VALUE) {
                return (int) result;
            }
            return result;
        }
    }

    public int bp() {
        return this.pos;
    }

    public char current() {
        return this.ch;
    }

    public void reset(int mark, char markChar, TokenType token) {
        this.pos = mark;
        this.ch = markChar;
        this.token = token;
    }

    public final String numberString() {
        return subString(mark, bufPos);
    }

    public BigDecimal decimalValue() {
        String value = subString(mark, bufPos);
        if (!StringUtils.isNumeric(value)){
            throw new ExpressException(value+" is not a number!");
        }
        return new BigDecimal(value.toCharArray());
    }

    public static interface CommentHandler {
        boolean handle(TokenType lastToken, String comment);
    }
    public int getLine() {
        return line;
    }
}
