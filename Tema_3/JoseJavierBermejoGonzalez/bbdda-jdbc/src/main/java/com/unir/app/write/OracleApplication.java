package com.unir.app.write;

import com.unir.config.OracleDatabaseConnector;
import com.unir.model.OracleCountry;
import com.unir.model.OracleJob;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * La version para MySQL seria muy similar a esta, cambiando únicamente el Driver.
 */
@Slf4j
public class OracleApplication {

    private static final String SERIVCE_NAME = "orcl";

    public static void main(String[] args) {

        //Creamos conexion. No es necesario indicar puerto en host si usamos el default, 1521
        //Try-with-resources. Se cierra la conexión automáticamente al salir del bloque try
        try(Connection connection = new OracleDatabaseConnector("localhost", SERIVCE_NAME).getConnection()) {

            log.info("Conexión establecida con la base de datos Oracle");
            OracleCountry spain = new OracleCountry("ES", 1, "Spain");
            upsert(connection, spain);

            log.info("Insertamos registro en puestos (JOB)");
            OracleJob ciso = new OracleJob("SEC_CISO", "Chief Information Security Officer", 11600, 18350);
            upsert(connection, ciso);

        } catch (Exception e) {
            log.error("Error al tratar con la base de datos", e);
        }
    }


    /**
     * Función que nos permite insertar o actualizar un país en la base de datos.
     * Si el país ya existe, se actualiza. Si no existe, se inserta.
     * OJO! No es lo mismo que un MERGE aunque funcionalmente sea parecido.
     * Mas info (1): https://www.sqlservercentral.com/articles/performance-of-the-sql-merge-vs-insertupdate
     * Mas info (2): https://www.mssqltips.com/sqlservertip/7590/sql-merge-performance-vs-insert-update-delete/
     * Mas info (3): https://www.linkedin.com/pulse/sql-load-performance-optimization-part-1-replace-merge-kragh/
     *
     * @param connection - Conexión a la base de datos.
     * @param country - País a insertar o actualizar.
     * @throws SQLException - Si ocurre algún error al ejecutar la consulta.
     */
    public static void upsert(Connection connection, OracleCountry country) throws SQLException {

        String selectSql = "SELECT COUNT(*) FROM countries WHERE country_id = ?";
        String updateSql = "UPDATE countries SET country_name = ?, region_id = ? WHERE country_id = ?";
        String insertSql = "INSERT INTO countries (country_id, country_name, region_id) VALUES (?, ?, ?)";

        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, country.getCountryId()); // Código del país
        ResultSet resultSet = selectStatement.executeQuery();
        resultSet.next(); // Nos movemos a la primera fila
        int rowCount = resultSet.getInt(1);

        if(rowCount > 0) {
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setString(1, country.getCountryName()); // Nuevo nombre del país
            updateStatement.setInt(2, country.getRegionId()); // Nuevo codigo de región
            updateStatement.setString(3, country.getCountryId()); // Código del país
            int filasActualizadas = updateStatement.executeUpdate();
            log.debug("Filas Actualizadas: {}", filasActualizadas);

        } else {
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setString(1, country.getCountryId()); // Nuevo código del país.
            insertStatement.setString(2, country.getCountryName()); // Nuevo nombre del país
            insertStatement.setInt(3, country.getRegionId()); // Código de región
            int filasInsertadas = insertStatement.executeUpdate();
            log.debug("Filas Insertadas: {}", filasInsertadas);
        }
    }

    /**
     * Función que nos permite insertar o actualizar un trabajo en la base de datos.
     * Si el trabajo ya existe, se actualiza. Si no existe, se inserta.
     * @param connection - Conexión a la base de datos.
     * @param job - Trabajo a insertar o actualizar.
     * @throws SQLException - Si ocurre algún error al ejecutar la consulta.
     */
    public static void upsert(Connection connection, OracleJob job) throws SQLException {

        String selectSql = "SELECT COUNT(*) FROM jobs WHERE job_id = ?";
        String updateSql = "UPDATE jobs SET job_title = ?, min_salary = ?, max_salary = ? WHERE job_id = ?";
        String insertSql = "INSERT INTO jobs (job_id, job_title, min_salary, max_salary) VALUES (?, ?, ?, ?)";

        PreparedStatement selectStatement = connection.prepareStatement(selectSql);
        selectStatement.setString(1, job.getJobId()); // Código del job
        ResultSet resultSet = selectStatement.executeQuery();
        resultSet.next(); // Nos movemos a la primera fila
        int rowCount = resultSet.getInt(1);

        if(rowCount > 0) {
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setString(1, job.getJobTitle()); // Título del trabajo
            updateStatement.setInt(2, job.getMinSalary()); // Salario mínimo del trabajo
            updateStatement.setInt(3, job.getMaxSalary()); // Salario máximo del trabajo
            updateStatement.setString(4, job.getJobId()); // Código del trabajo
            int filasActualizadas = updateStatement.executeUpdate();
            log.debug("Filas Actualizadas: {}", filasActualizadas);

        } else {
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setString(1, job.getJobId()); // Nuevo código del trabajo.
            insertStatement.setString(2, job.getJobTitle()); // Nuevo título del trabajo
            insertStatement.setInt(3, job.getMinSalary()); // Nuevo salario mínimo del trabajo
            insertStatement.setInt(4, job.getMaxSalary()); // Nuevo salario máximo del trabajo
            int filasInsertadas = insertStatement.executeUpdate();
            log.debug("Filas Insertadas: {}", filasInsertadas);
        }
    }
}
