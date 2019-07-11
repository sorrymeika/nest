
package cn.sonoframework.cornerstone.nest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import cn.sonoframework.cornerstone.nest.mapper.UserMapper;

@Configuration
@MapperScan(basePackageClasses = { UserMapper.class }, sqlSessionFactoryRef = "defaultSqlFactory")
@EnableTransactionManagement
public class DalConfig {

	private static final String MODEL_PACKAGE_PATH = "cn.sonoframework.cornerstone.nest";

	@Value(value = "classpath:mybatis/sqlmap/*.xml")
	private Resource[] mapperLocations;

	@Value(value = "classpath:mybatis/mybatis-config.xml")
	private Resource configLocation;

	@javax.annotation.Resource
	private DataSource dataSource;

	@Bean(initMethod = "init", destroyMethod = "close")
	@Autowired
	public DataSource dataSource(@Value("${spring.datasource.url}") String url,
			@Value("${spring.datasource.username}") String username,
			@Value("${spring.datasource.password}") String password,
			@Value("${spring.datasource.druid.removeAbandoned}") Boolean removeAbandoned) throws SQLException {

		return createDataSource(url, username, password, removeAbandoned);
	}

	@Bean(autowire = Autowire.BY_NAME)
	public SqlSessionFactoryBean defaultSqlFactory() {

		SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
		ssfb.setMapperLocations(mapperLocations);
		ssfb.setConfigLocation(configLocation);
		ssfb.setTypeAliasesPackage(MODEL_PACKAGE_PATH);

		ssfb.setDataSource(dataSource);

		return ssfb;
	}

	private DataSource createDataSource(String url, String username, String password, boolean removeAbandoned)
			throws SQLException {
		// 详细配置见
		// https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8

		DruidDataSource ds = new DruidDataSource();
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		// 最小连接池数量
		ds.setMinIdle(1);
		// 最大连接池数量
		ds.setMaxActive(60);
		ds.setInitialSize(1);
		// 获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
		ds.setMaxWait(60000);// 60s

		// 连接失败等待时间
		ds.setTimeBetweenConnectErrorMillis(30 * 1000);

		// 用来检测连接是否有效的sql，要求是一个查询语句，常用select
		// 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
		ds.setValidationQuery("select 1 FROM DUAL");
		// 单位：秒，检测连接是否有效的超时时间。底层调用jdbc Statement对象的void setQueryTimeout(int
		// seconds)方法
		ds.setValidationQueryTimeout(30);

		// 连接保持空闲而不被驱逐的最长时间
		ds.setMinEvictableIdleTimeMillis(300000);
		// 默认60s
		// 有两个含义：
		// 1) Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。
		// 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
		ds.setTimeBetweenEvictionRunsMillis(60 * 1000);
		// 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
		ds.setTestWhileIdle(true);

		// 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		ds.setTestOnBorrow(false);

		// 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
		ds.setTestOnReturn(false);

		// 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。
		ds.setPoolPreparedStatements(true);
		// 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
		// 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
		ds.setMaxPoolPreparedStatementPerConnectionSize(100);

		ds.setFilters("config");
		Properties properties = new Properties();
		properties.put("config.decrypt", "true");
		// 密码加密
		// ds.setConnectProperties(properties);

		// Set the abandoned connection check
		ds.setRemoveAbandoned(removeAbandoned);
		ds.setRemoveAbandonedTimeoutMillis(180 * 1000); // 180s
		ds.setLogAbandoned(true);

		StatFilter statFilter = new StatFilter();
		statFilter.setSlowSqlMillis(10000);// 10s。。慢
		statFilter.setMergeSql(true);
		statFilter.setLogSlowSql(true);

		List<Filter> filterList = new ArrayList<Filter>();
		filterList.add(statFilter);
		ds.setProxyFilters(filterList);

		return ds;
	}
}
