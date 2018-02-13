package dust.service.db.dict.express;

import dust.service.db.dict.DataObjColumn;
import dust.service.db.dict.DataObjRow;
import org.apache.commons.lang3.StringUtils;

/**
 * @author huangshengtao on 2018-1-18.
 */
public class ExprItemLTEQ extends ExprItem {

    @Override
    public boolean check(DataObjRow row) {
        DataObjColumn col = row.getDataObj().getColumn(this.left);
        Object srcVal = col.toValue(row.getValue(this.left));
        Object destVal = null;
        if (StringUtils.equals(this.right, TokenType.NULL.name)) {
            destVal = col.toValue(this.right);
        }

        return compare(srcVal, destVal) <= 0;
    }
}
