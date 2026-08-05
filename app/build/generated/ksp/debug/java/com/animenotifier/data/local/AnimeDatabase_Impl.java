package com.animenotifier.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AnimeDatabase_Impl extends AnimeDatabase {
  private volatile AnimeDao _animeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `saved_anime` (`id` INTEGER NOT NULL, `title` TEXT NOT NULL, `coverImage` TEXT NOT NULL, `watchedEpisodes` INTEGER NOT NULL, `totalEpisodes` INTEGER, `nextEpisodeNumber` INTEGER, `nextEpisodeAiringAt` INTEGER, `airingDayOfWeek` INTEGER, `status` TEXT, `siteUrl` TEXT, `notificationsEnabled` INTEGER NOT NULL, `alertLeadTimeMinutes` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6ec27735256b0d9f2a91e2f187fa4112')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `saved_anime`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSavedAnime = new HashMap<String, TableInfo.Column>(13);
        _columnsSavedAnime.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("coverImage", new TableInfo.Column("coverImage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("watchedEpisodes", new TableInfo.Column("watchedEpisodes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("totalEpisodes", new TableInfo.Column("totalEpisodes", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("nextEpisodeNumber", new TableInfo.Column("nextEpisodeNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("nextEpisodeAiringAt", new TableInfo.Column("nextEpisodeAiringAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("airingDayOfWeek", new TableInfo.Column("airingDayOfWeek", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("siteUrl", new TableInfo.Column("siteUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("notificationsEnabled", new TableInfo.Column("notificationsEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("alertLeadTimeMinutes", new TableInfo.Column("alertLeadTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAnime.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavedAnime = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavedAnime = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavedAnime = new TableInfo("saved_anime", _columnsSavedAnime, _foreignKeysSavedAnime, _indicesSavedAnime);
        final TableInfo _existingSavedAnime = TableInfo.read(db, "saved_anime");
        if (!_infoSavedAnime.equals(_existingSavedAnime)) {
          return new RoomOpenHelper.ValidationResult(false, "saved_anime(com.animenotifier.data.local.AnimeEntity).\n"
                  + " Expected:\n" + _infoSavedAnime + "\n"
                  + " Found:\n" + _existingSavedAnime);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6ec27735256b0d9f2a91e2f187fa4112", "fe49b95fa9b27d3cfffeb6c892b62d5f");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "saved_anime");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `saved_anime`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AnimeDao.class, AnimeDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AnimeDao animeDao() {
    if (_animeDao != null) {
      return _animeDao;
    } else {
      synchronized(this) {
        if(_animeDao == null) {
          _animeDao = new AnimeDao_Impl(this);
        }
        return _animeDao;
      }
    }
  }
}
