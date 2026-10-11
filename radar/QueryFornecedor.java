import java.sql.*;

public class QueryFornecedor {
    public static void main(String[] args) {
        String url = "jdbc:h2:file:./DBLRadar;MODE=MySQL;AUTO_SERVER=TRUE";
        String user = "sa";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida com sucesso!");
            
            // Buscar fornecedores
            String query = "SELECT ID, USERNAME, NOME, EMAIL, TIPO, FORNECEDOR, CNPJ_OU_CPF FROM USUARIOS WHERE FORNECEDOR = TRUE";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                System.out.println("\n=== FORNECEDORES CADASTRADOS ===\n");
                
                boolean encontrou = false;
                while (rs.next()) {
                    encontrou = true;
                    System.out.println("ID: " + rs.getLong("ID"));
                    System.out.println("Username: " + rs.getString("USERNAME"));
                    System.out.println("Nome: " + rs.getString("NOME"));
                    System.out.println("Email: " + rs.getString("EMAIL"));
                    System.out.println("Tipo: " + rs.getString("TIPO"));
                    System.out.println("CNPJ/CPF: " + rs.getString("CNPJ_OU_CPF"));
                    System.out.println("Senha: [criptografada com BCrypt - veja abaixo]\n");
                    System.out.println("---");
                }
                
                if (!encontrou) {
                    System.out.println("Nenhum fornecedor encontrado no banco de dados.");
                    System.out.println("\nPara criar um fornecedor de teste, acesse:");
                    System.out.println("http://localhost:8080/pages/cadastro.html");
                    System.out.println("\nOu entre como ADMIN e aprove uma solicitação de cadastro.");
                }
                
            }
            
            // Buscar também clientes para referência
            System.out.println("\n=== TODOS OS USUÁRIOS (para referência) ===\n");
            query = "SELECT ID, USERNAME, NOME, TIPO, FORNECEDOR FROM USUARIOS LIMIT 10";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    System.out.println("ID: " + rs.getLong("ID") + 
                                     " | User: " + rs.getString("USERNAME") + 
                                     " | Nome: " + rs.getString("NOME") + 
                                     " | Tipo: " + rs.getString("TIPO") +
                                     " | Fornecedor: " + rs.getBoolean("FORNECEDOR"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados:");
            e.printStackTrace();
        }
    }
}
