package br.com.senai.projeto.config;

import java.io.File;
import java.sql.*;

public class Conexao {
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static String getDbPath() {
        // Prioridade 1: Variável de ambiente (usada no Docker: DB_PATH=/app/data/banco)
        String envPath = System.getenv("DB_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            return envPath;
        }

        // Prioridade 2: Propriedade Java de sistema (-Ddb.path=...)
        String sysProp = System.getProperty("db.path");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp;
        }

        // Prioridade 3: Ambiente Linux / Docker padrão
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux") || new File("/app/data").exists()) {
            return "/app/data/banco";
        }

        // Prioridade 4: Fallback para execução local na máquina
        return System.getProperty("user.dir") + File.separator + "src" + File.separator + "data" + File.separator + "banco";
    }

    private static String getUrl() {
        String dbPath = getDbPath();
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        return "jdbc:h2:file:" + dbPath;
    }

    public static Connection conectar() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver H2 não encontrado!", e);
        }
        return DriverManager.getConnection(
                getUrl(), USUARIO, SENHA
        );
    }

    public static void createTable() {
        String sql = "    CREATE TABLE IF NOT EXISTS entidades (\n" +
                "        id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                "        nome VARCHAR(150) NOT NULL,\n" +
                "        categoria VARCHAR(150) NOT NULL,\n" +
                "        quantidade INT NOT NULL,\n" +
                "        preco_unitario DECIMAL(10, 2) NOT NULL\n" +
                "    )\n";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()){
            stmt.execute(sql);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}