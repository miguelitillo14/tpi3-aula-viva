package es.uloyola.aulaviva;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Main {
    private static final String ALL_SPACES_SQL = """
            SELECT space_id, building, space_name, capacity,
                   occupancy_ratio, noise_db, reported_at, available_places
            FROM app.vw_student_space_status
            ORDER BY building, space_name
            """;
    private static final String ONE_SPACE_SQL = """
            SELECT space_id, building, space_name, capacity,
                   occupancy_ratio, noise_db, reported_at, available_places
            FROM app.vw_student_space_status
            WHERE space_id = ?
            ORDER BY building, space_name
            """;

    private Main() {}

    public static void main(String[] args) {
        String url = env("DB_URL", "jdbc:postgresql://db:5432/aula_viva");
        String user = env("DB_USER", "aula_reader");
        String password = env("DB_PASSWORD", "aula_reader_local");
        String requestedSpace = args.length > 0 ? args[0] : null;
        String sql = requestedSpace == null ? ALL_SPACES_SQL : ONE_SPACE_SQL;

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setReadOnly(true);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (requestedSpace != null) {
                    statement.setString(1, requestedSpace);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    printHeader();
                    int count = 0;
                    while (rows.next()) {
                        count++;
                        BigDecimal occupancy = rows.getBigDecimal("occupancy_ratio");
                        BigDecimal noise = rows.getBigDecimal("noise_db");
                        System.out.printf(
                                "%-6s | %-2s | %-20s | %2d | %5s | %5s | %s | %2d%n",
                                rows.getString("space_id"), rows.getString("building"),
                                rows.getString("space_name"), rows.getInt("capacity"),
                                occupancy.stripTrailingZeros().toPlainString(),
                                noise.stripTrailingZeros().toPlainString(),
                                rows.getTimestamp("reported_at"),
                                rows.getInt("available_places"));
                    }
                    if (count == 0) {
                        System.out.println("No hay espacios con un informe disponible"
                                + (requestedSpace == null ? "." : " para " + requestedSpace + "."));
                    } else {
                        System.out.printf("%nFilas: %d%n", count);
                    }
                }
            }
        } catch (SQLException exception) {
            System.err.println("No se pudo consultar Aula Viva: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printHeader() {
        System.out.println("Espacio | Ed. | Nombre               | Cap | Ocup. | Ruido | Fecha               | Libres");
        System.out.println("--------+-----+----------------------+-----+-------+-------+---------------------+-------");
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
