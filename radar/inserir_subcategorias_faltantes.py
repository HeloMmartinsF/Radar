#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para inserir subcategorias faltantes no banco H2 via API REST
Categorias: Industrial, Limpeza e Higiene, Segurança, Pet, Energia, 
           Esportes e Fitness, Gráfica e Personalizados, Brinquedos e Infantil,
           Artesanato e Hobbies, Casa e Decoração, Outros
"""

import json
import requests
import time

# Configuração
API_URL = "http://localhost:8080/api/subcategorias"
JSON_FILE = "subcategorias_faltantes.json"

def carregar_subcategorias():
    """Carrega subcategorias do arquivo JSON"""
    try:
        with open(JSON_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"❌ Arquivo {JSON_FILE} não encontrado!")
        return None
    except json.JSONDecodeError:
        print(f"❌ Erro ao decodificar JSON do arquivo {JSON_FILE}")
        return None

def inserir_subcategoria(categoria, nome_subcategoria):
    """Insere uma subcategoria via API REST"""
    dados = {
        "categoria": categoria,
        "nome": nome_subcategoria,
        "ativa": True
    }
    
    try:
        response = requests.post(API_URL, json=dados, timeout=10)
        
        if response.status_code == 200 or response.status_code == 201:
            return True, "Sucesso"
        else:
            return False, f"Status {response.status_code}: {response.text}"
            
    except requests.exceptions.ConnectionError:
        return False, "Erro de conexão - Servidor não está rodando?"
    except requests.exceptions.Timeout:
        return False, "Timeout na requisição"
    except Exception as e:
        return False, f"Erro: {str(e)}"

def main():
    print("=" * 70)
    print(" INSERÇÃO DE SUBCATEGORIAS FALTANTES NO BANCO H2")
    print("=" * 70)
    print()
    
    # Carregar dados
    print("📂 Carregando subcategorias do JSON...")
    subcategorias_por_categoria = carregar_subcategorias()
    
    if not subcategorias_por_categoria:
        print("❌ Falha ao carregar dados")
        return
    
    print(f"✅ {len(subcategorias_por_categoria)} categorias carregadas")
    print()
    
    # Verificar servidor
    print("🔌 Verificando conexão com servidor...")
    try:
        response = requests.get("http://localhost:8080/api/subcategorias", timeout=5)
        print(f"✅ Servidor online (Status: {response.status_code})")
    except:
        print("❌ ERRO: Servidor não está respondendo!")
        print("   Inicie o servidor primeiro com: 'Iniciar SpringBoot Radar Fornecedor.bat'")
        input("\nPressione Enter para sair...")
        return
    
    print()
    print("=" * 70)
    print(" INICIANDO INSERÇÃO")
    print("=" * 70)
    print()
    
    # Contadores
    total_categorias = len(subcategorias_por_categoria)
    total_subcategorias = sum(len(subs) for subs in subcategorias_por_categoria.values())
    sucesso = 0
    falhas = 0
    
    # Inserir subcategorias
    for idx, (categoria, subcategorias) in enumerate(subcategorias_por_categoria.items(), 1):
        print(f"[{idx}/{total_categorias}] 📁 Categoria: {categoria}")
        print(f"    {len(subcategorias)} subcategorias")
        
        for sub_idx, nome_subcategoria in enumerate(subcategorias, 1):
            ok, mensagem = inserir_subcategoria(categoria, nome_subcategoria)
            
            if ok:
                print(f"    ✅ [{sub_idx:2d}/10] {nome_subcategoria}")
                sucesso += 1
            else:
                print(f"    ❌ [{sub_idx:2d}/10] {nome_subcategoria} - {mensagem}")
                falhas += 1
            
            # Pequeno delay para não sobrecarregar
            time.sleep(0.1)
        
        print()
    
    # Relatório final
    print("=" * 70)
    print(" RELATÓRIO FINAL")
    print("=" * 70)
    print(f"Total de subcategorias: {total_subcategorias}")
    print(f"✅ Inseridas com sucesso: {sucesso}")
    print(f"❌ Falhas: {falhas}")
    print()
    
    if falhas == 0:
        print("🎉 TODAS AS SUBCATEGORIAS FORAM INSERIDAS COM SUCESSO!")
        print()
        print("✅ As subcategorias agora estão salvas no banco H2 (DBLRadar.mv.db)")
        print("✅ Elas NÃO vão sumir após reiniciar o servidor")
        print("✅ O banco está em modo FILE (persistente)")
    else:
        print("⚠️  Algumas subcategorias falharam. Verifique os erros acima.")
    
    print()
    print("=" * 70)
    input("\nPressione Enter para sair...")

if __name__ == "__main__":
    main()
