# 🔴 PROBLEMA: Java 25 Incompatível com o Projeto

**Data:** 2026-10-06  
**Erro:** `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag`

---

## 🔍 DIAGNÓSTICO

### Versões Detectadas:
- **Java Atual:** OpenJDK 25.0.2 (2026-01-20 LTS)
- **Java Necessário:** Java 11 ou 17 LTS
- **Spring Boot:** 2.7.14 (suporta até Java 19 no máximo)
- **Maven Compiler Plugin:** Atualizado para 3.11.0

### Por que Java 25 não funciona?
O Spring Boot 2.7.x **não foi desenvolvido** para Java 25. As APIs internas do compilador Java mudaram drasticamente entre versões, causando `TypeTag::UNKNOWN` error.

---

## ✅ SOLUÇÃO RECOMENDADA: Instalar Java 17 LTS

Java 17 é a versão LTS (Long-Term Support) ideal para este projeto.

### Passo 1: Baixar Java 17
Acesse: https://adoptium.net/temurin/releases/?version=17

**Escolha:**
- **Versão:** 17 (LTS)
- **Sistema:** Windows x64
- **Tipo:** JDK (não JRE)

### Passo 2: Instalar
1. Execute o instalador `.msi` baixado
2. Durante instalação, marque:
   - ✅ Set JAVA_HOME variable
   - ✅ Add to PATH
   - ✅ JavaSoft registry keys

### Passo 3: Verificar Instalação
Abra um **novo terminal** (importante: novo, não o antigo) e execute:
```cmd
java -version
```

**Resultado esperado:**
```
openjdk version "17.0.X" 2023-XX-XX LTS
OpenJDK Runtime Environment Temurin-17.0.X+XX (build 17.0.X+XX-LTS)
OpenJDK 64-Bit Server VM Temurin-17.0.X+XX (build 17.0.X+XX-LTS, mixed mode, sharing)
```

### Passo 4: Compilar o Projeto
Execute:
```
c:\Projeto\Projeto_Radar_Fornecedor\radar\COMPILAR_COM_JAVA_CORRETO.bat
```

---

## 🔧 SOLUÇÃO ALTERNATIVA: Múltiplas Versões Java

Se você precisa manter Java 25 para outros projetos:

### Opção A: Usar JAVA_HOME temporário

**1. Localize sua instalação Java 17:**
```
C:\Program Files\Eclipse Adoptium\jdk-17.X.X-hotspot
```

**2. No terminal, defina JAVA_HOME temporariamente:**
```cmd
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.X-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
```

**3. Compile:**
```cmd
cd c:\Projeto\Projeto_Radar_Fornecedor\radar
mvnw.cmd clean compile
```

### Opção B: Criar .bat com Java específico

Crie `COMPILAR_JAVA17.bat`:
```batch
@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.X-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"
call mvnw.cmd clean compile
pause
```

---

## 📝 ALTERAÇÕES JÁ APLICADAS NO POM.XML

Atualizei o `pom.xml` para Java 17:

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <release>17</release>
        ...
    </configuration>
</plugin>
```

Essas mudanças **não afetam** a funcionalidade — apenas tornam o projeto compatível com Java 17.

---

## 🚫 POR QUE NÃO ATUALIZAR PARA SPRING BOOT 3.x?

Spring Boot 3.x **exigiria refatoração massiva:**
- Migração `javax.*` → `jakarta.*` (todas as classes JPA, Validation, Mail)
- Hibernate 5 → Hibernate 6 (mudanças de API)
- Reescrever SQLite Dialect customizado
- Risco de perda de dados durante migração

**Veredito:** Não vale o risco. Java 17 com Spring Boot 2.7.x é estável e produtivo.

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ **Instalar Java 17** (link acima)
2. ✅ **Fechar todos os terminais** antigos
3. ✅ **Verificar:** `java -version` deve mostrar "17.0.X"
4. ✅ **Compilar:** executar `COMPILAR_COM_JAVA_CORRETO.bat`
5. ✅ **Iniciar servidor:** `Iniciar SpringBoot Radar Fornecedor.bat`
6. ✅ **Testar:** `http://localhost:8080/pages/busca.html`

---

## ❓ PERGUNTAS FREQUENTES

**Q: Vou perder meus dados no banco?**  
A: Não. Trocar versão do Java não afeta o banco de dados `DBLRadar.mv.db`.

**Q: Preciso reinstalar o Maven?**  
A: Não. O projeto usa Maven Wrapper (`mvnw.cmd`) que já está incluído.

**Q: Posso usar Java 11 em vez de 17?**  
A: Sim, mas Java 17 tem melhorias de performance e segurança (recomendado).

**Q: Java 21 funciona?**  
A: Não com Spring Boot 2.7.x. Precisaria migrar para Spring Boot 3.1+.

---

## 📞 SUPORTE

Se após instalar Java 17 ainda houver erros:
1. Capture a saída completa do comando `mvnw.cmd clean compile -X`
2. Verifique se `JAVA_HOME` está configurado: `echo %JAVA_HOME%`
3. Verifique se o caminho está correto: `where java`
