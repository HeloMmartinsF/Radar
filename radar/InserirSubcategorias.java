import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class InserirSubcategorias {
    public static void main(String[] args) throws Exception {
        String json = Files.readString(Paths.get("subcategorias_completas.json"), StandardCharsets.UTF_8);
        
        // Parse manual simples (sem bibliotecas externas)
        String[] categorias = json.split("\\{\\s*\"categoria\"");
        
        int total = 0;
        int erros = 0;
        
        System.out.println("=== INSERINDO SUBCATEGORIAS ===\n");
        
        for (int i = 1; i < categorias.length; i++) {
            String catBlock = categorias[i];
            
            // Extrair categoria
            String categoria = extract(catBlock, "\"categoria\"\\s*:\\s*\"", "\"");
            System.out.println("[" + i + "/24] " + categoria);
            
            // Extrair subcategorias
            String[] subs = catBlock.split("\\{\\s*\"nome\"");
            
            for (int j = 1; j < subs.length; j++) {
                String subBlock = subs[j];
                String nome = extract(subBlock, "\"nome\"\\s*:\\s*\"", "\"");
                String descricao = extract(subBlock, "\"descricao\"\\s*:\\s*\"", "\"");
                
                // Montar JSON
                String body = String.format(
                    "{\"nome\":\"%s\",\"descricao\":\"%s\",\"categoria\":\"%s\",\"ativa\":true}",
                    nome, descricao, categoria
                );
                
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/api/subcategorias").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                    
                    int code = conn.getResponseCode();
                    if (code >= 200 && code < 300) {
                        System.out.println("  [OK] " + nome);
                        total++;
                    } else {
                        System.out.println("  [ERRO " + code + "] " + nome);
                        erros++;
                    }
                    
                    conn.disconnect();
                    Thread.sleep(50); // Pausa 50ms
                    
                } catch (Exception e) {
                    System.out.println("  [ERRO] " + nome + " - " + e.getMessage());
                    erros++;
                }
            }
            System.out.println();
        }
        
        System.out.println("=== RESUMO ===");
        System.out.println("Total inseridas: " + total);
        System.out.println("Erros: " + erros);
        
        // Estatísticas
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/api/subcategorias/estatisticas").openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            System.out.println("\nEstatísticas finais: " + br.readLine());
            br.close();
        } catch (Exception e) {}
    }
    
    private static String extract(String text, String startRegex, String end) {
        try {
            int start = text.indexOf(":") + 1;
            start = text.indexOf("\"", start) + 1;
            int endIdx = text.indexOf("\"", start);
            return text.substring(start, endIdx);
        } catch (Exception e) {
            return "";
        }
    }
}
