import json
import http.client
import time

def inserir_subcategoria(nome, descricao, categoria):
    try:
        conn = http.client.HTTPConnection("localhost", 8080)
        payload = json.dumps({
            "nome": nome,
            "descricao": descricao,
            "categoria": categoria,
            "ativa": True
        })
        headers = {'Content-Type': 'application/json'}
        conn.request("POST", "/api/subcategorias", payload, headers)
        res = conn.getresponse()
        conn.close()
        return res.status == 200 or res.status == 201
    except Exception as e:
        print(f"  ✗ {nome}: {str(e)}")
        return False

# Carregar dados
with open('subcategorias_insert.json', 'r', encoding='utf-8') as f:
    dados = json.load(f)

total_cat = len(dados)
total_sub = 0
sucessos = 0
erros = 0

print("=== Inserindo subcategorias ===\n")

for item in dados:
    categoria = item['categoria']
    subcategorias = item['subcategorias']
    count = len(subcategorias)
    total_sub += count
    
    print(f"[{categoria}] - {count} subcategorias")
    
    for sub in subcategorias:
        if inserir_subcategoria(sub['nome'], sub['descricao'], categoria):
            print(f"  ✓ {sub['nome']}")
            sucessos += 1
        else:
            erros += 1
        time.sleep(0.05)

print(f"\n=== Resumo ===")
print(f"Categorias: {total_cat}")
print(f"Total subcategorias: {total_sub}")
print(f"Sucessos: {sucessos}")
print(f"Erros: {erros}")
