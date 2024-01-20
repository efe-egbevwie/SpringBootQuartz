package com.efe.jobscheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JobSchedulerApplication

fun main(args: Array<String>) {
    val applicationContext = runApplication<JobSchedulerApplication>(*args)

    val emailPromotionService: EmailPromotionService = applicationContext.getBean(EmailPromotionService::class.java)

    emailPromotionService.scheduleEmailJobs()
}
