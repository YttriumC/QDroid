package ng.i.sav.qdroid.infra.persistence

import jakarta.persistence.EntityManagerFactory
import org.h2.jdbcx.JdbcDataSource
import org.hibernate.dialect.H2Dialect
import org.hibernate.jpa.HibernatePersistenceProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaDialect
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.io.File
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager"
)
@ComponentScan
open class H2Configuration {

    @Bean
    open fun dataSource(): DataSource {
//        var file = File(this::class.java.protectionDomain.codeSource.location.file).parentFile
        var file = File(".")
        file = File("db")

        file.mkdirs()
        if (!file.exists()) {
            throw Error("Create database folder failure")
        }
        val dbUrl = "jdbc:h2:file:${file.absolutePath.removeSuffix("/").removeSuffix("\\")}/qdroid"
        val username = "amoeba"
        val password = "amoeba"
        return JdbcDataSource().apply {
            this.user = username
            this.password = password
            this.setUrl(dbUrl)
        }
    }

    @Bean
    open fun entityManagerFactory(
        dataSource: DataSource,
        jpaVendorAdapter: HibernateJpaVendorAdapter
    ): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.setEntityManagerFactoryInterface(EntityManagerFactory::class.java)
            this.dataSource = dataSource
            this.jpaVendorAdapter = jpaVendorAdapter
            this.setPackagesToScan("ng.i.sav.qdroid.infra.persistence")
            persistenceProvider = HibernatePersistenceProvider()
            jpaDialect = HibernateJpaDialect()
        }
    }

    @Bean
    open fun jpaVendorAdapter(): HibernateJpaVendorAdapter {
        return HibernateJpaVendorAdapter().apply {
            this.setDatabase(Database.H2)
            this.setGenerateDdl(true)
            this.setShowSql(true)
            this.setDatabasePlatform(H2Dialect::class.java.name)
        }
    }

    @Bean
    open fun transactionManager(entityManagerFactoryBean: LocalContainerEntityManagerFactoryBean): JpaTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactoryBean.`object`
        }
    }
}
