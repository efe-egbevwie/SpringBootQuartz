package com.efe.jobscheduler

import org.quartz.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EmailPromotionJobListener : JobListener { //1

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun getName(): String {
        return "Email promotion job listener"
    }

    override fun jobToBeExecuted(context: JobExecutionContext?) {
        logger.info("email promotion job about to be executed")
    }

    override fun jobExecutionVetoed(context: JobExecutionContext?) {
        logger.info("email promotion job vetoed")
    }

    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) { //2
        if (jobException == null) { //3
            logger.info("job executed successfully")
            return
        }

        logger.info("job encountered an exception: $jobException")

        rescheduleEmailJob(context)
    }


    private fun rescheduleEmailJob(context: JobExecutionContext) {
        logger.info("email promotion job threw an error, attempting to reschedule")

        try {
            val scheduler: Scheduler = context.scheduler

            val oldJobData: JobDataMap = context.mergedJobDataMap //4


            val jobAttemptCount: Int = oldJobData.getString(EMAIL_PROMOTION_JOB_ATTEMPTS_COUNT).toInt() //5

            val nextExecutionTime: Duration = getNextExecutionTime(jobAttemptCount) //6
            val nextExecutionDateTime = Date.from(Instant.now().plusSeconds(nextExecutionTime.inWholeSeconds)) //7

            val newJobData: JobDataMap = oldJobData.apply {
                val newJobAttemptCount = jobAttemptCount.plus(1)
                this[EMAIL_PROMOTION_JOB_ATTEMPTS_COUNT] = newJobAttemptCount.toString()
            } //8

            val oldJobTrigger = context.trigger

            val newJobTrigger = TriggerBuilder.newTrigger() //9
                .forJob(context.jobDetail)
                .withIdentity(oldJobTrigger.key.name)
                .startAt(nextExecutionDateTime)
                .usingJobData(newJobData)
                .build()





            if (jobAttemptCount > 5) { //10
                logger.info("job has run for max attempts....pausing job")
                pauseEmailPromotionJob(context = context, scheduler = scheduler)
                    .onSuccess {
                        logger.info("job with ID: ${newJobTrigger.key.name} paused")
                    }
                    .onFailure { error ->
                        logger.error("failed to pause job with ID: ${newJobTrigger.key.name} due to  $error")
                        error.printStackTrace()
                    }

                return
            }

            scheduler.rescheduleJob(oldJobTrigger.key, newJobTrigger) //11


            val newExecutionTimeFormatted = nextExecutionTime.toComponents { days, hours, minutes, seconds, _ ->
                "$days day(s), $hours hour(s), $minutes minute(s), $seconds second(s)"
            }

            logger.info("job rescheduled in duration -> $newExecutionTimeFormatted")


        } catch (e: Exception) {
            logger.error("failed to reschedule job due to exception: $e")
            e.printStackTrace()
        }
    }


    private fun getNextExecutionTime(jobAttemptCount: Int): Duration {
        return when (jobAttemptCount) {
            1 -> 5.seconds
            2 -> 10.seconds
            3 -> 15.seconds
            4 -> 20.seconds
            5 -> 25.seconds
            else -> 1.minutes
        }
    }


    private fun pauseEmailPromotionJob(context: JobExecutionContext, scheduler: Scheduler) = runCatching {
        logger.info("pausing job with key -> ${context.jobDetail.key.name}")
        scheduler.pauseJob(context.jobDetail.key)

        val jobTrigger = context.trigger
        val currentJobState = scheduler.getTriggerState(jobTrigger.key)

        logger.info("job state -> ${currentJobState.name}")
        logger.info("job paused -> ${currentJobState == Trigger.TriggerState.PAUSED}")
    }


}