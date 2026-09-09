package com.animenotifier.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AnimeDao_Impl implements AnimeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AnimeEntity> __insertionAdapterOfAnimeEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfIncrementWatchedEpisode;

  private final SharedSQLiteStatement __preparedStmtOfUpdateNotificationSettings;

  public AnimeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAnimeEntity = new EntityInsertionAdapter<AnimeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `saved_anime` (`id`,`title`,`coverImage`,`bannerImage`,`synopsis`,`studio`,`durationMinutes`,`genres`,`averageScore`,`watchedEpisodes`,`totalEpisodes`,`nextEpisodeNumber`,`nextEpisodeAiringAt`,`airingDayOfWeek`,`status`,`siteUrl`,`notificationsEnabled`,`alertLeadTimeMinutes`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AnimeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getCoverImage());
        if (entity.getBannerImage() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBannerImage());
        }
        if (entity.getSynopsis() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getSynopsis());
        }
        if (entity.getStudio() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStudio());
        }
        if (entity.getDurationMinutes() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDurationMinutes());
        }
        if (entity.getGenres() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getGenres());
        }
        if (entity.getAverageScore() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getAverageScore());
        }
        statement.bindLong(10, entity.getWatchedEpisodes());
        if (entity.getTotalEpisodes() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getTotalEpisodes());
        }
        if (entity.getNextEpisodeNumber() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getNextEpisodeNumber());
        }
        if (entity.getNextEpisodeAiringAt() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getNextEpisodeAiringAt());
        }
        if (entity.getAiringDayOfWeek() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getAiringDayOfWeek());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getStatus());
        }
        if (entity.getSiteUrl() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSiteUrl());
        }
        final int _tmp = entity.getNotificationsEnabled() ? 1 : 0;
        statement.bindLong(17, _tmp);
        statement.bindLong(18, entity.getAlertLeadTimeMinutes());
        statement.bindLong(19, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM saved_anime WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementWatchedEpisode = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE saved_anime SET watchedEpisodes = watchedEpisodes + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateNotificationSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE saved_anime SET notificationsEnabled = ?, alertLeadTimeMinutes = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final AnimeEntity anime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAnimeEntity.insert(anime);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementWatchedEpisode(final int id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementWatchedEpisode.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementWatchedEpisode.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNotificationSettings(final int id, final boolean enabled,
      final int leadTimeMinutes, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateNotificationSettings.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, leadTimeMinutes);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateNotificationSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AnimeEntity>> getAllSavedAnime() {
    final String _sql = "SELECT * FROM saved_anime ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"saved_anime"}, new Callable<List<AnimeEntity>>() {
      @Override
      @NonNull
      public List<AnimeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCoverImage = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImage");
          final int _cursorIndexOfBannerImage = CursorUtil.getColumnIndexOrThrow(_cursor, "bannerImage");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfStudio = CursorUtil.getColumnIndexOrThrow(_cursor, "studio");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfAverageScore = CursorUtil.getColumnIndexOrThrow(_cursor, "averageScore");
          final int _cursorIndexOfWatchedEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "watchedEpisodes");
          final int _cursorIndexOfTotalEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalEpisodes");
          final int _cursorIndexOfNextEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeNumber");
          final int _cursorIndexOfNextEpisodeAiringAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeAiringAt");
          final int _cursorIndexOfAiringDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "airingDayOfWeek");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSiteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "siteUrl");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final int _cursorIndexOfAlertLeadTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "alertLeadTimeMinutes");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<AnimeEntity> _result = new ArrayList<AnimeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AnimeEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCoverImage;
            _tmpCoverImage = _cursor.getString(_cursorIndexOfCoverImage);
            final String _tmpBannerImage;
            if (_cursor.isNull(_cursorIndexOfBannerImage)) {
              _tmpBannerImage = null;
            } else {
              _tmpBannerImage = _cursor.getString(_cursorIndexOfBannerImage);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpStudio;
            if (_cursor.isNull(_cursorIndexOfStudio)) {
              _tmpStudio = null;
            } else {
              _tmpStudio = _cursor.getString(_cursorIndexOfStudio);
            }
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpAverageScore;
            if (_cursor.isNull(_cursorIndexOfAverageScore)) {
              _tmpAverageScore = null;
            } else {
              _tmpAverageScore = _cursor.getInt(_cursorIndexOfAverageScore);
            }
            final int _tmpWatchedEpisodes;
            _tmpWatchedEpisodes = _cursor.getInt(_cursorIndexOfWatchedEpisodes);
            final Integer _tmpTotalEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalEpisodes)) {
              _tmpTotalEpisodes = null;
            } else {
              _tmpTotalEpisodes = _cursor.getInt(_cursorIndexOfTotalEpisodes);
            }
            final Integer _tmpNextEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeNumber)) {
              _tmpNextEpisodeNumber = null;
            } else {
              _tmpNextEpisodeNumber = _cursor.getInt(_cursorIndexOfNextEpisodeNumber);
            }
            final Long _tmpNextEpisodeAiringAt;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeAiringAt)) {
              _tmpNextEpisodeAiringAt = null;
            } else {
              _tmpNextEpisodeAiringAt = _cursor.getLong(_cursorIndexOfNextEpisodeAiringAt);
            }
            final Integer _tmpAiringDayOfWeek;
            if (_cursor.isNull(_cursorIndexOfAiringDayOfWeek)) {
              _tmpAiringDayOfWeek = null;
            } else {
              _tmpAiringDayOfWeek = _cursor.getInt(_cursorIndexOfAiringDayOfWeek);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSiteUrl;
            if (_cursor.isNull(_cursorIndexOfSiteUrl)) {
              _tmpSiteUrl = null;
            } else {
              _tmpSiteUrl = _cursor.getString(_cursorIndexOfSiteUrl);
            }
            final boolean _tmpNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp != 0;
            final int _tmpAlertLeadTimeMinutes;
            _tmpAlertLeadTimeMinutes = _cursor.getInt(_cursorIndexOfAlertLeadTimeMinutes);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new AnimeEntity(_tmpId,_tmpTitle,_tmpCoverImage,_tmpBannerImage,_tmpSynopsis,_tmpStudio,_tmpDurationMinutes,_tmpGenres,_tmpAverageScore,_tmpWatchedEpisodes,_tmpTotalEpisodes,_tmpNextEpisodeNumber,_tmpNextEpisodeAiringAt,_tmpAiringDayOfWeek,_tmpStatus,_tmpSiteUrl,_tmpNotificationsEnabled,_tmpAlertLeadTimeMinutes,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllSavedAnimeList(final Continuation<? super List<AnimeEntity>> $completion) {
    final String _sql = "SELECT * FROM saved_anime";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AnimeEntity>>() {
      @Override
      @NonNull
      public List<AnimeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCoverImage = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImage");
          final int _cursorIndexOfBannerImage = CursorUtil.getColumnIndexOrThrow(_cursor, "bannerImage");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfStudio = CursorUtil.getColumnIndexOrThrow(_cursor, "studio");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfAverageScore = CursorUtil.getColumnIndexOrThrow(_cursor, "averageScore");
          final int _cursorIndexOfWatchedEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "watchedEpisodes");
          final int _cursorIndexOfTotalEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalEpisodes");
          final int _cursorIndexOfNextEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeNumber");
          final int _cursorIndexOfNextEpisodeAiringAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeAiringAt");
          final int _cursorIndexOfAiringDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "airingDayOfWeek");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSiteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "siteUrl");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final int _cursorIndexOfAlertLeadTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "alertLeadTimeMinutes");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<AnimeEntity> _result = new ArrayList<AnimeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AnimeEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCoverImage;
            _tmpCoverImage = _cursor.getString(_cursorIndexOfCoverImage);
            final String _tmpBannerImage;
            if (_cursor.isNull(_cursorIndexOfBannerImage)) {
              _tmpBannerImage = null;
            } else {
              _tmpBannerImage = _cursor.getString(_cursorIndexOfBannerImage);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpStudio;
            if (_cursor.isNull(_cursorIndexOfStudio)) {
              _tmpStudio = null;
            } else {
              _tmpStudio = _cursor.getString(_cursorIndexOfStudio);
            }
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpAverageScore;
            if (_cursor.isNull(_cursorIndexOfAverageScore)) {
              _tmpAverageScore = null;
            } else {
              _tmpAverageScore = _cursor.getInt(_cursorIndexOfAverageScore);
            }
            final int _tmpWatchedEpisodes;
            _tmpWatchedEpisodes = _cursor.getInt(_cursorIndexOfWatchedEpisodes);
            final Integer _tmpTotalEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalEpisodes)) {
              _tmpTotalEpisodes = null;
            } else {
              _tmpTotalEpisodes = _cursor.getInt(_cursorIndexOfTotalEpisodes);
            }
            final Integer _tmpNextEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeNumber)) {
              _tmpNextEpisodeNumber = null;
            } else {
              _tmpNextEpisodeNumber = _cursor.getInt(_cursorIndexOfNextEpisodeNumber);
            }
            final Long _tmpNextEpisodeAiringAt;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeAiringAt)) {
              _tmpNextEpisodeAiringAt = null;
            } else {
              _tmpNextEpisodeAiringAt = _cursor.getLong(_cursorIndexOfNextEpisodeAiringAt);
            }
            final Integer _tmpAiringDayOfWeek;
            if (_cursor.isNull(_cursorIndexOfAiringDayOfWeek)) {
              _tmpAiringDayOfWeek = null;
            } else {
              _tmpAiringDayOfWeek = _cursor.getInt(_cursorIndexOfAiringDayOfWeek);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSiteUrl;
            if (_cursor.isNull(_cursorIndexOfSiteUrl)) {
              _tmpSiteUrl = null;
            } else {
              _tmpSiteUrl = _cursor.getString(_cursorIndexOfSiteUrl);
            }
            final boolean _tmpNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp != 0;
            final int _tmpAlertLeadTimeMinutes;
            _tmpAlertLeadTimeMinutes = _cursor.getInt(_cursorIndexOfAlertLeadTimeMinutes);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new AnimeEntity(_tmpId,_tmpTitle,_tmpCoverImage,_tmpBannerImage,_tmpSynopsis,_tmpStudio,_tmpDurationMinutes,_tmpGenres,_tmpAverageScore,_tmpWatchedEpisodes,_tmpTotalEpisodes,_tmpNextEpisodeNumber,_tmpNextEpisodeAiringAt,_tmpAiringDayOfWeek,_tmpStatus,_tmpSiteUrl,_tmpNotificationsEnabled,_tmpAlertLeadTimeMinutes,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAnimeById(final int id, final Continuation<? super AnimeEntity> $completion) {
    final String _sql = "SELECT * FROM saved_anime WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AnimeEntity>() {
      @Override
      @Nullable
      public AnimeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCoverImage = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImage");
          final int _cursorIndexOfBannerImage = CursorUtil.getColumnIndexOrThrow(_cursor, "bannerImage");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfStudio = CursorUtil.getColumnIndexOrThrow(_cursor, "studio");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfAverageScore = CursorUtil.getColumnIndexOrThrow(_cursor, "averageScore");
          final int _cursorIndexOfWatchedEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "watchedEpisodes");
          final int _cursorIndexOfTotalEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalEpisodes");
          final int _cursorIndexOfNextEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeNumber");
          final int _cursorIndexOfNextEpisodeAiringAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextEpisodeAiringAt");
          final int _cursorIndexOfAiringDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "airingDayOfWeek");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSiteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "siteUrl");
          final int _cursorIndexOfNotificationsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationsEnabled");
          final int _cursorIndexOfAlertLeadTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "alertLeadTimeMinutes");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final AnimeEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCoverImage;
            _tmpCoverImage = _cursor.getString(_cursorIndexOfCoverImage);
            final String _tmpBannerImage;
            if (_cursor.isNull(_cursorIndexOfBannerImage)) {
              _tmpBannerImage = null;
            } else {
              _tmpBannerImage = _cursor.getString(_cursorIndexOfBannerImage);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpStudio;
            if (_cursor.isNull(_cursorIndexOfStudio)) {
              _tmpStudio = null;
            } else {
              _tmpStudio = _cursor.getString(_cursorIndexOfStudio);
            }
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpAverageScore;
            if (_cursor.isNull(_cursorIndexOfAverageScore)) {
              _tmpAverageScore = null;
            } else {
              _tmpAverageScore = _cursor.getInt(_cursorIndexOfAverageScore);
            }
            final int _tmpWatchedEpisodes;
            _tmpWatchedEpisodes = _cursor.getInt(_cursorIndexOfWatchedEpisodes);
            final Integer _tmpTotalEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalEpisodes)) {
              _tmpTotalEpisodes = null;
            } else {
              _tmpTotalEpisodes = _cursor.getInt(_cursorIndexOfTotalEpisodes);
            }
            final Integer _tmpNextEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeNumber)) {
              _tmpNextEpisodeNumber = null;
            } else {
              _tmpNextEpisodeNumber = _cursor.getInt(_cursorIndexOfNextEpisodeNumber);
            }
            final Long _tmpNextEpisodeAiringAt;
            if (_cursor.isNull(_cursorIndexOfNextEpisodeAiringAt)) {
              _tmpNextEpisodeAiringAt = null;
            } else {
              _tmpNextEpisodeAiringAt = _cursor.getLong(_cursorIndexOfNextEpisodeAiringAt);
            }
            final Integer _tmpAiringDayOfWeek;
            if (_cursor.isNull(_cursorIndexOfAiringDayOfWeek)) {
              _tmpAiringDayOfWeek = null;
            } else {
              _tmpAiringDayOfWeek = _cursor.getInt(_cursorIndexOfAiringDayOfWeek);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSiteUrl;
            if (_cursor.isNull(_cursorIndexOfSiteUrl)) {
              _tmpSiteUrl = null;
            } else {
              _tmpSiteUrl = _cursor.getString(_cursorIndexOfSiteUrl);
            }
            final boolean _tmpNotificationsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotificationsEnabled);
            _tmpNotificationsEnabled = _tmp != 0;
            final int _tmpAlertLeadTimeMinutes;
            _tmpAlertLeadTimeMinutes = _cursor.getInt(_cursorIndexOfAlertLeadTimeMinutes);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new AnimeEntity(_tmpId,_tmpTitle,_tmpCoverImage,_tmpBannerImage,_tmpSynopsis,_tmpStudio,_tmpDurationMinutes,_tmpGenres,_tmpAverageScore,_tmpWatchedEpisodes,_tmpTotalEpisodes,_tmpNextEpisodeNumber,_tmpNextEpisodeAiringAt,_tmpAiringDayOfWeek,_tmpStatus,_tmpSiteUrl,_tmpNotificationsEnabled,_tmpAlertLeadTimeMinutes,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
