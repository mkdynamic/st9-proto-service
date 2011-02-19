package com.g414.st9.proto.service.store;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Response;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;

/**
 * MySQL implementation of key-value storage using JDBI.
 */
public class SqliteKeyValueStorage extends JDBIKeyValueStorage {
    private static final Logger log = LoggerFactory
            .getLogger(SqliteKeyValueStorage.class);

    protected String getPrefix() {
        return "sqlite:sqlite_";
    }

    public static class SqliteKeyValueStorageModule extends AbstractModule {
        @Override
        public void configure() {
            Binder binder = binder();

            BoneCPDataSource datasource = new BoneCPDataSource();
            datasource.setDriverClass(JDBC.class.getName());
            datasource.setJdbcUrl("jdbc:sqlite:thedb.db");
            datasource.setUsername("root");
            datasource.setPassword("notreallyused");

            datasource.setConnectionHook(new AbstractConnectionHook() {
                @Override
                public void onCheckOut(ConnectionHandle arg0) {
                    PreparedStatement stmt = null;
                    try {
                        stmt = arg0
                                .prepareStatement("pragma synchronous = off");
                        stmt.execute();
                    } catch (SQLException e) {
                        log.warn(
                                "Error while setting pragma " + e.getMessage(),
                                e);

                        throw new RuntimeException(e);
                    } finally {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                log.warn(
                                        "Error while setting pragma "
                                                + e.getMessage(), e);

                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });

            DBI dbi = JDBIHelper.getDBI(datasource);
            binder.bind(IDBI.class).toInstance(dbi);

            binder.bind(KeyValueStorage.class).to(SqliteKeyValueStorage.class)
                    .asEagerSingleton();
        }
    }

    @Override
    public synchronized Response create(String type, String inValue)
            throws Exception {
        return super.create(type, inValue);
    }

    @Override
    public synchronized Response retrieve(String key) throws Exception {
        return super.retrieve(key);
    }

    @Override
    public synchronized Response multiRetrieve(List<String> keys)
            throws Exception {
        return super.multiRetrieve(keys);
    }

    @Override
    public synchronized Response update(String key, String inValue)
            throws Exception {
        return super.update(key, inValue);
    }

    @Override
    public synchronized Response delete(String key) throws Exception {
        return super.delete(key);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }
}