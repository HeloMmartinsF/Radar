import json
import http.client
import time

# Ler JSON
with open('subcategorias_completas.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

total_inseridas = 0
erros = 0

print("=== INSERINDO SUBCATEGORIAS ===\n")

for idx, cat_obj in enumerate(data):
    categoria = cat_obj['categoria']
    subcategorias = cat_obj['subcategorias']
    
    print(f"[{idx+1}/24] {categoria}")
    
    for sub in subcategorias:
        body = json.dumps({
            'nome': sub['nome'],
            'descricao': sub['descricao'],
            'categoria': categoria,
            'ativa': True
        })
        
        try:
            conn = http.client.HTTPConnection('localhost', 8080)
            conn.request('POST', '/api/subcategorias', body, {'Content-Type': 'application/json; charset=utf-8'})
            response = conn.getresponse()
            
            if 200 <= response.status < 300:
                print(f"  [OK] {sub['nome']}")
                total_inseridas += 1
            else:
                print(f"  [ERRO {response.status}] {sub['nome']}")
                erros += 1
            
            conn.close()
            time.sleep(0.05)  # Pausa 50ms
            
        except Exception as e:
            print(f"  [ERRO] {sub['nome']} - {str(e)}")
            erros += 1
    
    print()

print("=== RESUMO ===")
print(f"Total inseridas: {total_inseridas}")
print(f"Erros: {erros}")

# Estatísticas finais
try:
    conn = http.client.HTTPConnection('localhost', 8080)
    conn.request('GET', '/api/subcategorias/estatisticas')
    response = conn.getresponse()
    stats = json.loads(response.read().decode())
    print(f"\nTotal no banco: {stats['total']}")
    print(f"Ativas: {stats['ativas']}")
    conn.close()
except:
    pass

input("\nPressione ENTER para sair...")
