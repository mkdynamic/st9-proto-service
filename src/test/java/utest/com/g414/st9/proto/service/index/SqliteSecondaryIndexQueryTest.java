package utest.com.g414.st9.proto.service.index;

import org.testng.annotations.Test;

import com.g414.st9.proto.service.store.SqliteKeyValueStorage.SqliteKeyValueStorageModule;
import com.google.inject.Module;

@Test
public class SqliteSecondaryIndexQueryTest extends SecondaryIndexQueryTestBase {
    public Module getKeyValueStorageModule() {
        return new SqliteKeyValueStorageModule();
    }
}