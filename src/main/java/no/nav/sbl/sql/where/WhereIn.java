package no.nav.sbl.sql.where;


import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.stream.Collectors;

public class WhereIn extends WhereClause {
    private String field;
    private Collection<? extends Object> objects;

    public WhereIn(String field, Collection<? extends Object> objects) {
        this.field = field;
        this.objects = objects;
    }

    public static WhereIn of(String field, Collection<? extends Object> objects) {
        return new WhereIn(field, objects);
    }

    @Override
    @SneakyThrows
    public int applyTo(PreparedStatement ps, int index) {
        for(Object object : objects) {
            ps.setObject(index++, object);
        }
        return index;
    }

    @Override
    public String toSql() {
        String parameters = objects.stream().map(dummy-> "?").collect(Collectors.joining(","));

        return String.format("%s %s (%s)",field, WhereOperator.IN.sql, parameters);
    }

    @Override
    public boolean appliesTo(String key) {
        return key.equals(field);
    }
}
