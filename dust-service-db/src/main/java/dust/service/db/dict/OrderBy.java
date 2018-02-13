package dust.service.db.dict;

/**
 * @author huangshengtao on 2017-12-18.
 */
public class OrderBy {
    private String columnLabel;
    private OrderByType orderByType;

    public String getColumnLabel() {
        return columnLabel;
    }

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public OrderByType getOrderByType() {
        return orderByType;
    }

    public void setOrderByType(OrderByType orderByType) {
        this.orderByType = orderByType;
    }
}
