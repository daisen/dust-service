package dust.service.db.pool;

import dust.service.db.DustDbRuntimeException;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * 动态数据源类
 * 用于根据具体情况切换至所需的数据源
 * @author huangshengtao
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final ApplicationContext context;
    DataSourceTemplate dataSourceTemplate;

    public DynamicDataSource(ApplicationContext context) {
        this.context = context;
    }

    private DataSourceTemplate getTemplate() {
        //第一次，检查bean（dataSourceTemplate）是否存在
        if (this.dataSourceTemplate == null) {
            if (context.containsBean("dataSourceTemplate")) {
                this.dataSourceTemplate = (DataSourceTemplate) context.getBean("dataSourceTemplate");
            }


            //第二次，检查bean（druidTemplate）是否存在
            if (this.dataSourceTemplate == null && context.containsBean("druidTemplate")) {
                this.dataSourceTemplate = (DataSourceTemplate) context.getBean("druidTemplate");
            }

            //第三次，如果为空，则异常中断
            if (this.dataSourceTemplate == null) {
                throw new DustDbRuntimeException("没有找到对应的DataSourceTemplate");
            }
        }
        return dataSourceTemplate;
    }

    public DataSource createDataSource(DataSourceContext context) {
        return getTemplate().createDataSource(context);
    }

    public DataSource getDataSource(String name) {
        return DataSourceCache.getInstance().get(name);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        DataSourceContext context = DynamicDataSourceHolder.getContext();
        DataSource ds = getTemplate().getDataSource(context.getName());
        if (ds == null) {
            throw new DustDbRuntimeException("DynamicDataSource无法找到指定的数据源");
        }

        return ds;
    }

    @Override
    public void afterPropertiesSet() {
//        this.dataSourceTemplate = (DruidTemplate) BeanUtils.getBean("dataSourceTemplate");
    }
}
