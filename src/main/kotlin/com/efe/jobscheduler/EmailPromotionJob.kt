package com.efe.jobscheduler

import org.quartz.*
import org.slf4j.LoggerFactory

const val RECIPIENT_NAME_KEY = "recipient_name"
const val RECIPIENT_EMAIL_ADDRESS_KEY = "recipient_email_Address"
const val RECIPIENT_ID_KEY = "recipient_id"
const val EMAIL_PROMOTION_JOB_ATTEMPTS_COUNT = "job_attempts_count"

class EmailPromotionJob : Job {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun execute(context: JobExecutionContext) {
        try {
            val jobDataMap: JobDataMap? = context.mergedJobDataMap

            if (jobDataMap == null) {
                logger.error("jobData not found")
                return
            }


            val recipientName = jobDataMap.getString(RECIPIENT_NAME_KEY)
            val recipientEmailAddress = jobDataMap.getString(RECIPIENT_EMAIL_ADDRESS_KEY)
            val recipientId = jobDataMap.getString(RECIPIENT_ID_KEY)

            //1
            addJobListener(jobId = recipientId, scheduler = context.scheduler)

            if (recipientName.isNullOrBlank() || recipientEmailAddress.isNullOrBlank() || recipientId.isNullOrBlank()) {
                logger.error("recipient details not found. exiting....")
                return
            }


            val recipient =
                Recipient(userId = recipientId.toLong(), name = recipientName, emailAddress = recipientEmailAddress)

            sendEmailsToRecipient(recipient)

            //2
            removeJobListener(scheduler =  context.scheduler, jobId = recipientId)

        }catch (exception:Exception){ //1
            logger.error("exception executing email promotion job: ${exception.message}")
            val jobExecutionException = JobExecutionException(exception) //2
            throw jobExecutionException //4
        }

    }

    //8
    private fun sendEmailsToRecipient(recipient: Recipient) {
        throw IllegalArgumentException("Bad email address format") //this code simulates a job that throws an exception during execution
        // logic to send out email
        logger.info("sending promotional email to -> ${recipient.emailAddress}")
    }


    private fun addJobListener(jobId: String, scheduler: Scheduler) {

        val jobMatcher = Matcher<JobKey> { jobKey ->
            jobKey.name == jobId
        }

        scheduler.listenerManager.addJobListener(
            EmailPromotionJobListener(),
            jobMatcher
        )


    }

    private fun removeJobListener(scheduler: Scheduler, jobId: String) {
        scheduler.listenerManager.removeJobListener(jobId)
        val jobListenerRemoved = scheduler.listenerManager.jobListeners.find {
            it.name == jobId
        } == null

        logger.info("job listener removed -> $jobListenerRemoved")
    }


}

