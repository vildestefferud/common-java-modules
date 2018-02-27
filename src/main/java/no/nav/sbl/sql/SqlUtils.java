package no.nav.sbl.sql;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.function.Function;

public class SqlUtils {
    static Function<String, String> append(final String suffix) {
        return (String value) -> value + suffix;
    }

    public static UpdateQuery update(JdbcTemplate db, String tableName) {
        return new UpdateQuery(db, tableName);
    }

    public static <S> UpdateBatchQuery<S> updateBatch(JdbcTemplate db, String tableName, Class<S> cls) {
        return new UpdateBatchQuery<>(db, tableName);
    }

    public static InsertQuery insert(JdbcTemplate db, String tableName) {
        return new InsertQuery(db, tableName);
    }

    public static UpsertQuery upsert(JdbcTemplate db, String tableName) {
        return new UpsertQuery(db, tableName);
    }

    public static <T> SelectQuery<T> select(DataSource ds, String tableName, SQLFunction<ResultSet, T> mapper) {
        return new SelectQuery<>(ds, tableName, mapper);
    }

    public static SelectQuery<Long> nextFromSeq(DataSource ds, String sekvens) {
        return select(ds, "dual", resultSet -> resultSet.getLong(1))
                .column(String.format("%s.NEXTVAL", sekvens));
    }

    public static DeleteQuery delete(DataSource ds, String tableName) {
        return new DeleteQuery(ds, tableName);
    }

}
