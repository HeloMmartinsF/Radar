@echo off
cd /d C:\Projeto\Projeto_Radar_Fornecedor
git add -A > commit_output.txt 2>&1
git commit -m "ajustes finais" >> commit_output.txt 2>&1
echo Commit concluido! >> commit_output.txt
