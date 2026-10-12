@echo off
cd /d C:\Projeto\Projeto_Radar_Fornecedor\radar
call mvnw.cmd clean compile > compile_output.log 2>&1
echo Compilacao concluida. Verifique compile_output.log
pause
