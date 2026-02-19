Schema helper (migrations)
==========================

Use the `Schema` helper in Java-based migrations to create tables in a
provider-agnostic way (currently generates MySQL-compatible DDL).

Example migration:

```
public class CreatePlayersTable implements Migration {
  public String id() { return "2026_02_19_create_players"; }
  public String description() { return "Create players table"; }
  public MigrationType type() { return MigrationType.JAVA; }
  public void apply(MigrationContext ctx) throws Exception {
    if (!(ctx.provider() instanceof MigrationCapable)) throw new IllegalStateException();
    String sql = Schema.table("players")
       .id()
       .string("name", 191, true)
       .integer("coins", true)
       .toCreateSql();
    ((MigrationCapable) ctx.provider()).executeSql(sql);

    // register model mapping so repositories save into this table
    java.util.Map<String,String> cols = new java.util.HashMap<>();
    cols.put("id", "VARCHAR(255)");
    cols.put("name", "VARCHAR(191)");
    cols.put("coins", "INT");
    com.skyblockexp.ezframework.storage.model.ModelTableRegistry.register("players", "players", cols);
  }
}
```

Notes
-----

- `Schema` is a small helper that currently targets MySQL DDL. It returns a
  CREATE TABLE statement which migrations can execute via the provider's
  `executeSql` or `executeSqlStatements` methods.
- After creating tables, register the table/column mapping with
  `ModelTableRegistry.register(prefix, tableName, columns)` so that
  `ModelRepository` will persist models into columns when the provider
  implements `JdbcStorage` (e.g., the MySQL provider).
