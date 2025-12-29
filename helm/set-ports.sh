#!/bin/bash

echo "Setting ports for microservices of Best Bank application..."

NAMESPACE=bank-app

declare -A SERVICES=(
  ["keycloak"]=8080
  ["accounts-service"]=8081
  ["transfers-service"]=8082
  ["cash-service"]=8083
  ["notification-service"]=8084
  ["front-ui"]=8085
  ["blocker-service"]=8086
  ["exchange-service"]=8087
  ["exchange-generator"]=8088
  ["kafka-ui"]=8079
)

for svc in "${!SERVICES[@]}"; do
  port=${SERVICES[$svc]}
  echo "🔌 Forwarding $svc → localhost:$port"
  kubectl port-forward -n "$NAMESPACE" "svc/$svc" "$port:$port" >/dev/null 2>&1 &
done

echo ""
echo "✅ All port-forwards started."
echo "⚠️ They run in the background."
echo "To stop them:"
echo "  pkill -f \"kubectl port-forward -n $NAMESPACE\""
read -r -p "Press ENTER to exit"
