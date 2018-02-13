package dust.service.db.dict.express;

import dust.service.db.dict.DataObjRow;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author huangshengtao on 2018-1-18.
 */
public abstract class ExprItem {
    protected String left;
    protected String right;
    protected boolean require;

    public abstract boolean check(DataObjRow row);

    protected int compare(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            String clsName = b.getClass().getSimpleName();
            switch (clsName) {
                case "Integer":
                    return Integer.compare((Integer) a, (Integer) b);
                case "Double":
                    return Double.compare((Double) a, (Double) b);
                case "BigDecimal":
                    return reverse(((BigDecimal) b).compareTo((BigDecimal) a));
                case "String":
                    return StringUtils.compare((String) a, (String) b);
                case "Date":
                    return reverse(((BigDecimal) b).compareTo((BigDecimal) a));
                default:
                    throw new ExpressException("value can not calc compare result");

            }
        } else {
            String clsName = b.getClass().getSimpleName();
            switch (clsName) {
                case "Integer":
                    return Integer.compare((Integer) a, (Integer) b);
                case "Double":
                    return Double.compare((Double) a, (Double) b);
                case "BigDecimal":
                    return ((BigDecimal) a).compareTo((BigDecimal) b);
                case "String":
                    return StringUtils.compare((String) a, (String) b);
                case "Date":
                    return ((BigDecimal) a).compareTo((BigDecimal) b);
                default:
                    throw new ExpressException("value can not calc compare result");

            }
        }
    }

    private int reverse(int compareResult) {
        if (compareResult > 0) {
            compareResult = -1;
        } else if (compareResult < 0) {
            compareResult = 1;
        }
        return compareResult;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }
}
