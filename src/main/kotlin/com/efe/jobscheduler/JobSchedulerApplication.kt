package com.efe.jobscheduler

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@SpringBootApplication
class JobSchedulerApplication {

    @Bean
    fun dataSource(): DataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.apply {
            jdbcUrl = System.getenv("JOB_SCHEDULER_JDBC_URL")
            username = System.getenv("JOB_SCHEDULER_JDBC_USERNAME")
            password = System.getenv("JOB_SCHEDULER_JDBC_PASSWORD")
            driverClassName = "org.postgresql.Driver"
        }
        return HikariDataSource(hikariConfig)
    }
}

fun main(args: Array<String>) {
    val applicationContext = runApplication<JobSchedulerApplication>(*args)

    val emailPromotionService: EmailPromotionService = applicationContext.getBean(EmailPromotionService::class.java)

    emailPromotionService.scheduleEmailJobs()
}
