docker compose down --rmi local -v gateway-server
./mvnw -pl gateway-server -am clean package
docker compose build --no-cache gateway-server
docker compose up -d gateway-server