package com.efe.jobscheduler

import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

const val RECIPIENT_NAME_KEY = "recipient_name"
const val RECIPIENT_EMAIL_ADDRESS_KEY = "recipient_email_Address"
const val RECIPIENT_ID = "recipient_id"

class EmailPromotionJob : Job { //1

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun execute(context: JobExecutionContext) { //2
        val jobDataMap: JobDataMap? = context.mergedJobDataMap //3

        if (jobDataMap == null) { //4
            logger.error("jobData not found")
            return
        }

        //5
        val recipientName = jobDataMap.getString(RECIPIENT_NAME_KEY)
        val recipientEmailAddress = jobDataMap.getString(RECIPIENT_EMAIL_ADDRESS_KEY)
        val recipientId = jobDataMap.getString(RECIPIENT_ID)

        //6
        if (recipientName.isNullOrBlank() || recipientEmailAddress.isNullOrBlank() || recipientId.isNullOrBlank()) {
            logger.error("recipient details not found. exiting....")
            return
        }

        //7
        val recipient =
            Recipient(userId = recipientId.toLong(), name = recipientName, emailAddress = recipientEmailAddress)

        sendEmailsToRecipient(recipient)


    }

    //8
    private fun sendEmailsToRecipient(recipient: Recipient) {
        // logic to send out email
        logger.info("sending promotional email to -> ${recipient.emailAddress}")
    }

}

