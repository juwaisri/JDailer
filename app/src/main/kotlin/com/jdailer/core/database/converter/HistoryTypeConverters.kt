package com.jdailer.core.database.converter

import androidx.room.TypeConverter
import com.jdailer.core.database.enums.HistoryDirection
import com.jdailer.core.database.enums.HistorySource

class HistoryTypeConverters {
    @TypeConverter
    fun fromHistorySource(value: HistorySource?): String? = value?.name

    @TypeConverter
    fun toHistorySource(value: String?): HistorySource = when (value) {
        HistorySource.CALL.name -> HistorySource.CALL
        HistorySource.SMS.name -> HistorySource.SMS
        HistorySource.MMS.name -> HistorySource.MMS
        HistorySource.WHATSAPP.name -> HistorySource.WHATSAPP
        HistorySource.TELEGRAM.name -> HistorySource.TELEGRAM
        HistorySource.SIGNAL.name -> HistorySource.SIGNAL
        HistorySource.EMAIL.name -> HistorySource.EMAIL
        else -> HistorySource.UNKNOWN
    }

    @TypeConverter
    fun fromHistoryDirection(value: HistoryDirection?): String? = value?.name

    @TypeConverter
    fun toHistoryDirection(value: String?): HistoryDirection = when (value) {
        HistoryDirection.INCOMING.name -> HistoryDirection.INCOMING
        HistoryDirection.OUTGOING.name -> HistoryDirection.OUTGOING
        HistoryDirection.MISSED.name -> HistoryDirection.MISSED
        HistoryDirection.UNKNOWN.name -> HistoryDirection.UNKNOWN
        else -> HistoryDirection.UNKNOWN
    }
}
