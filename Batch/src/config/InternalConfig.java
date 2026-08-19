package config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(
    basePackages = "repository.internal",
    sqlSessionTemplateRef = "internalSqlSessionTemplate"
)
public class InternalConfig {
	
	@Bean
	@ConfigurationProperties(prefix = "database.internal")
	public DataSource internalDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	public SqlSessionFactory internalSqlSessionFactory(@Qualifier("internalDataSource") DataSource dataSource) throws Exception {
		
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setMapperLocations(new PathMatchingResourcePatternResolver()
					.getResources(
							"classpath:/db/repository/internal/*.xml"
					)
		);
		return factory.getObject();
	}
	
	@Bean
	public SqlSessionTemplate internalSqlSessionTemplate(@Qualifier("internalSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
	@Bean
	public DataSourceTransactionManager internalTransactionManager(@Qualifier("internalDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}