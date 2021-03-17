package no.nav.common.kafka.producer.feilhandtering;

import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static java.lang.String.format;
import static no.nav.common.kafka.util.DatabaseConstants.*;
import static no.nav.common.kafka.util.DatabaseUtils.*;

public class OracleProducerRepository implements KafkaProducerRepository {

    private final DataSource dataSource;

    public OracleProducerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public long storeRecord(KafkaProducerRecord record) {
        String sql = format(
                "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                PRODUCER_RECORD_TABLE, ID, TOPIC, KEY, VALUE
        );

        long id = incrementAndGetOracleSequence(dataSource, PRODUCER_RECORD_ID_SEQ);

        try(PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setLong(1, id);
            statement.setString(2, record.getTopic());
            statement.setBytes(3, record.getKey());
            statement.setBytes(4, record.getValue());
            statement.executeUpdate();
        }

        return id;
    }

    @SneakyThrows
    @Override
    public void deleteRecord(long id) {
        String sql = format("DELETE FROM %s WHERE %s = ?", PRODUCER_RECORD_TABLE, ID);
        try(PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @SneakyThrows
    @Override
    public List<KafkaProducerRecord> getRecords(Instant olderThan, int maxMessages) {
        String sql = format(
                "SELECT * FROM %s WHERE %s >= ? ORDER BY %s FETCH NEXT %d ROWS ONLY",
                PRODUCER_RECORD_TABLE, CREATED_AT, ID, maxMessages
        );

        try(PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setTimestamp(1, new Timestamp(olderThan.toEpochMilli()));
            return fetchProducerRecords(statement.executeQuery());
        }
    }

}
