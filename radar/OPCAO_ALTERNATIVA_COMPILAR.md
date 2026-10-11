# 🚀 OPÇÃO ALTERNATIVA: Compilar Sem Instalar Java 17

Se você **não puder** instalar Java 17 imediatamente, há uma solução temporária usando o **Java 25 com flag de compatibilidade**.

---

## ⚠️ AVISO

Esta solução é **experimental** e pode não funcionar 100%. A solução recomendada continua sendo instalar Java 17.

---

## 🔧 Tentativa 1: Forçar Compatibilidade Java 17

Edite o arquivo `pom.xml` e force o Maven a compilar como se fosse Java 17:

### Modificação no pom.xml

Substitua a seção `<build>` inteira por:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.12.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <release>17</release>
                <compilerArgs>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                </compilerArgs>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Comando para testar

Depois de editar o `pom.xml`:

```cmd
cd c:\Projeto\Projeto_Radar_Fornecedor\radar
mvnw.cmd clean compile -X
```

---

## 🔧 Tentativa 2: Downgrade do Spring Boot Parent (ARRISCADO)

**⚠️ RISCO:** Pode quebrar funcionalidades existentes.

Edite `pom.xml` e altere a versão do Spring Boot:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.5</version>  <!-- Antes: 2.7.14 -->
    <relativePath/>
</parent>
```

**Mudanças necessárias no código:**

1. **Renomear imports `javax` para `jakarta`:**
   ```java
   // ANTES
   import javax.persistence.*;
   import javax.validation.*;
   
   // DEPOIS
   import jakarta.persistence.*;
   import jakarta.validation.*;
   ```

2. **Atualizar dependências SQLite:**
   ```xml
   <dependency>
       <groupId>org.xerial</groupId>
       <artifactId>sqlite-jdbc</artifactId>
       <version>3.45.0.0</version>
   </dependency>
   ```

**Isso requer refatoração massiva e pode quebrar o sistema!**

---

## 🔧 Tentativa 3: Usar Maven com Fork Desabilitado

Crie `COMPILAR_FORCE_JAVA17.bat`:

```batch
@echo off
cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

set MAVEN_OPTS=-Xmx1024m --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED

call mvnw.cmd clean compile -Dmaven.compiler.fork=false

pause
```

Execute esse `.bat` e veja se compila.

---

## 🎯 CONCLUSÃO

**Nenhuma dessas opções é garantida.** O mais confiável é:

1. **Instalar Java 17:** Execute `INSTALAR_JAVA_17.bat`
2. **Verificar:** `java -version` deve mostrar "17.0.X"
3. **Compilar:** `COMPILAR_COM_JAVA_CORRETO.bat`

Java 17 é LTS (suporte até 2029) e totalmente compatível com o projeto.

---

## 📞 SE NADA FUNCIONAR

Considere usar um ambiente isolado:

### Opção: Docker (se disponível)
```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean package
CMD ["java", "-jar", "target/radar-0.0.1-SNAPSHOT.jar"]
```

### Opção: Máquina Virtual
Se você tem VirtualBox/VMware, crie uma VM Windows com Java 17 instalado.

---

## ❓ POR QUE JAVA 25 NÃO FUNCIONA?

- Spring Boot 2.7.x foi testado até Java 19
- Java 20-25 mudaram APIs internas do compilador (`TypeTag`, `Symbol`, `Type`)
- Maven Compiler Plugin 3.10.1 não reconhece essas mudanças
- Resultado: `ExceptionInInitializerError` fatal

**Solução definitiva = Java 17 LTS**
