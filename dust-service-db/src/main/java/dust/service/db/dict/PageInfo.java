package dust.service.db.dict;

/**
 * 分页信息。
 * start小于0，默认第一页，第一行地址为0。pageSize大于时，才会启用分页
 * @author huangshengtao on 2017-12-18.
 */
public class PageInfo {
    private int start;
    private int pageSize;
    private int totalRows;

    public PageInfo() {

    }

    public PageInfo(int start, int pageSize) {
        this.start = start;
        this.pageSize = pageSize;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
}
