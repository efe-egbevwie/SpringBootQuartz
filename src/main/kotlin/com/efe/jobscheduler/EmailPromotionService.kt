package com.efe.jobscheduler

import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class EmailPromotionService(
    private val quartzScheduler: Scheduler
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val recipients: List<Recipient> = buildList {
        add(Recipient(userId = 1, name = "John Smith", emailAddress = "johnSmith@exampleEmail.com"))
        add(Recipient(userId = 2, name = "Sarah Connor", emailAddress = "sarahConnor@exampleEmail.com"))
        add(Recipient(userId = 3, name = "Mario Appleseed", emailAddress = "marioAppleseed@exampleEmail.com"))
        add(Recipient(userId = 4, name = "Anthony Taylor", emailAddress = "anthonytaylor@exampleEmail.com"))
        add(Recipient(userId = 5, name = "John Reese", emailAddress = "johnReese@exampleEmail.com"))
    }


    fun scheduleEmailJobs() {
        recipients.forEach { recipient ->
            createAndTriggerEmailJobToRecipient(recipient)
        }
    }

    private fun createAndTriggerEmailJobToRecipient(recipient: Recipient) {

        val jobData = JobDataMap().apply {
            put(RECIPIENT_ID_KEY, recipient.userId.toString())
            put(RECIPIENT_NAME_KEY, recipient.name)
            put(RECIPIENT_EMAIL_ADDRESS_KEY, recipient.emailAddress)
            putAsString(EMAIL_PROMOTION_JOB_ATTEMPTS_COUNT, 1) //1
        }

        val job: JobDetail = JobBuilder
            .newJob(EmailPromotionJob::class.java)
            .withIdentity(recipient.userId.toString())
            .usingJobData(jobData)
            .requestRecovery(true)
            .storeDurably(true)
            .withDescription("Send promotional email to ${recipient.name}")
            .build()

        val jobStartTime = Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))

        val scheduledFutureTime = jobStartTime.time - Date().time

        logger.info("scheduling job to start at -> ${TimeUnit.MILLISECONDS.toMinutes(scheduledFutureTime)} minutes from now")

        val jobTrigger: Trigger =
            TriggerBuilder
                .newTrigger()
                .withIdentity("Trigger for Promotional email to ${recipient.name}")
                .forJob(job)
                .startAt(jobStartTime)
                .build()


        quartzScheduler.scheduleJob(job, jobTrigger)
        quartzScheduler.start()
    }
}