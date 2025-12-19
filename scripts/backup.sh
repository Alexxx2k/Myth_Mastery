#!/bin/bash
CONTAINER_NAME="postgres_container"
DB_USER="postgres"
DB_NAME="postgres"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_FILE="${SCRIPT_DIR}/../src/main/resources/sql/backup_before_reset.sql"

echo "Creating a backup copy..."

docker exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" -d "$DB_NAME" > "$BACKUP_FILE"

echo "The backup is saved!"
