package com.jdailer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jdailer.core.database.converter.HistoryTypeConverters
import com.jdailer.core.database.dao.CallRecordingDao
import com.jdailer.core.database.dao.CommunicationEventDao
import com.jdailer.core.database.dao.ContactDao
import com.jdailer.core.database.dao.CallerLookupDao
import com.jdailer.core.database.dao.ContactNoteDao
import com.jdailer.core.database.dao.ContactTagDao
import com.jdailer.core.database.dao.ExternalConversationLinkDao
import com.jdailer.core.database.dao.MessageDao
import com.jdailer.core.database.dao.SpamDao
import com.jdailer.core.database.entity.CallerLookupEntity
import com.jdailer.core.database.entity.CallRecordingEntity
import com.jdailer.core.database.entity.CommunicationEventEntity
import com.jdailer.core.database.entity.ExternalConversationLinkEntity
import com.jdailer.core.database.entity.ContactEntity
import com.jdailer.core.database.entity.PhoneNumberEntity
import com.jdailer.core.database.entity.MessageItemEntity
import com.jdailer.core.database.entity.MessageThreadEntity
import com.jdailer.core.database.entity.ContactNoteEntity
import com.jdailer.core.database.entity.ContactTagEntity
import com.jdailer.core.database.entity.SpamProfileEntity

@Database(
    entities = [
        ContactEntity::class,
        PhoneNumberEntity::class,
        CommunicationEventEntity::class,
        SpamProfileEntity::class,
        CallRecordingEntity::class,
        MessageThreadEntity::class,
        MessageItemEntity::class,
        ExternalConversationLinkEntity::class,
        CallerLookupEntity::class,
        ContactTagEntity::class,
        ContactNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(HistoryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun communicationEventDao(): CommunicationEventDao
    abstract fun spamDao(): SpamDao
    abstract fun callRecordingDao(): CallRecordingDao
    abstract fun messageDao(): MessageDao
    abstract fun externalConversationLinkDao(): ExternalConversationLinkDao
    abstract fun callerLookupDao(): CallerLookupDao
    abstract fun contactTagDao(): ContactTagDao
    abstract fun contactNoteDao(): ContactNoteDao

    companion object {
        const val NAME = "communications_hub.db"
    }
}
