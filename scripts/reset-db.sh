#!/bin/bash
set -e

CONTAINER_NAME="postgres_container"
DB_USER="postgres"
DB_NAME="postgres"
PG_VOLUME="myth_mastery_pg_data"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_BEFORE_RESET="${SCRIPT_DIR}/../src/main/resources/sql/backup_before_reset.sql"

echo "Using configuration:"
echo "Container: $CONTAINER_NAME"
echo "User: $DB_USER"
echo "DB: $DB_NAME"
echo "Volume: $PG_VOLUME"

echo "Creating a backup before reset..."
docker exec "$CONTAINER_NAME" pg_dump -U "$DB_USER" -d "$DB_NAME" > "$BACKUP_BEFORE_RESET"

if [ $? -ne 0 ]; then
    echo "Error creating backup! Operation aborted."
    exit 1
fi

echo "Backup saved as $BACKUP_BEFORE_RESET."

echo "Stopping the container..."
docker-compose down

echo "Removing old data..."
docker volume rm "$PG_VOLUME"

echo "Restarting the container..."
docker-compose up -d

echo "Database reset complete!"
