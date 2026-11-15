package com.badger.app.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.badger.app.model.NagItem;
import java.lang.Class;
import java.lang.Exception;
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
public final class NagDao_Impl implements NagDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NagItem> __insertionAdapterOfNagItem;

  private final EntityDeletionOrUpdateAdapter<NagItem> __deletionAdapterOfNagItem;

  private final EntityDeletionOrUpdateAdapter<NagItem> __updateAdapterOfNagItem;

  public NagDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNagItem = new EntityInsertionAdapter<NagItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `nags` (`id`,`title`,`isCompleted`,`dueDate`,`priorityRaw`,`burrowed`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NagItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDueDate());
        }
        statement.bindLong(5, entity.getPriorityRaw());
        final int _tmp_1 = entity.getBurrowed() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
      }
    };
    this.__deletionAdapterOfNagItem = new EntityDeletionOrUpdateAdapter<NagItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `nags` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NagItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfNagItem = new EntityDeletionOrUpdateAdapter<NagItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `nags` SET `id` = ?,`title` = ?,`isCompleted` = ?,`dueDate` = ?,`priorityRaw` = ?,`burrowed` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NagItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDueDate());
        }
        statement.bindLong(5, entity.getPriorityRaw());
        final int _tmp_1 = entity.getBurrowed() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insertNag(final NagItem nag, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNagItem.insert(nag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNag(final NagItem nag, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfNagItem.handle(nag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNag(final NagItem nag, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfNagItem.handle(nag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<NagItem>> getOpenNags() {
    final String _sql = "SELECT * FROM nags WHERE isCompleted = 0 AND burrowed = 0 ORDER BY priorityRaw DESC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nags"}, new Callable<List<NagItem>>() {
      @Override
      @NonNull
      public List<NagItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfPriorityRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "priorityRaw");
          final int _cursorIndexOfBurrowed = CursorUtil.getColumnIndexOrThrow(_cursor, "burrowed");
          final List<NagItem> _result = new ArrayList<NagItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NagItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final int _tmpPriorityRaw;
            _tmpPriorityRaw = _cursor.getInt(_cursorIndexOfPriorityRaw);
            final boolean _tmpBurrowed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBurrowed);
            _tmpBurrowed = _tmp_1 != 0;
            _item = new NagItem(_tmpId,_tmpTitle,_tmpIsCompleted,_tmpDueDate,_tmpPriorityRaw,_tmpBurrowed);
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
  public Flow<List<NagItem>> getCompletedNags() {
    final String _sql = "SELECT * FROM nags WHERE isCompleted = 1 ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nags"}, new Callable<List<NagItem>>() {
      @Override
      @NonNull
      public List<NagItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfPriorityRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "priorityRaw");
          final int _cursorIndexOfBurrowed = CursorUtil.getColumnIndexOrThrow(_cursor, "burrowed");
          final List<NagItem> _result = new ArrayList<NagItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NagItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final int _tmpPriorityRaw;
            _tmpPriorityRaw = _cursor.getInt(_cursorIndexOfPriorityRaw);
            final boolean _tmpBurrowed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBurrowed);
            _tmpBurrowed = _tmp_1 != 0;
            _item = new NagItem(_tmpId,_tmpTitle,_tmpIsCompleted,_tmpDueDate,_tmpPriorityRaw,_tmpBurrowed);
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
  public Flow<List<NagItem>> getBurrowedNags() {
    final String _sql = "SELECT * FROM nags WHERE burrowed = 1 AND isCompleted = 0 ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"nags"}, new Callable<List<NagItem>>() {
      @Override
      @NonNull
      public List<NagItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfPriorityRaw = CursorUtil.getColumnIndexOrThrow(_cursor, "priorityRaw");
          final int _cursorIndexOfBurrowed = CursorUtil.getColumnIndexOrThrow(_cursor, "burrowed");
          final List<NagItem> _result = new ArrayList<NagItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NagItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final int _tmpPriorityRaw;
            _tmpPriorityRaw = _cursor.getInt(_cursorIndexOfPriorityRaw);
            final boolean _tmpBurrowed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBurrowed);
            _tmpBurrowed = _tmp_1 != 0;
            _item = new NagItem(_tmpId,_tmpTitle,_tmpIsCompleted,_tmpDueDate,_tmpPriorityRaw,_tmpBurrowed);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
