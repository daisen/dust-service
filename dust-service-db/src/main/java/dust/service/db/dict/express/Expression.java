package dust.service.db.dict.express;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import dust.service.db.dict.DataObj;
import dust.service.db.dict.DataObjRow;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author huangshengtao on 2018-1-17.
 */
public class Expression {
    private final DataObj dataObj;
    private final String expr;
    private List<ExprItem> exprItems = Lists.newArrayList();
    private ExprItem curItem;
    private boolean require = true;
    private String columnName;
    private Lexer lexer;

    public Expression(DataObj obj, String expr) {
        checkNotNull(obj);
        this.dataObj = obj;
        this.expr = expr;
    }

    private void parseExpr() {
        lexer = new Lexer(this.expr);

        for (; ; ) {
            lexer.nextToken();
            if (lexer.isEOF()) {
                break;
            }

            if (lexer.getToken() == TokenType.AND
                    || lexer.getToken() == TokenType.OR
                    ) {
                if (exprItems.size() == 0) {
                    throw new ExpressException("syntax error");
                }

                require = lexer.getToken() == TokenType.AND;

            }


            if (lexer.getToken() == TokenType.IDENTIFIER) {
                columnName = lexer.stringVal;
            } else {
                throw new ExpressException("syntax error");
            }

            lexer.nextToken();
            if (lexer.isEOF()) {
                throw new ExpressException("syntax error");
            }

            switch (lexer.getToken()) {
                case GT:
                    curItem = new ExprItemGT();
                    exprItems.add(curItem);
                    break;
                case EQ:
                    curItem = new ExprItemEQ();
                    exprItems.add(curItem);
                    break;
                case BANGEQ:
                    curItem = new ExprItemBANGEQ();
                    exprItems.add(curItem);
                    break;
                case GTEQ:
                    curItem = new ExprItemGTEQ();
                    exprItems.add(curItem);
                    break;
                case LTEQ:
                    curItem = new ExprItemGTEQ();
                    exprItems.add(curItem);
                    break;
                default:
                    throw new ExpressException("operation not supported");
            }

            curItem.setRequire(require);
            curItem.setLeft(columnName);

            lexer.nextToken();
            if (lexer.isEOF()) {
                throw new ExpressException("syntax error");
            }

            switch (lexer.getToken()) {
                case NULL:
                    curItem.setRight(null);
                    break;
                case IDENTIFIER:
                case LITERAL_CHARS:
                    curItem.setRight(lexer.stringVal);
                    break;
                case LITERAL_INT:
                case LITERAL_FLOAT:
                    curItem.setRight(lexer.numberString());
                default:
                    throw new ExpressException("value type not support");

            }

        }

    }

    public List<DataObjRow> execute() {
        parseExpr();
        List<DataObjRow> tempRows = Lists.newArrayList();
        for (DataObjRow r : dataObj.getRows()) {
            if (check(r)) {
                tempRows.add(r);
            }
        }

        return tempRows;
    }

    public void execute(Function<DataObjRow, Boolean> iterator) {
        parseExpr();
        for (DataObjRow r : dataObj.getRows()) {
            if (check(r)) {
                if (!iterator.apply(r)) {
                    break;
                }
            }
        }
    }

    private boolean check(DataObjRow r) {
        boolean ret = true;
        for (ExprItem item : exprItems) {
            if (!item.isRequire() && item.check(r)) {
                return true;
            }

            ret = ret && item.check(r);
            if (!ret) {
                return false;
            }
        }

        return ret;
    }

}
