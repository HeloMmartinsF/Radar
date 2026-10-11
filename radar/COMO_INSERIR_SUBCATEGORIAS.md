# Como Inserir Subcategorias Restantes

## ✅ Status Atual
- **13 subcategorias** já inseridas e **PERSISTENTES** no banco H2
- **3** AUTOMOTIVO (teste)
- **10** MOTOCICLETAS

## 🎯 Faltam 21 categorias (~210 subcategorias)

---

## 📊 Verificar Quantas Existem

Acesse no navegador:
```
http://localhost:8080/api/subcategorias/estatisticas
```

Ou no PowerShell:
```powershell
Invoke-RestMethod http://localhost:8080/api/subcategorias/estatisticas
```

---

## 🚀 Método 1: Inserir Via Navegador (Mais Simples)

1. Acesse: `http://localhost:8080/api/subcategorias`
2. Use uma ferramenta como **Postman** ou **Insomnia**
3. Envie POST com body JSON:
```json
{
  "nome": "Nome da Subcategoria",
  "descricao": "Descrição",
  "categoria": "TECNOLOGIA",
  "ativa": true
}
```

---

## 🚀 Método 2: PowerShell (Linha por Linha)

Copie e cole no PowerShell (uma categoria por vez):

### TECNOLOGIA (10 subcategorias)
```powershell
$base = 'http://localhost:8080/api/subcategorias'
$cat = 'TECNOLOGIA'
$subs = @(
    'Computadores e Notebooks',
    'Perifericos',
    'Componentes Hardware',
    'Armazenamento',
    'Redes e Conectividade',
    'Servidores',
    'Smartphones e Tablets',
    'Impressoras e Scanners',
    'Monitores',
    'Software e Licencas'
)
foreach($s in $subs){
    Invoke-RestMethod -Uri $base -Method Post -Body "{`"nome`":`"$s`",`"descricao`":`"$cat - $s`",`"categoria`":`"$cat`",`"ativa`":true}" -ContentType 'application/json' | Out-Null
    Write-Host "OK: $s"
}
Write-Host "$cat CONCLUÍDO!" -ForegroundColor Green
```

### CONSTRUCAO (10 subcategorias)
```powershell
$cat = 'CONSTRUCAO'
$subs = @(
    'Cimento e Argamassas',
    'Tijolos e Blocos',
    'Areia e Pedras',
    'Telhas e Coberturas',
    'Tintas e Vernizes',
    'Madeiras',
    'Ferragens',
    'Pisos e Revestimentos',
    'Hidraulica',
    'Estruturas Metalicas'
)
foreach($s in $subs){
    Invoke-RestMethod -Uri $base -Method Post -Body "{`"nome`":`"$s`",`"descricao`":`"$cat - $s`",`"categoria`":`"$cat`",`"ativa`":true}" -ContentType 'application/json' | Out-Null
    Write-Host "OK: $s"
}
Write-Host "$cat CONCLUÍDO!" -ForegroundColor Green
```

### Continue para as demais categorias...

**Categorias restantes:**
- MODA_TEXTIL
- EMBALAGENS
- MOVEIS
- ELETRICA
- FERRAMENTAS
- BELEZA
- SAUDE
- ALIMENTOS
- AGRONEGOCIO
- LIMPEZA
- PET
- ESPORTES
- BRINQUEDOS
- CASA
- INDUSTRIAL
- SEGURANCA
- ENERGIA
- GRAFICA
- ARTESANATO
- OUTROS

---

## ✅ Como Confirmar que Salvou

Após inserir, REINICIE o servidor e verifique:
```
http://localhost:8080/api/subcategorias/estatisticas
```

Se o total aumentou após reiniciar, **está persistindo corretamente!**

---

## 🎯 O Importante

**Você JÁ TEM:**
- ✅ Servidor rodando
- ✅ Banco H2 persistente funcionando
- ✅ Tabela subcategoria criada
- ✅ 13 subcategorias salvas permanentemente
- ✅ API REST funcionando

Pode inserir o restante aos poucos, sem pressa. **Os dados NÃO serão perdidos!**

---

## 📖 Acessar o Sistema

```
http://localhost:8080
```

A página `produtos.html` e `produtos-subcategorias.html` já estão prontas para mostrar as subcategorias!
