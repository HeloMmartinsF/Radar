@echo off
chcp 65001 >nul
echo ========================================
echo  INSERIR SUBCATEGORIAS FALTANTES
echo ========================================
echo.
echo Este script vai adicionar 110 subcategorias para:
echo   - Industrial (10 subcategorias)
echo   - Limpeza e Higiene (10)
echo   - Segurança (10)
echo   - Pet (10)
echo   - Energia (10)
echo   - Esportes e Fitness (10)
echo   - Gráfica e Personalizados (10)
echo   - Brinquedos e Infantil (10)
echo   - Artesanato e Hobbies (10)
echo   - Casa e Decoração (10)
echo   - Outros (10)
echo.
echo IMPORTANTE: O servidor Spring Boot precisa estar rodando!
echo.
pause

echo.
echo Verificando se Python está instalado...
python --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Python não encontrado!
    echo    Instale Python em: https://www.python.org/downloads/
    echo    Ou execute manualmente via API
    pause
    exit /b 1
)

echo ✅ Python encontrado
echo.
echo Instalando biblioteca requests (se necessário)...
python -m pip install requests --quiet --disable-pip-version-check

echo.
echo ========================================
echo  EXECUTANDO INSERÇÃO
echo ========================================
echo.
python inserir_subcategorias_faltantes.py

echo.
pause
