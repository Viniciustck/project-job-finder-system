#!/bin/bash

# Verificar se .env existe
if [ -f .env ]; then
  # Carrega variáveis ignorando linhas com # ou vazias
  # grep -v '^#' remove comentários
  # xargs deixa tudo numa linha só para o export
  export $(grep -v '^#' .env | xargs)
  echo "✅ Variáveis de ambiente carregadas do arquivo .env"
else
  echo "⚠️ Arquivo .env não encontrado. O sistema pode falhar se as chaves não estiverem configuradas no sistema."
fi

# Rodar a aplicação diretamente com o Maven instalado
echo "🚀 Iniciando Job Hunter..."
mvn spring-boot:run
