package org.apache.lucene.store.jdbc.index.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.lucene.store.jdbc.index.RAMJdbcIndexOutput;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * A specialized Oracle version that works (through reflection) with
 * Oracle 9i/8i specific blob API for blobs bigger than 4k.
 *
 * @author kimchy
 */
public class OracleRAMJdbcIndexOutput extends RAMJdbcIndexOutput {

    public void close() throws IOException {
        flush();
        final long length = length();
        doBeforeClose();
        String sqlInsert = OracleIndexOutputHelper.sqlInsert(jdbcDirectory.getTable());
        jdbcDirectory.getJdbcTemplate().executeUpdate(sqlInsert, new JdbcTemplate.PrepateStatementAwareCallback() {
            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                ps.setFetchSize(1);
                ps.setString(1, name);
                ps.setLong(2, length);
                ps.setBoolean(3, false);
            }
        });

        String sqlUpdate = OracleIndexOutputHelper.sqlUpdate(jdbcDirectory.getTable());
        jdbcDirectory.getJdbcTemplate().executeSelect(sqlUpdate,
                new JdbcTemplate.ExecuteSelectCallback() {
                    public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, name);
                    }

                    public Object execute(ResultSet rs) throws Exception {
                        OutputStream os = null;
                        try {
                            rs.next();
                            os = OracleIndexOutputHelper.getBlobOutputStream(rs);
                            InputStream is = openInputStream();
                            byte[] buffer = new byte[1000];
                            int bytesRead = 0;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            return null;
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                });
        doAfterClose();
    }
}
