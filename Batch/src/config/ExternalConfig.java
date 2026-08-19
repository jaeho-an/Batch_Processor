package config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(
    basePackages = "repository.external",
    sqlSessionTemplateRef = "externalSqlSessionTemplate"
)
public class ExternalConfig {
	
    @Bean
    @ConfigurationProperties(prefix = "database.external")
    public DataSource externalDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    public SqlSessionFactory externalSqlSessionFactory(@Qualifier("externalDataSource") DataSource dataSource) throws Exception {
    	
    	SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    	factory.setDataSource(dataSource);
    	factory.setMapperLocations(new PathMatchingResourcePatternResolver()
    				.getResources(
    						"classpath:/db/repository/external/*.xml"
    				)
    	);
    	return factory.getObject();
    }
    
    @Bean
    public SqlSessionTemplate externalSqlSessionTemplate(@Qualifier("externalSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
    	return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    @Bean
    public DataSourceTransactionManager externalTransactionManager(@Qualifier("externalDataSource") DataSource dataSource) {
    	return new DataSourceTransactionManager(dataSource);
    }
}