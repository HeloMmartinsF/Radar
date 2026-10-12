@echo off
echo ========================================
echo COMMIT: Perfil Social com Admin
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor"

echo [1/4] Adicionando arquivos principais...

REM Modelo, Repository, Service, Controller
git add radar/src/main/java/br/com/radarfornecedor/radar/model/PerfilSocial.java
git add radar/src/main/java/br/com/radarfornecedor/radar/repository/PerfilSocialRepository.java
git add radar/src/main/java/br/com/radarfornecedor/radar/service/PerfilSocialService.java
git add radar/src/main/java/br/com/radarfornecedor/radar/controller/PerfilSocialController.java

echo [2/4] Adicionando repositorios atualizados...

REM Repositories modificados
git add radar/src/main/java/br/com/radarfornecedor/radar/repository/ProdutoRepository.java
git add radar/src/main/java/br/com/radarfornecedor/radar/repository/PublicacaoRepository.java
git add radar/src/main/java/br/com/radarfornecedor/radar/repository/ClienteRepository.java

echo [3/4] Adicionando frontend...

REM Páginas HTML
git add radar/src/main/resources/static/pages/perfil.html
git add radar/src/main/resources/static/pages/admin-perfis.html
git add radar/src/main/resources/static/layout/menu-lateral.html

REM Pasta de uploads (vazia mas necessária)
git add radar/src/main/resources/static/uploads/perfis/.gitkeep 2>nul

echo [4/4] Adicionando documentacao...

REM Documentações
git add radar/PERFIL_SOCIAL_IMPLEMENTADO.md
git add radar/ADMIN_PERFIS_IMPLEMENTADO.md
git add radar/CORRECAO_APLICADA.md
git add GUIA_TESTE_PERFIL_SOCIAL.txt

echo.
echo ========================================
echo Arquivos adicionados! Criando commit...
echo ========================================
echo.

git commit -m "feat: Perfil Social com auto-criacao e painel admin

FUNCIONALIDADES PRINCIPAIS:
- Auto-criacao de perfil sem aprovacao (fornecedores/clientes/representantes)
- Usuarios editam livremente seus perfis (biografia, foto, privacidade)
- Admin lista e edita TODOS os perfis via /pages/admin-perfis.html
- Zero duplicacao de dados (produtos/publicacoes vem das tabelas originais)

BACKEND:
- Model: PerfilSocial (OneToOne com Usuario)
- Repository: PerfilSocialRepository (findByUsuario, findByUsuarioId)
- Service: PerfilSocialService (montarPerfilCompleto com agregacao de dados)
- Controller: PerfilSocialController (endpoints com controle de acesso)
- Atualizados: ProdutoRepository, PublicacaoRepository, ClienteRepository

ENDPOINTS API:
- GET /api/perfil/meu - Busca perfil do usuario (auto-cria se nao existir)
- PUT /api/perfil - Atualiza proprio perfil
- POST /api/perfil/foto - Upload foto de perfil
- GET /api/perfil/todos - Admin lista todos (somente ADMIN)
- GET /api/perfil/{usuarioId} - Admin ve qualquer perfil
- PUT /api/perfil/{usuarioId} - Admin edita qualquer perfil

FRONTEND:
- /pages/perfil.html - Perfil estilo Instagram (abas: Sobre, Produtos, Publicacoes)
- /pages/admin-perfis.html - Painel admin (tabela, filtros, modal de edicao)
- menu-lateral.html - Link 'Meu Perfil' com badge roxo NOVO

SEGURANCA:
- Verificacao usuario.getTipo() == TipoUsuario.ADMIN
- Usuarios normais veem apenas perfis publicos
- Admin bypassa restricoes de privacidade
- Session management via getAttribute('usuario')

CONFORMIDADE:
- Regra 06: Zero queries destrutivas (apenas INSERT/UPDATE)
- Regra 01: Banco DBLRadar.db mantido no padrao
- Regra 02: Arquivos static copiados para src/ e target/"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo COMMIT CRIADO COM SUCESSO!
    echo ========================================
    echo.
    echo Deseja fazer PUSH para o repositorio remoto?
    echo.
    choice /C SN /M "Pressione S para Sim ou N para Nao"
    
    if errorlevel 2 (
        echo.
        echo Commit local criado. Execute 'git push' quando quiser enviar.
    ) else (
        echo.
        echo [PUSH] Enviando para repositorio remoto...
        git push
        
        if %ERRORLEVEL% EQU 0 (
            echo.
            echo ========================================
            echo PUSH CONCLUIDO COM SUCESSO!
            echo ========================================
            echo.
            echo Perfil Social salvo no GitHub/repositorio remoto!
        ) else (
            echo.
            echo [ERRO] Falha no push. Execute 'git push' manualmente.
        )
    )
) else (
    echo.
    echo ========================================
    echo ERRO AO CRIAR COMMIT
    echo ========================================
    echo Verifique mensagens acima.
)

echo.
pause
