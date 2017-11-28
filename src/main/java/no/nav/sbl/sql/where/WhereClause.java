package no.nav.sbl.sql.where;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class WhereClause {
    public static WhereClause equals(String field, Object value) {
        return new ComparativeWhereClause(WhereOperator.EQUALS, field, value);
    }
    public static WhereClause gt(String field, Object value) {
        return new ComparativeWhereClause(WhereOperator.GT, field, value);
    }
    public static WhereClause gteq(String field, Object value) {
        return new ComparativeWhereClause(WhereOperator.GTEQ, field, value);
    }
    public static WhereClause lt(String field, Object value) {
        return new ComparativeWhereClause(WhereOperator.LT, field, value);
    }
    public static WhereClause lteq(String field, Object value) {
        return new ComparativeWhereClause(WhereOperator.LTEQ, field, value);
    }

    public WhereClause and(WhereClause other) {
        return new LogicalWhereClause(WhereOperator.AND, this, other);
    }

    public WhereClause andIf(WhereClause other, boolean add) {
        return add ? and(other) : this;
    }

    public WhereClause or(WhereClause other) {
        return new LogicalWhereClause(WhereOperator.OR, this, other);
    }

    public abstract int applyTo(PreparedStatement ps, int index) throws SQLException;

    public abstract String toSql();

    public abstract boolean appliesTo(String key);
}
